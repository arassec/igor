# Changelog

## Version 0.2.3
- UI refactoring: 
  - igor's version is displayed in the navigation
  - job configuration and job executions switch places
  - simulation results and documentation can overlap
- Actions can now have a description.

## Version 0.2.2
- Minor changes in 'HTTP request' action.
    - POST, PUT, PATCH and DELETE are no longer executed during simulated job executions
    - All 2XX HTTP status codes are now accepted as successful
    - Mustache templates can now be used in headers
- Minor changes in HTTP connector (all 2XX HTTP status codes are now accepted as successful during connector tests)
- Added new property to 'Filter by Regular Expression' action to drop matching items if configured.

## Version 0.2.1
- Replaced JSON-Path support in parameters with Mustache templates.

## Version 0.2.0
- Added Skip action to skip the first 'n' data items of a stream.
- Added Limit action to filter data items from the stream after the first 'n' data items have been processed.
- Added 'Split Array' action to split an input array into multiple data items.
- Added Log action to debug-log processed data items.
- Added 'List Files' action to replace the 'List Files' provider.
- Removed Providers completely
- Added 'Web-Hook' trigger to start a job by web requests.
- Triggers now support JSON input to enhance data and meta-data of the initial data item.
- Added HTTP connector to support web requests.
- Added 'HTTP Request' action to support web requests.
- Updated 'Filter by Timestamp' action to support epoch timestamps in seconds and milliseconds.

## Version 0.1.0
- First stable release with core features.