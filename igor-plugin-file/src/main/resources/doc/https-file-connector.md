# HTTPS Connector

## Description
A file-connector that connects to an HTTPS server.

## Parameters
The connector can be configured by the following parameters:

Parameter | Description
---|:---|
Host | The host running the HTTPS server.
Port | The port, the HTTPS server is listening on.
Follow redirects | If checked, igor will follow HTTP redirects. If unchecked, an error will be thrown.
Username | An optional username for authentication/authorization.
Password | An optional password for authentication/authorization.
Timeout | A timeout in seconds after which requests to the HTTP server will be aborted.
Certificate verification | If checked, the SSL certificate of the server is verified. If unchecked, the SSL certificate of the server is ignored.