"""
Turns the platform permission catalogue (cf-lob-platform: support/src/main/resources/reeve-permissions.json)
into the Keycloak model used by the seed (sync_permissions.py) and the migration (migrate_keycloak.py):

- permissions: client roles of the `reeve-api` client, named resource:action
- module roles: realm roles composed of permissions (catalogue "roles")
- presets: realm roles composed of module roles + a few permissions (catalogue "presets")
- per organisation: group /organisations/<orgId> (attribute modules, organisationRole mapped on it) with the
  subgroup layout of the catalogue ("groups": relative path -> realm role)
- legacy preset subgroups (catalogue "legacyGroups"): only in organisations seeded or migrated with them, not in new ones
"""
import json
from pathlib import Path

PERMISSIONS_CLIENT = "reeve-api"
BACKEND_CLIENT = "reeve-backend"
BACKEND_ADMIN_ROLES = ["view-users", "query-groups", "view-clients", "view-realm", "manage-users"]
ORGANISATIONS_GROUP = "organisations"
LEGACY_ROLES = ["reeve_account_manager", "reeve_accountant", "reeve_auditor", "reeve_admin"]
# Subgroups of an earlier layout (viewer roles now sit on the module folder groups, erp moved under setup); removed when empty
OBSOLETE_GROUPS = ["transactions/viewer", "reporting/viewer", "funding/viewer", "transactions/erp"]


class Catalogue:
    def __init__(self, path):
        data = json.loads(Path(path).read_text())
        self.modules = data["modules"]
        self.resources = data["resources"]
        self.organisation_role = data["organisationRole"]
        self.roles = data["roles"]
        self.presets = data["presets"]
        self.groups = data["groups"]
        self.legacy_groups = data.get("legacyGroups", {})
        platform = data.get("platform", {})
        self.platform_group = platform.get("group", "platform")
        self.platform_roles = platform.get("roles", {})
        self.platform_groups = platform.get("groups", {})

    def permissions(self):
        """[(name, description)] for every resource:action."""
        result = []
        for resource, definition in self.resources.items():
            modules = ", ".join(definition.get("modules", [])) or "core"
            for action in definition["actions"]:
                result.append((f"{resource}:{action}", f"{action} on {resource} ({modules})"))
        return result

    def realm_roles(self):
        """[(name, description, realm role composites, permission composites)] for module roles, presets, platform roles."""
        result = [(name, role["description"], [], role["permissions"]) for name, role in self.roles.items()]
        result += [(name, preset["description"], preset["roles"], preset["permissions"]) for name, preset in self.presets.items()]
        result += [(name, role["description"], [], role["permissions"]) for name, role in self.platform_roles.items()]
        return result

    def preset_for_legacy_role(self, legacy_role):
        return next((name for name, preset in self.presets.items() if preset.get("replaces") == legacy_role), None)

    def preset_group(self, preset):
        """Relative group path of a preset, e.g. preset_accountant -> presets/accountant."""
        return next(path for path, role in self.legacy_groups.items() if role == preset)

    def organisation_group_tree(self, include_legacy=False):
        """Nested subgroup layout under one organisation: {name: (role or None, children)}."""
        tree = {}
        layout = {**self.groups, **self.legacy_groups} if include_legacy else self.groups
        for relative_path, role in layout.items():
            node = tree
            segments = relative_path.split("/")
            for i, segment in enumerate(segments):
                entry = node.setdefault(segment, [None, {}])
                if i == len(segments) - 1:
                    entry[0] = role
                node = entry[1]
        return tree
