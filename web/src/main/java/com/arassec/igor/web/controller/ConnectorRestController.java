package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.ConnectorManager;
import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.ConnectorListEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * REST controller for {@link Connector}s.
 */
@RestController
@RequestMapping("/api/connector")
@RequiredArgsConstructor
public class ConnectorRestController {

    /**
     * The connector manager.
     */
    private final ConnectorManager connectorManager;

    /**
     * The job manager.
     */
    private final JobManager jobManager;

    /**
     * Returns all available connectors.
     *
     * @return List of available connectors.
     */
    @GetMapping
    public ModelPage<ConnectorListEntry> getConnectors(
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "2147483647") int pageSize,
            @RequestParam(value = "nameFilter", required = false) String nameFilter) {
        ModelPage<Connector> connectorsPage = connectorManager.loadPage(pageNumber, pageSize, nameFilter);
        if (connectorsPage != null && !connectorsPage.getItems().isEmpty()) {
            ModelPage<ConnectorListEntry> result = new ModelPage<>(pageNumber, pageSize, connectorsPage.getTotalPages(), null);

            result.setItems(connectorsPage.getItems().stream().map(connector -> {
                ModelPage<Pair<String, String>> referencingJobs = connectorManager.getReferencingJobs(connector.getId(), 0, 1);
                return new ConnectorListEntry(connector.getId(), connector.getName(),
                        (referencingJobs != null && !referencingJobs.getItems().isEmpty()));
            }).collect(Collectors.toList()));

            return result;
        }
        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

    /**
     * Returns all connectors which are a candidate for the supplied type IDs.
     *
     * @param types The required connector types.
     *
     * @return List of available connectors of the given types.
     */
    @GetMapping(value = "candidate/{types}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelPage<Connector> getConnectorCandidates(@PathVariable("types") String types,
                                                       @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                                                       @RequestParam(value = "pageSize", required = false, defaultValue = "2147483647") int pageSize) {
        // Type-IDs are comma separated and Base64 encoded:
        Set<String> typeIds = new HashSet<>();
        Stream.of(types.split(",")).filter(Objects::nonNull).forEach(typeId -> typeIds.add(new String(Base64.getDecoder().decode(typeId))));
        return connectorManager.loadAllOfType(typeIds, pageNumber, pageSize);
    }

    /**
     * Returns the connector with the given ID.
     *
     * @param id The connector's ID.
     *
     * @return The connector.
     */
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Connector getConnector(@PathVariable("id") String id) {
        Connector connector = connectorManager.load(id);
        if (connector != null) {
            return connector;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Connector not found");
    }

    /**
     * Deletes the connector with the given ID.
     *
     * @param id The connector's ID.
     */
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConnector(@PathVariable("id") String id,
                                @RequestParam(value = "deleteAffectedJobs", required = false, defaultValue = "false")
                                        Boolean deleteAffectedJobs) {
        ModelPage<Pair<String, String>> referencingJobs = getReferencingJobs(id, 0, Integer.MAX_VALUE);
        if (referencingJobs.getItems() != null && Boolean.TRUE.equals(deleteAffectedJobs)) {
            referencingJobs.getItems().forEach(jobReference -> jobManager.delete(jobReference.getKey()));
        } else if (referencingJobs.getItems() != null) {
            referencingJobs.getItems().forEach(jobReference -> {
                Job job = jobManager.load(jobReference.getKey());
                job.setActive(false);
                jobManager.save(job);
            });
        }
        connectorManager.deleteConnector(id);
    }

    /**
     * Creates a new connector.
     *
     * @param connector The new connector configuration.
     *
     * @return The connector JSON on success.
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Connector createConnector(@Valid @RequestBody Connector connector) {
        if (connectorManager.loadByName(connector.getName()) == null) {
            return connectorManager.save(connector);
        } else {
            throw new IllegalArgumentException(RestControllerExceptionHandler.NAME_ALREADY_EXISTS_ERROR);
        }
    }

    /**
     * Updates an existing connector.
     *
     * @param connector The new connector configuration.
     */
    @PutMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateConnector(@Valid @RequestBody Connector connector) {
        Connector existingConnectorWithSameName = connectorManager.loadByName(connector.getName());
        if (existingConnectorWithSameName == null || existingConnectorWithSameName.getId().equals(connector.getId())) {
            connectorManager.save(connector);
        } else {
            throw new IllegalArgumentException(RestControllerExceptionHandler.NAME_ALREADY_EXISTS_ERROR);
        }
    }

    /**
     * Tests the supplied connector configuration.
     *
     * @param connector The connector configuration.
     *
     * @return The string 'OK' on success, an error message if the test was not successful.
     */
    @PostMapping("test")
    public ResponseEntity<String> testConnector(@Valid @RequestBody Connector connector) {
        try {
            connector.testConfiguration();
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } catch (Exception e) {
            String result = e.getMessage();
            if (e.getCause() != null) {
                result += " (" + e.getCause().getMessage() + ")";
            }
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Checks whether a connector name has already been taken or not.
     *
     * @param encodedName The connector's Base64 encoded name.
     * @param id          The connector's ID.
     *
     * @return {@code true} if a connector with the provided name already exists, {@code false} otherwise.
     */
    @GetMapping("check/{name}/{id}")
    public Boolean checkConnectorName(@PathVariable("name") String encodedName, @PathVariable("id") String id) {
        String name = new String(Base64.getDecoder().decode(encodedName));
        Connector existingConnector = connectorManager.loadByName(name);
        return (existingConnector != null && !(existingConnector.getId().equals(id)));
    }

    /**
     * Returns the jobs that reference this connector.
     *
     * @param id The connector's ID.
     *
     * @return The jobs.
     */
    @GetMapping(value = "{id}/job-references", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelPage<Pair<String, String>> getReferencingJobs(@PathVariable("id") String id,
                                                              @RequestParam(value = "pageNumber", required = false, defaultValue
                                                                      = "0") int pageNumber,
                                                              @RequestParam(value = "pageSize", required = false, defaultValue =
                                                                      "2147483647") int pageSize) {
        ModelPage<Pair<String, String>> referencingJobs = connectorManager.getReferencingJobs(id, pageNumber, pageSize);
        if (referencingJobs != null) {
            return referencingJobs;
        }
        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

}
