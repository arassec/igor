# Connector

## Description
External services can be configured in igor using **connectors**. 
A connector can be used in job configurations to access the service.

Connectors are grouped in the following categories:

- **File:** connectors of this category can be used to process files, e.g. copy a file from one place to another using two connectors. 
Every file-connector is valid for configuration, i.e. you can configure to copy the files from an FTP server via SCP to a remote host.
- **Message:** Connectors of this category handle messages, e.g. send a message with content from the processed data item.

## Parameters
The following parameters can be configured for every connector. 

Parameter | Description
---|:---|
Name | The name of the connector.
