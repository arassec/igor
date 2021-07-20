# 'E-Mail Sender (SMTP)' Connector

## Description
A connector that uses the SMTP protocol to send E-Mails.

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Host | The host providing the E-Mail service (e.g. IMAP or SMTP).
Port | The port of the SMTP service.
Username | The username for authentication.
Password | The password for authentication.
Enable Tls | If checked, TLS is used to secure the communication with the service.
Always Trust Ssl | If checked, the SSL certificate provided by the service is always accepted.
Connection Timeout | Timeout **in milliseconds** before a connection attempt is aborted.
Read Timeout | Timeout **in milliseconds** before reading messages is aborted.
