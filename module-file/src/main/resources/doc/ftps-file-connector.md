# FTPS Connector
A file-connector providing access to an FTPS server.

# Parameters
The FTPS connector can be configured by the following parameters:

Parameter | Description
---|:---|
Host | The host running the FTPS server.
Port | The port, the FTPS server is listening on.
Username | An optional username for authentication/authorization.
Password | An optional password for authentication/authorization.
Passive mode | If checked, the connector will use FTP passive mode to connect to the FTPS server.
Buffer size | The size of the buffer that is used by the connector to e.g. copy files.
Data timeout | A timeout after which a data connection will be aborted if no new data is received from the FTPS server.
Keep alive timeout | A timeout after which the connection to the FTPS server will be aborted if no new data is received.
Windows ftp | If checked, the connector expects the FTPS server to be running on a Microsoft Windows operating system.