# Changelog

## Version {{ $themeConfig.igorVersion }}
- Refactored common-plugin into core-plugin.
- Added new 'Message' trigger.
- Event-triggered jobs now use real events during simulated job executions.
- Added new core component category: 'test'. 

## Version 0.2.6
- Refactored modules into plugins.
- Inactive jobs are now greyed out on the job overview page.
- Referencing jobs in the connector editor are now ordered alphabetically.
- Errors during simulated job executions are now shown in the job navigator.
- Simulation results of the last action are added as 'stale' to newly created actions.
- Simulation results are now shown in an accordion component.
- Mustache template selections from the simulation results are automatically copied to the clipboard.
- Added 'HTTP File Download' action.
- Removed 'HTTP(S) File' connector.
- Unknown components (e.g. due to removed plugins) no longer cause exceptions during startup, but are indicated in the UI.

::: danger Breaking Changes
- 'Split Array' action now takes a mustache template as 'Array selector' configuration parameter instead of a JSON-Path query.
:::

## Version 0.2.5
- Added 'mustache-selector' for JSON simulation results.

## Version 0.2.4
- Added E2E tests with Cypress
- Fixed missing 'Import connector' functionality
- Fixed detecting changes in connector configurator.
- Updated documentation.
- Minor layout changes.

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
::: danger Breaking Changes
- Replaced JSON-Path support in parameters with Mustache templates.
:::


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