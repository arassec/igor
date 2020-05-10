# SFTP Connector
A file-connector that connects to an SFTP server.

# Parameters
The connector can be configured by the following parameters:

Parameter | Description
---|:---|
Host | The host running the SFTP server.
Port | The port, the SFTP server is listening on.
Username | A username for authentication/authorization.
Password | A password for authentication/authorization.
Stric hostkey checking | If checked, the host key of the server will be verified. If unchecked, the host key is ignored.
Preferred authentications | List of preferred SFTP authentication methods for this connector.
Timeout | A timeout in seconds after which requests to the SFTP server will be aborted.
