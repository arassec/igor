# 'Receive E-Mail' Action

## Description

This action receives E-Mails and creates a new data item for every received E-Mail. 

## Parameters

The action can be configured by the following parameters.

Parameter | Description
---|:---|
Email connector | The E-Mail IMAP connector to use for receiving E-Mails.
Folder Name | The name of the IMAP folder to read mails from, e.g. 'INBOX'.
Only New | If checked, only new mails will be received by this action.
Delete Processed | If checked, received E-Mails will be deleted by the action. 
Save Attachments | If checked, attachments of the received E-Mails are saved in the configured directory (see below).
Attachment Directory | The target directory for downloaded attachments.

## Example

For every data item processed by the action, E-Mails are retrieved from the configured server.

The created new data items contain the E-Mails data, e.g.:

``` json
{
  "data": {
    ...
  },
  "meta": {
    ...
  },
  "message": {
    "headers": {
      "Return-Path": "<andreas.sensen@arassec.com>",
      "Delivered-To": "igor@igor-test.dev",
      "From": "andreas.sensen@arassec.com",
      "To": "igor@igor-test.dev",
      "Subject": "Test E-Mail",
      "MIME-Version": "1.0",
      "Content-Type": "multipart/mixed"
    },
    "recipients": [
      "igor@igor-test.dev"
    ],
    "bodies": [
      {
        "contentType": "text/plain; charset=us-ascii",
        "content": "Just a simple test..."
      }
    ],
    "from": "andreas.sensen@arassec.com"
  }
}
```
