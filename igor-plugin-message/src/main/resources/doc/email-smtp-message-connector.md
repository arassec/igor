# 'E-Mail Sender (SMTP)' Connector

## Description
A connector that uses the SMTP protocol to send E-Mails.

## Parameters
The connector can be configured with the following parameters:

Parameter | Description
---|:---|
Host | The host running the SMTP service.
Port | The port, the SMTP service is listening on.
Username | The username for authentication.
Password | The password for authentication.
Enable tls | If checked, TLS is used to secure the communication with the service.
Always  trust ssl | If checked, the SSL certificate provided by the service is always accepted.
Connection timeout | Timeout in milliseconds before a connection attempt is aborted.
Read timeout | Timeout in milliseconds before reading messages is aborted.
