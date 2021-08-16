---
pageClass: docpage
---

# Custom Connectors

## Introduction
Connectors are the key components to connect igor to the outside world. 
There is only one method a custom connector must implement:

``` java
/**
 * Tests the connector's configuration.
 */
void testConfiguration();
```
When the user hits the ![test](../user/images/common/test-button.png) button in the connector editor, this method is executed.
You should implement a reasonable configuration check, e.g. connect to a service you are adding.
In case the configuration is not working, an Exception should be thrown.

## Example Code
A custom connector should extend `BaseConnector` to get sensible defaults for the methods to override.

The code of our custom connector looks like this:

``` java
/**
 * A basic example connector querying google, to demonstrate the usage of igor connectors.
 */
@IgorComponent(categoryId = "Demo-Connectors", typeId = "Custom-Connector")
public class CustomConnector extends BaseConnector {

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
        // The result is ignored, we only want to test the connection...
        getGoogleResult("igor");
    }

    /**
     * Returns the google search result for the given parameter.
     *
     * @return The search result.
     */
    @IgorSimulationSafe
    public String getGoogleResult(String searchParameter) {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.google.com/search?q=" + searchParameter))
                .GET().build();
        try {
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException e) {
            throw new IgorException("Connection failed!", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IgorException("Interrupted!", e);
        }
    }

}
```
Note that the `getGoogleResult()` method can be annoted with `@IgorSimulationSafe` since the search query can be executed during simulations without altering the data.

This connector will be available in igor under the Category- and Type-ID we set in the constructor.

Category|Type
---|---
Demo-Connectors|Custom-Connector

## Usage

In order to use the new connector we set it as parameter in a new action:

``` java
/**
 * A custom action adding a google search result to the data items.
 */
@IgorComponent(categoryId = "Demo-Actions", typeId = "Search-Action")
public class SearchAction extends BaseAction {

    /**
     * The connector providing the search result.
     */
    @NotNull
    @IgorParam
    private CustomConnector connector;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data,
                                             JobExecution jobExecution) {
        String confetti = connector.getGoogleResult("confetti");
        // Instead of parsing the HTML, we simply return the first 50
        // characters... It's a demo after all.
        data.put("searchResults", confetti.substring(0, 50));
        return List.of(data);
    }

}
```

This action will be available in igor under the Category- and Type-ID we set in the constructor.

Category|Type
---|---
Demo-Actions|Search-Action

The new connector instance can be configured as parameter for the action.

The processed data item of our action then looks like this:

``` json
{
  "data": {},
  "meta": {
    "jobId": "2400f526-b5b2-4d7e-b1f7-12e8cb886944",
    "simulation": true,
    "simulationLimit": 25,
    "timestamp": 1605884415869
  },
  "message": "A custom action's message",
  "searchResults": "<!DOCTYPE html><html lang=en><meta charset=utf-8><"
}
```
