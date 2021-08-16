# FTP Connector

## Description
A file-connector providing access to an FTP server.

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Host | The host running the server.
Port | The port, the server is listening on.
Username | An optional username for authentication/authorization.
Password | An optional password for authentication/authorization.
Passive Mode | If checked, the connector will use FTP passive mode to connect to the FTP(S) server.
Buffer Size | The size of the buffer **in bytes** that is used by the connector to e.g. copy files.
Data Timeout | A timeout **in milliseconds** after which a data connection will be aborted if no new data is received from the FTP(S) server.
Keep Alive Timeout | A timeout **in minutes** after which the connection to the FTP(S) server will be aborted if no new data is received.
Windows Ftp | If checked, the connector expects the FTP(S) server to be running on a Microsoft Windows operating system.
