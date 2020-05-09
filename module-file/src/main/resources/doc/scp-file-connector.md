# SCP Service
A service that connects to an SCP server.

# Parameters
The service can be configured by the following parameters:

Parameter | Description
---|:---|
Host | The host running the SSH server.
Port | The port, the SSH server is listening on.
Username | A username for authentication/authorization.
Password | A password for authentication/authorization.
Stric hostkey checking | If checked, the host key of the server will be verified. If unchecked, the host key is ignored.
Preferred authentications | List of preferred SSH authentication methods for this service.
Timeout | A timeout in seconds after which requests to the SSH server will be aborted.
