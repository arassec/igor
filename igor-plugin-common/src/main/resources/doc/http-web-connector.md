# HTTP Connector

## Description
The HTTP connector can be used to query web servers via HTTP/HTTPS.

The java.net.http.HttpClient is used internally to connect to servers. 
All configuration options available, e.g. with JVM parameters, will automatically be used by the connector.

## Parameters
The following parameters can be configured for the HTTP connector:

Parameter | Description
---|:---|
Test url | An HTTP(S) URL that is queried via HTTP-GET when the connector's configuration is tested by the user.
Proxy host | Hostname of a proxy server.
Proxy port | Port of the proxy server.
Keymanager keystore | Path to a keystore file containing the HTTP client's keys.
Keymanager password | Password for the keymanager's keystore file.
Keymanager type | Type of the keymanager's keystore.
Trustmanager keystore | Path to a keystore file containing trusted server certificates.
Trustmanager password | Password for the trustmanager's keystore file.
Trustmanager type | Type of the trustmanager's keystore.
Certificate verification | If checked, server SSL certificates will be validated by the connector. If unchecked, all provided certificates will be accepted, THUS DISABLING SECURITY!
Follow redirects | If checked, the connector will follow redirects in a secure way (e.g. not leaving SSL connections). If unchecked, HTTP redirects will be treated as errors. 
