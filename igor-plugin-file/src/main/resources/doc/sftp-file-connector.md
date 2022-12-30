# SFTP Connector

## Description
A file-connector that connects to an SFTP server.

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Host | The host running the SSH server.
Port | The port, the SSH server is listening on.
Username | A username for authentication/authorization.
Password | A password for authentication/authorization.
Strict Hostkey Checking | If checked, the host key of the server will be verified. If unchecked, the host key is ignored.
Preferred Authentications | List of preferred SSH authentication methods for this connector.
Timeout | A timeout **in milliseconds** after which requests to the SSH server will be aborted.
