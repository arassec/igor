# HTTP Connector

## Description
The HTTP connector can be used to query web servers via HTTP/HTTPS.


The java.net.http.HttpClient is used internally to connect to servers. All configuration options available, e.g. with JVM parameters, will automatically be used by the connector.

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Test Url | An HTTP(S) URL that is queried via HTTP-GET when the connector's configuration is tested by the user.
Proxy Host | An optional proxy server for HTTP(S) connections.
Proxy Port | The port of the proxy server.
Keymanager Keystore | Path to a keystore file containing the HTTP client's keys.
Keymanager Password | Password for the keymanager's keystore file.
Keymanager Type | Type of the keymanager's keystore.
Trustmanager Keystore | Path to a keystore file containing trusted server certificates.
Trustmanager Password | Password for the trustmanager's keystore file.
Trustmanager Type | Type of the trustmanager's keystore.
Certificate Verification | If checked, server SSL certificates will be validated by the connector. If unchecked, all provided certificates will be accepted, **THUS DISABLING SECURITY!**
Follow Redirects | If checked, the connector will follow redirects in a secure way (e.g. not leaving SSL connections). If unchecked, HTTP redirects will be treated as errors.
