## Overview

Authorization Server for all other services which grants tokens for the backend resource services. All other secured services must set jwk uri for endpoint implemented on this service. This authorization service based on [RBAC pattern](https://en.wikipedia.org/wiki/Role-based_access_control).

### Endpoints
#### Authentication
Method	| Path	| Description	| User authenticated	
------------- | ------------------------- | ------------- |:-------------:|
POST	| /auth-role/auth	| System endpoint to get JWT |  × |

### Permission
Manage permissions information

Method	| Path	| Description	| User authenticated	| Permission
------------- | ------------------------- | ------------- |:-------------:| :-------------:|
GET	| /permissions/page/{page}/limit/{limit}	| Get all permissions by pages	|  ✓ | PERM_READ_PERMISSION |
GET	| /permissions/{id}		| Get permission by id	|  ✓ | PERM_READ_PERMISSION |
POST| /permissions	| Create new permission | ✓  | PERM_WRITE_PERMISSION |
DELETE	| /permissions/{id}	| Delete permission by id | ✓  | PERM_DELETE_PERMISSION |

### Role
Manage roles information

Method	| Path	| Description	| User authenticated	| Permission
------------- | ------------------------- | ------------- |:-------------:| :-------------:|
GET	| /roles/page/{page}/limit/{limit}	| Get all roles by pages	|  ✓ | PERM_READ_ROLE |
GET	| /roles/{id}		| Get role by id	|  ✓ | PERM_READ_ROLE |
POST| /roles	| Create new role | ✓  | PERM_WRITE_ROLE |
DELETE	| /roles/{id}	| Delete role by id | ✓  | PERM_DELETE_ROLE |
POST| /roles/assign	| Assign permissions for role | ✓  | PERM_WRITE_ROLE |

### Credential
Manage credentials information

Method	| Path	| Description	| User authenticated	| Permission
------------- | ------------------------- | ------------- |:-------------:| :-------------:|
GET	| /credentials/page/{page}/limit/{limit}	| Get all credentials by pages	|  ✓ | PERM_READ_CREDENTIAL |
GET	| /credentials/{username}		| Get credential by user name	|  ✓ | PERM_READ_CREDENTIAL |
GET	| /credentials/roles/{username}		| Get all roles by user name	|  ✓ | PERM_READ_CREDENTIAL |
GET	| /credentials/permissions/{username}		| Get all permissions by user name	|  ✓ | PERM_READ_CREDENTIAL |
POST| /credentials	| Create new credential | ✓  | PERM_WRITE_CREDENTIAL |
DELETE	| /credentials/{username}	| Delete credential by username | ✓  | PERM_DELETE_CREDENTIAL |
POST| /credentials/assign	| Assign role for credential | ✓  | PERM_WRITE_CREDENTIAL |

You can check out the examples [here](https://github.com/congcoi123/product-order-services/blob/develop/systems/auth-role/ENDPOINTS.md) !
