"""
Turns the permission catalogue (lob.permissions in cf-application/src/main/resources/application.yml, needs PyYAML)
into the Keycloak model used by the seed (sync_permissions.py) and the migration (migrate_keycloak.py):

- permissions: client roles of the `reeve-api` client, named resource:action
- module roles: realm roles composed of permissions (catalogue "roles")
- per organisation: group /organisations/<name> (attributes organisationId and modules, organisationRole mapped on it) with the
  subgroup layout of the catalogue ("groups": relative path -> realm role)
"""
from pathlib import Path

import yaml

PERMISSIONS_CLIENT = "reeve-api"
BACKEND_CLIENT = "reeve-backend"
BACKEND_ADMIN_ROLES = ["view-users", "query-groups", "view-clients", "view-realm", "manage-users"]
ORGANISATIONS_GROUP = "organisations"
# An organisation group is named after the organisation; the id the backend uses is in this attribute
ORGANISATION_ID_ATTRIBUTE = "organisationId"
# The flat roles of earlier versions -> the groups giving exactly the same access in the organisation. Only used to move
# users off them: nothing reads these roles anymore. Keep in sync with what the old roles could do, not with new rights.
LEGACY_ROLE_GROUPS = {
    "reeve_auditor": ["transactions", "reporting", "funding", "document_vault/editor"],
    "reeve_accountant": ["transactions/setup", "transactions/importer", "transactions/reviewer", "transactions/reconciler",
                         "reporting/editor", "funding/event_editor", "funding/setup", "document_vault/editor"],
    "reeve_admin": ["org_admin", "transactions/setup/erp", "transactions/importer", "transactions/reviewer", "transactions/reconciler", "transactions/publish_viewer",
                    "reporting/setup", "reporting/publish_viewer", "funding/project_manager", "funding/event_editor", "funding/publisher", "funding/setup",
                    "document_vault/editor", "document_vault/publisher", "document_vault/admin"],
    "reeve_account_manager": ["org_admin", "transactions/reconciler", "transactions/importer", "transactions/reviewer", "transactions/publisher",
                              "transactions/dashboard_editor", "transactions/setup/erp", "reporting/editor", "reporting/publisher", "reporting/setup",
                              "funding/event_editor", "funding/project_manager", "funding/publisher", "funding/setup",
                              "document_vault/editor", "document_vault/publisher"],
}
LEGACY_ROLES = list(LEGACY_ROLE_GROUPS)
# These could create organisations, which is now /platform/administrators
LEGACY_PLATFORM_ROLES = ["reeve_admin", "reeve_account_manager"]
PLATFORM_ADMIN_GROUP = "administrators"
# Bundles of an earlier version of this branch: /organisations/<org>/presets/<name> and their realm roles
PRESET_FOLDER = "presets"
LEGACY_PRESET_GROUPS = {"auditor": "reeve_auditor", "accountant": "reeve_accountant", "admin": "reeve_admin", "account_manager": "reeve_account_manager"}
PRESET_ROLES = ["preset_auditor", "preset_accountant", "preset_admin", "preset_account_manager"]
# Subgroups of an earlier layout (viewer roles now sit on the module folder groups, erp moved under setup); removed when empty
OBSOLETE_GROUPS = ["transactions/viewer", "reporting/viewer", "funding/viewer", "transactions/erp"]
# Access is read from Keycloak's Admin API, so this client's tokens only identify the user: without full scope they carry no
# roles. Not done by removing the role mappers of the `roles` scope: that scope is shared, and the account console needs them.
FRONTEND_CLIENT = "webclient"


def _by_name(entries, key="name"):
    return {entry[key]: {field: value for field, value in entry.items() if field != key} for entry in entries}


def _roles_by_path(entries):
    return {entry["path"]: entry["role"] for entry in entries}


class Catalogue:
    def __init__(self, path):
        data = yaml.safe_load(Path(path).read_text())["lob"]["permissions"]
        self.modules = data["modules"]
        self.resources = _by_name(data["resources"])
        self.organisation_role = data["organisation-role"]
        self.roles = _by_name(data["roles"])
        self.groups = _roles_by_path(data["groups"])
        platform = data.get("platform", {})
        self.platform_group = platform.get("group", "platform")
        self.platform_roles = _by_name(platform.get("roles", []))
        self.platform_groups = {group["name"]: group["role"] for group in platform.get("groups", [])}

    def permissions(self):
        """[(name, description)] for every resource:action."""
        result = []
        for resource, definition in self.resources.items():
            modules = ", ".join(definition.get("modules", [])) or "core"
            for action in definition["actions"]:
                result.append((f"{resource}:{action}", f"{action} on {resource} ({modules})"))
        return result

    def realm_roles(self):
        """[(name, description, realm role composites, permission composites)] for module roles and platform roles."""
        result = [(name, role["description"], [], role["permissions"]) for name, role in self.roles.items()]
        result += [(name, role["description"], [], role["permissions"]) for name, role in self.platform_roles.items()]
        return result

    def organisation_group_tree(self):
        """Nested subgroup layout under one organisation: {name: (role or None, children)}."""
        tree = {}
        for relative_path, role in self.groups.items():
            node = tree
            segments = relative_path.split("/")
            for i, segment in enumerate(segments):
                entry = node.setdefault(segment, [None, {}])
                if i == len(segments) - 1:
                    entry[0] = role
                node = entry[1]
        return tree
