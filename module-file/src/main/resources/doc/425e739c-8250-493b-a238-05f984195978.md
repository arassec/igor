# HTTP Service
A service that connects to an HTTP server.

# Parameters
The service can be configured by the following parameters:

Parameter | Description
---|:---|
Host | The host running the HTTP server.
Port | The port, the HTTP server is listening on.
Follow redirects | If checked, igor will follow HTTP redirects. If unchecked, an error will be thrown.
Username | An optional username for authentication/authorization.
Password | An optional password for authentication/authorization.
Timeout | A timeout in seconds after which requests to the HTTP server will be aborted.