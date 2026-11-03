#!/usr/bin/env python3
"""
Syncs keycloak-config/realm-export.json (realm reeve-master, imported only into a fresh Keycloak) with the platform
permission catalogue (cf-lob-platform: support/src/main/resources/reeve-permissions.json):

- every permission becomes a client role of `reeve-api`
- module roles and presets become realm roles; the legacy reeve_* roles are removed
- the `reeve-backend` service-account client is added (the backend reads access from Keycloak with it)
- the /organisations group tree is created for the seeded organisations and every seeded user is put in groups;
  /platform/administrators (create organisations) is created with manager in it
  (no `organisations` attribute, no directly assigned roles)

For an existing Keycloak use migrate_keycloak.py instead.

Usage: python3 sync_permissions.py <path to reeve-permissions.json> [path to realm-export.json]
Re-running it is safe: it replaces what it manages and leaves everything else untouched.
"""
import json
import sys
from pathlib import Path

from reeve_model import BACKEND_ADMIN_ROLES, BACKEND_CLIENT, LEGACY_ROLES, ORGANISATIONS_GROUP, PERMISSIONS_CLIENT, Catalogue

# Local/demo secret only; set a real one in Keycloak for any shared environment
BACKEND_CLIENT_SECRET = "reeve-backend-dev-secret"

CF_ORG = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"

ORGANISATIONS = [
    {"id": CF_ORG, "displayName": "Cardano Foundation", "modules": ["transactions", "reporting", "funding"]},
]

# username -> (organisation, relative group path) memberships; users not listed here keep their groups.
# manager uses the individual module groups (MANAGER_GROUPS) so stages can be added/removed one by one,
# and can create organisations (PLATFORM_MEMBERSHIPS); the others use presets.
MANAGER_GROUPS = ["org_admin", "transactions/reconciler", "transactions/importer", "transactions/reviewer", "transactions/publisher",
                  "transactions/setup/erp", "reporting/editor", "reporting/publisher", "reporting/setup",
                  "funding/event_editor", "funding/project_manager", "funding/publisher", "funding/setup"]

MEMBERSHIPS = {
    "manager": [(CF_ORG, path) for path in MANAGER_GROUPS],
    "admin": [(CF_ORG, "presets/admin")],
    "accountant": [(CF_ORG, "presets/accountant")],
    "auditor": [(CF_ORG, "presets/auditor")],
}

PLATFORM_MEMBERSHIPS = {"manager": ["administrators"]}


def group_representation(name, role, children, attributes=None):
    return {
        "name": name,
        "attributes": attributes or {},
        "realmRoles": [role] if role else [],
        "subGroups": [group_representation(child, child_role, grandchildren) for child, (child_role, grandchildren) in children.items()],
    }


def remove_legacy_organisations_claim(realm):
    """The organisations attribute/claim is no longer used: memberships are groups."""
    realm["clientScopes"] = [scope for scope in realm.get("clientScopes", []) if scope["name"] != "organisations"]
    for client in realm["clients"]:
        if "defaultClientScopes" in client:
            client["defaultClientScopes"] = [scope for scope in client["defaultClientScopes"] if scope != "organisations"]
    for component in realm.get("components", {}).get("org.keycloak.userprofile.UserProfileProvider", []):
        configs = component.get("config", {}).get("kc.user.profile.config", [])
        for i, raw in enumerate(configs):
            profile = json.loads(raw)
            profile["attributes"] = [attribute for attribute in profile.get("attributes", []) if attribute["name"] != "organisations"]
            configs[i] = json.dumps(profile)


def main(catalogue_path, realm_path):
    catalogue = Catalogue(catalogue_path)
    export_path = Path(realm_path)
    export = json.loads(export_path.read_text())
    realms = export if isinstance(export, list) else [export]
    realm = next(r for r in realms if r["realm"] == "reeve-master")

    roles = realm.setdefault("roles", {})
    roles.setdefault("client", {})[PERMISSIONS_CLIENT] = [
        {"name": name, "description": description, "composite": False, "clientRole": True} for name, description in catalogue.permissions()
    ]
    managed = set(LEGACY_ROLES) | {name for name, *_ in catalogue.realm_roles()}
    realm_roles = [role for role in roles.setdefault("realm", []) if role["name"] not in managed]
    for name, description, role_composites, permission_composites in catalogue.realm_roles():
        composites = {}
        if role_composites:
            composites["realm"] = role_composites
        if permission_composites:
            composites["client"] = {PERMISSIONS_CLIENT: permission_composites}
        realm_roles.append({"name": name, "description": description, "composite": True, "composites": composites})
    roles["realm"] = realm_roles

    clients = [c for c in realm["clients"] if c["clientId"] != BACKEND_CLIENT]
    clients.append({
        "clientId": BACKEND_CLIENT,
        "name": "Reeve backend",
        "description": "Service account the backend uses to read organisations, roles and modules",
        "enabled": True,
        "protocol": "openid-connect",
        "publicClient": False,
        "clientAuthenticatorType": "client-secret",
        "secret": BACKEND_CLIENT_SECRET,
        "serviceAccountsEnabled": True,
        "standardFlowEnabled": False,
        "implicitFlowEnabled": False,
        "directAccessGrantsEnabled": False,
    })
    realm["clients"] = clients

    if not any(scope["name"] == "basic" for scope in realm.get("clientScopes", [])):
        # Keycloak 25+ puts "sub" in the access token through the "basic" scope, which older exports lack
        realm.setdefault("clientScopes", []).append({
            "name": "basic",
            "description": "OpenID Connect scope for add all basic claims to the token",
            "protocol": "openid-connect",
            "attributes": {"include.in.token.scope": "false", "display.on.consent.screen": "false"},
            "protocolMappers": [{
                "name": "sub",
                "protocol": "openid-connect",
                "protocolMapper": "oidc-sub-mapper",
                "consentRequired": False,
                "config": {"introspection.token.claim": "true", "access.token.claim": "true"},
            }],
        })
    remove_legacy_organisations_claim(realm)

    # The seeded organisation has seeded users in the legacy presets
    tree = catalogue.organisation_group_tree(include_legacy=True)
    platform_group = {
        "name": catalogue.platform_group,
        "attributes": {},
        "realmRoles": [],
        "subGroups": [{"name": name, "attributes": {}, "realmRoles": [role], "subGroups": []} for name, role in catalogue.platform_groups.items()],
    }
    realm["groups"] = [platform_group, {
        "name": ORGANISATIONS_GROUP,
        "attributes": {},
        "realmRoles": [],
        "subGroups": [
            group_representation(organisation["id"], catalogue.organisation_role, tree,
                                 {"displayName": [organisation["displayName"]], "modules": organisation["modules"]})
            for organisation in ORGANISATIONS
        ],
    }]

    users = [u for u in realm.get("users", []) if u["username"] != f"service-account-{BACKEND_CLIENT}"]
    users.append({
        "username": f"service-account-{BACKEND_CLIENT}",
        "enabled": True,
        "serviceAccountClientId": BACKEND_CLIENT,
        "clientRoles": {"realm-management": BACKEND_ADMIN_ROLES},
    })
    for user in users:
        if user["username"] in MEMBERSHIPS:
            user.pop("realmRoles", None)
            user.get("attributes", {}).pop("organisations", None)
            user["groups"] = [f"/{ORGANISATIONS_GROUP}/{organisation}/{path}" for organisation, path in MEMBERSHIPS[user["username"]]]
            user["groups"] += [f"/{catalogue.platform_group}/{name}" for name in PLATFORM_MEMBERSHIPS.get(user["username"], [])]
    realm["users"] = users

    export_path.write_text(json.dumps(export, indent=2, ensure_ascii=False) + "\n")
    print(f"{len(catalogue.permissions())} permissions, {len(catalogue.realm_roles())} roles/presets, "
          f"{len(ORGANISATIONS)} organisations, {len(MEMBERSHIPS)} users in groups written to {export_path}")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else str(Path(__file__).resolve().parent.parent / "realm-export.json"))
