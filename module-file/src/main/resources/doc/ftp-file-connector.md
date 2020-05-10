# FTP Connector
A file-connector providing access to an FTP server.

# Parameters
The FTP connector can be configured by the following parameters:

Parameter | Description
---|:---|
Host | The host running the FTP server.
Port | The port, the FTP server is listening on.
Username | An optional username for authentication/authorization.
Password | An optional password for authentication/authorization.
Passive mode | If checked, the connector will use FTP passive mode to connect to the FTP server.
Buffer size | The size of the buffer that is used by the connector to e.g. copy files.
Data timeout | A timeout after which a data connection will be aborted if no new data is received from the FTP server.
Keep alive timeout | A timeout after which the connection to the FTP server will be aborted if no new data is received.
Windows ftp | If checked, the connector expects the FTP server to be running on a Microsoft Windows operating system.