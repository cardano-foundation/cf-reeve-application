#!/usr/bin/env python3
"""
Migrates an existing Keycloak realm to the group-based access model, through the Admin REST API.
Dry run by default: prints what it would do. Pass --apply to make the changes. Safe to run again.

1. permissions: client roles of `reeve-api` (created if missing)
2. module roles and presets: realm roles with their composites (missing ones created, missing composites added)
3. the `reeve-backend` service-account client (created if missing) with the realm-management roles it needs
4. /platform/administrators (create organisations) and, for every organisation (found in users' legacy `organisations` attribute, under /organisations, or --organisation):
   /organisations/<name> with the organisationId attribute, the organisation role and the subgroup layout (existing groups and attributes kept)
   Groups still named after the id are renamed to their displayName and get the organisationId attribute; without a
   displayName they keep the id as name until someone renames them in the admin console (that's safe, the id is in the attribute)
   (members of role subgroups of earlier layouts, e.g. /organisations/<orgId>/reeve_accountant, are moved to the matching preset)
   The legacy preset subgroups are only added to organisations with legacy users or legacy subgroups, or kept where they already exist
5. every user with the legacy `organisations` attribute and/or a legacy reeve_* role is put in the matching preset
   group of each of their organisations, then the attribute and the directly assigned legacy roles are removed
6. the reeve_* realm roles get a "(legacy)" description, or are deleted with --delete-legacy-roles

Usage:
  python3 migrate_keycloak.py --url https://keycloak.example.org --catalogue <reeve-permissions.json> \\
      --admin-user admin [--admin-password ... | env KEYCLOAK_ADMIN_PASSWORD] [--realm reeve-master] [--apply]
"""
import argparse
import getpass
import json
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request

from reeve_model import BACKEND_ADMIN_ROLES, BACKEND_CLIENT, LEGACY_ROLES, OBSOLETE_GROUPS, ORGANISATION_ID_ATTRIBUTE, ORGANISATIONS_GROUP, PERMISSIONS_CLIENT, Catalogue

ORGANISATION_ID = re.compile(r"[0-9a-f]{64}")


class Admin:
    def __init__(self, url, realm, user, password, apply):
        self.url = url.rstrip("/")
        self.realm = realm
        self.apply = apply
        body = urllib.parse.urlencode({"grant_type": "password", "client_id": "admin-cli", "username": user, "password": password}).encode()
        token = self._request("POST", f"{self.url}/realms/master/protocol/openid-connect/token", body, {"Content-Type": "application/x-www-form-urlencoded"}, auth=False)
        self.token = token["access_token"]

    def _request(self, method, url, data=None, headers=None, auth=True):
        request = urllib.request.Request(url, data=data, method=method, headers=headers or {})
        if auth:
            request.add_header("Authorization", f"Bearer {self.token}")
        try:
            with urllib.request.urlopen(request) as response:
                raw = response.read()
                location = response.headers.get("Location")
                if raw:
                    return json.loads(raw)
                return {"location": location} if location else None
        except urllib.error.HTTPError as e:
            if e.code == 404:
                return None
            raise SystemExit(f"{method} {url} failed: {e.code} {e.read().decode()[:300]}")

    def get(self, path, **params):
        query = f"?{urllib.parse.urlencode(params)}" if params else ""
        return self._request("GET", f"{self.url}/admin/realms/{self.realm}{path}{query}")

    def write(self, method, path, body=None, description=""):
        print(("  APPLY " if self.apply else "  would ") + description)
        if not self.apply:
            return None
        data = json.dumps(body).encode() if body is not None else None
        return self._request(method, f"{self.url}/admin/realms/{self.realm}{path}", data, {"Content-Type": "application/json"})


def q(value):
    return urllib.parse.quote(value, safe="")


def created_id(result):
    """Id of a created resource: Keycloak returns it in the body (child groups) or in the Location header."""
    if not result:
        return None
    if result.get("id"):
        return result["id"]
    return result["location"].rsplit("/", 1)[-1] if result.get("location") else None


def organisation_id_of(group):
    """The organisation id of an organisation group: its attribute, or its name for groups of the earlier layout."""
    values = (group.get("attributes") or {}).get(ORGANISATION_ID_ATTRIBUTE)
    return values[0] if values else group["name"]


def readable_name(group, organisation, taken):
    """The name an organisation group should have: its displayName (from the earlier layout) or its current name."""
    display_name = ((group.get("attributes") or {}).get("displayName") or [group["name"]])[0].strip().replace("/", "-")
    if display_name != group["name"] and display_name in taken:
        return f"{display_name} ({organisation[:8]})"
    return display_name


def ensure_client(admin, client_id, representation):
    clients = admin.get("/clients", clientId=client_id) or []
    if clients:
        return clients[0]["id"]
    created = admin.write("POST", "/clients", representation, f"create client {client_id}")
    return created_id(created) or f"<new {client_id}>"


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--url", required=True)
    parser.add_argument("--realm", default="reeve-master")
    parser.add_argument("--catalogue", required=True, help="path to reeve-permissions.json")
    parser.add_argument("--admin-user", required=True, help="admin of the master realm")
    parser.add_argument("--admin-password", default=os.environ.get("KEYCLOAK_ADMIN_PASSWORD"))
    parser.add_argument("--organisation", action="append", default=[], help="extra organisation id to set up (repeatable)")
    parser.add_argument("--delete-legacy-roles", action="store_true")
    parser.add_argument("--apply", action="store_true", help="make the changes (default: dry run)")
    args = parser.parse_args()

    catalogue = Catalogue(args.catalogue)
    admin = Admin(args.url, args.realm, args.admin_user, args.admin_password or getpass.getpass("Keycloak admin password: "), args.apply)
    print(f"{'APPLYING' if args.apply else 'DRY RUN'} on realm {args.realm} at {args.url}\n")

    print("1. Permissions (client roles of reeve-api)")
    api_id = ensure_client(admin, PERMISSIONS_CLIENT, {"clientId": PERMISSIONS_CLIENT, "name": "Reeve API", "enabled": True, "bearerOnly": True, "publicClient": False})
    existing_permissions = {role["name"] for role in (admin.get(f"/clients/{api_id}/roles", max=10000) or [])} if not api_id.startswith("<") else set()
    for name, description in catalogue.permissions():
        if name not in existing_permissions:
            admin.write("POST", f"/clients/{api_id}/roles", {"name": name, "description": description}, f"create permission {name}")

    print("2. Module roles and presets")
    for name, description, role_composites, permission_composites in catalogue.realm_roles():
        if admin.get(f"/roles/{q(name)}") is None:
            admin.write("POST", "/roles", {"name": name, "description": description}, f"create role {name}")
            current = set()
        else:
            current = {role["name"] for role in admin.get(f"/roles/{q(name)}/composites") or []}
        missing_roles = [role for role in role_composites if role not in current]
        missing_permissions = [permission for permission in permission_composites if permission not in current]
        if missing_roles or missing_permissions:
            body = []
            if args.apply:
                body = [admin.get(f"/roles/{q(role)}") for role in missing_roles]
                body += [admin.get(f"/clients/{api_id}/roles/{q(permission)}") for permission in missing_permissions]
            admin.write("POST", f"/roles/{q(name)}/composites", body, f"add to {name}: {len(missing_roles)} roles, {len(missing_permissions)} permissions")

    print("3. reeve-backend service account")
    backend_id = ensure_client(admin, BACKEND_CLIENT, {
        "clientId": BACKEND_CLIENT, "name": "Reeve backend", "enabled": True, "publicClient": False, "clientAuthenticatorType": "client-secret",
        "serviceAccountsEnabled": True, "standardFlowEnabled": False, "implicitFlowEnabled": False, "directAccessGrantsEnabled": False})
    if not backend_id.startswith("<"):
        service_user = admin.get(f"/clients/{backend_id}/service-account-user")
        management = (admin.get("/clients", clientId="realm-management") or [{}])[0]
        assigned = {role["name"] for role in admin.get(f"/users/{service_user['id']}/role-mappings/clients/{management['id']}") or []}
        missing = [role for role in BACKEND_ADMIN_ROLES if role not in assigned]
        if missing:
            body = [admin.get(f"/clients/{management['id']}/roles/{q(role)}") for role in missing] if args.apply else []
            admin.write("POST", f"/users/{service_user['id']}/role-mappings/clients/{management['id']}", body, f"give reeve-backend {missing}")
        secret = admin.get(f"/clients/{backend_id}/client-secret")
        print(f"  reeve-backend secret (set LOB_KEYCLOAK_ADMIN_CLIENT_SECRET): {secret.get('value') if secret else '?'}")
    else:
        print("  the secret is printed when run with --apply")

    print("4. Organisation groups")
    users = admin.get("/users", max=100000, briefRepresentation="false") or []
    organisations = set(args.organisation)
    legacy_organisations = set()
    for user in users:
        legacy_organisations.update((user.get("attributes") or {}).get("organisations", []))
    organisations.update(legacy_organisations)
    root = admin.get(f"/group-by-path/{ORGANISATIONS_GROUP}")
    if root is None:
        root = {"id": created_id(admin.write("POST", "/groups", {"name": ORGANISATIONS_GROUP}, f"create /{ORGANISATIONS_GROUP}")) or "<root>"}
    organisation_groups = (admin.get(f"/groups/{root['id']}/children", max=10000, briefRepresentation="false") or []) if not root["id"].startswith("<") else []
    existing_orgs = {organisation_id_of(group): group for group in organisation_groups}
    taken_names = {group["name"] for group in organisation_groups}
    organisations.update(existing_orgs)
    organisation_paths = {}  # organisation id -> "/organisations/<name>"
    group_ids = {}  # "/organisations/<name>/<relative path>" -> id

    def ensure_children(parent_id, parent_path, layout):
        children = {group["name"]: group for group in (admin.get(f"/groups/{parent_id}/children", max=10000) or [])} if not parent_id.startswith("<") else {}
        for name, (role, grandchildren) in layout.items():
            path = f"{parent_path}/{name}"
            if name in children:
                group_id = children[name]["id"]
                mapped = {r["name"] for r in admin.get(f"/groups/{group_id}/role-mappings/realm") or []}
            else:
                group_id = created_id(admin.write("POST", f"/groups/{parent_id}/children", {"name": name}, f"create group {path}")) or f"<{path}>"
                mapped = set()
            if role and role not in mapped:
                admin.write("POST", f"/groups/{group_id}/role-mappings/realm", [admin.get(f"/roles/{q(role)}")] if args.apply else [], f"map {role} on {path}")
            group_ids[path] = group_id
            ensure_children(group_id, path, grandchildren)

    tree = catalogue.organisation_group_tree()
    tree_with_legacy = catalogue.organisation_group_tree(include_legacy=True)
    preset_folders = {path.split("/", 1)[0] for path in catalogue.legacy_groups}

    def uses_presets(organisation, org_id):
        """Presets are only for organisations coming from the legacy roles; new organisations don't get them."""
        if organisation in legacy_organisations:
            return True
        if org_id.startswith("<"):
            return False
        return any(child["name"] in preset_folders or catalogue.preset_for_legacy_role(child["name"])
                   for child in admin.get(f"/groups/{org_id}/children", max=10000) or [])

    for organisation in sorted(organisations):
        if organisation in existing_orgs:
            group = existing_orgs[organisation]
            org_id = group["id"]
            attributes = dict(group.get("attributes") or {})
            has_id = ORGANISATION_ID_ATTRIBUTE in attributes
            if not has_id and not ORGANISATION_ID.fullmatch(organisation):
                print(f"  WARNING: /{ORGANISATIONS_GROUP}/{group['name']} has no {ORGANISATION_ID_ATTRIBUTE} attribute and its name isn't an organisation id; "
                      f"it gives no access until the attribute is set by hand")
                continue
            name = readable_name(group, organisation, taken_names)
            path = f"/{ORGANISATIONS_GROUP}/{name}"
            if not has_id or "displayName" in attributes or name != group["name"]:
                attributes[ORGANISATION_ID_ATTRIBUTE] = [organisation]
                attributes.pop("displayName", None)
                taken_names.add(name)
                admin.write("PUT", f"/groups/{org_id}", {"name": name, "attributes": attributes},
                            f"rename /{ORGANISATIONS_GROUP}/{group['name']} to {path}, {ORGANISATION_ID_ATTRIBUTE}={organisation[:12]}…")
            mapped = {r["name"] for r in admin.get(f"/groups/{org_id}/role-mappings/realm") or []}
        else:
            path = f"/{ORGANISATIONS_GROUP}/{organisation}"
            org_id = created_id(admin.write("POST", f"/groups/{root['id']}/children",
                                            {"name": organisation, "attributes": {ORGANISATION_ID_ATTRIBUTE: [organisation], "modules": catalogue.modules}},
                                            f"create group {path} (all modules; rename it in the admin console to a readable name)")) or f"<{path}>"
            mapped = set()
        organisation_paths[organisation] = path
        if catalogue.organisation_role not in mapped:
            admin.write("POST", f"/groups/{org_id}/role-mappings/realm", [admin.get(f"/roles/{q(catalogue.organisation_role)}")] if args.apply else [],
                        f"map {catalogue.organisation_role} on {path}")
        ensure_children(org_id, path, tree_with_legacy if uses_presets(organisation, org_id) else tree)
        for obsolete in OBSOLETE_GROUPS:
            parent_path, name = obsolete.rsplit("/", 1)
            parent_id = group_ids.get(f"{path}/{parent_path}")
            if not parent_id or parent_id.startswith("<"):
                continue
            existing = next((g for g in admin.get(f"/groups/{parent_id}/children", max=10000) or [] if g["name"] == name), None)
            if existing is None:
                continue
            members = admin.get(f"/groups/{existing['id']}/members", max=1) or []
            if members or existing.get("subGroupCount", 0) > 0:
                print(f"  WARNING: {path}/{obsolete} is from an earlier layout but not empty; move its members to {path}/{parent_path} and delete it")
            else:
                admin.write("DELETE", f"/groups/{existing['id']}", None, f"delete obsolete empty group {path}/{obsolete}")
        # Role subgroups of an earlier layout, named after a legacy role (e.g. /organisations/<org>/reeve_accountant):
        # move their members to the matching preset group, then delete them
        if not org_id.startswith("<"):
            for legacy_group in admin.get(f"/groups/{org_id}/children", max=10000) or []:
                preset = catalogue.preset_for_legacy_role(legacy_group["name"])
                if not preset:
                    continue
                preset_path = f"{path}/{catalogue.preset_group(preset)}"
                for member in admin.get(f"/groups/{legacy_group['id']}/members", max=100000) or []:
                    admin.write("PUT", f"/users/{member['id']}/groups/{group_ids.get(preset_path, '<new>')}", None,
                                f"add {member['username']} to {preset_path} (was in {path}/{legacy_group['name']})")
                admin.write("DELETE", f"/groups/{legacy_group['id']}", None, f"delete group {path}/{legacy_group['name']} (earlier layout)")

    print("4b. Platform groups")
    platform_root = admin.get(f"/group-by-path/{catalogue.platform_group}")
    if platform_root is None:
        platform_root = {"id": created_id(admin.write("POST", "/groups", {"name": catalogue.platform_group}, f"create /{catalogue.platform_group}"))
                         or f"<{catalogue.platform_group}>"}
    ensure_children(platform_root["id"], f"/{catalogue.platform_group}", {name: [role, {}] for name, role in catalogue.platform_groups.items()})
    print(f"  add organisation creators to /{catalogue.platform_group}/administrators by hand (Users → Groups → Join group)")

    print("5. Users")
    for user in users:
        attributes = user.get("attributes") or {}
        user_organisations = attributes.get("organisations", [])
        legacy = [role for role in admin.get(f"/users/{user['id']}/role-mappings/realm") or [] if role["name"] in LEGACY_ROLES]
        if not user_organisations and not legacy:
            continue
        print(f"  {user['username']}: organisations={[o[:8] for o in user_organisations]} legacy roles={[r['name'] for r in legacy]}")
        if legacy and not user_organisations:
            print("    WARNING: legacy role but no organisations attribute; add the user to groups by hand")
        current = {group["path"] for group in admin.get(f"/users/{user['id']}/groups", max=10000) or []}
        for organisation in user_organisations:
            for role in legacy:
                preset = catalogue.preset_for_legacy_role(role["name"])
                path = f"{organisation_paths[organisation]}/{catalogue.preset_group(preset)}"
                if path not in current:
                    admin.write("PUT", f"/users/{user['id']}/groups/{group_ids.get(path, '<new>')}", None, f"add {user['username']} to {path}")
        if legacy:
            admin.write("DELETE", f"/users/{user['id']}/role-mappings/realm", legacy, f"unassign {[r['name'] for r in legacy]} from {user['username']}")
        if user_organisations:
            full = admin.get(f"/users/{user['id']}")
            full.get("attributes", {}).pop("organisations", None)
            admin.write("PUT", f"/users/{user['id']}", full, f"remove the organisations attribute of {user['username']}")

    print("6. Legacy roles")
    for role in LEGACY_ROLES:
        representation = admin.get(f"/roles/{q(role)}")
        if representation is None:
            continue
        if args.delete_legacy_roles:
            admin.write("DELETE", f"/roles/{q(role)}", None, f"delete role {role}")
            continue
        description = (f"(legacy) Not used anymore: replaced by {catalogue.preset_for_legacy_role(role)}. "
                       "Delete it with migrate_keycloak.py --delete-legacy-roles once nobody uses it.")
        if representation.get("description") != description:
            representation["description"] = description
            admin.write("PUT", f"/roles/{q(role)}", representation, f"mark {role} as legacy")

    print("\nDone." if args.apply else "\nDry run only: re-run with --apply to make these changes.")


if __name__ == "__main__":
    sys.exit(main())
