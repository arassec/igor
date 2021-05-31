package com.arassec.igor.web.controller;

import com.arassec.igor.application.manager.ConnectorManager;
import com.arassec.igor.application.manager.JobManager;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.TransferData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * REST-Controller for transferring igor data (e.g. for import/export functionality).
 */
@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    /**
     * Manager for jobs.
     */
    private final JobManager jobManager;

    /**
     * Manager for connectors.
     */
    private final ConnectorManager connectorManager;

    /**
     * Object-Mapper for igor data.
     */
    private final ObjectMapper objectMapper;

    /**
     * Exports the job with the given ID.
     *
     * @param id       The job's ID.
     * @param response The {@link HttpServletResponse} of the request.
     */
    @GetMapping(value = "job/{id}")
    public void exportJob(@PathVariable("id") String id, HttpServletResponse response) {
        var job = jobManager.load(id);
        if (job == null) {
            throw new IllegalArgumentException("No job with ID '" + id + "' found!");
        }
        try {
            var transferData = new TransferData();

            transferData.getJobJsons().add(objectMapper.convertValue(job, new TypeReference<>() {
            }));

            Set<Pair<String, String>> referencedConnectors = jobManager.getReferencedConnectors(id);
            referencedConnectors.forEach(referencedConnector -> transferData.getConnectorJsons().add(
                objectMapper.convertValue(connectorManager.load(referencedConnector.getKey()), new TypeReference<>() {
                })));

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + job.getId() + ".igor.json");

            response.getOutputStream().write(objectMapper.writeValueAsBytes(transferData));
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new IllegalStateException("Could not create transfer data!", e);
        }
    }

    /**
     * Exports the connector with the given ID.
     *
     * @param id       The connector's ID.
     * @param response The {@link HttpServletResponse} of the request.
     */
    @GetMapping(value = "connector/{id}")
    public void exportConnector(@PathVariable("id") String id, HttpServletResponse response) {
        var connector = connectorManager.load(id);
        if (connector == null) {
            throw new IllegalArgumentException("No connector with ID '" + id + "' found!");
        }

        try {
            var transferData = new TransferData();

            transferData.getConnectorJsons().add(objectMapper.convertValue(connector, new TypeReference<>() {
            }));

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + connector.getId() + ".igor.json");

            response.getOutputStream().write(objectMapper.writeValueAsBytes(transferData));
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new IllegalStateException("Could not convert connector to JSON!", e);
        }
    }

    /**
     * Imports transfer data.
     *
     * @param transferData The data to import.
     *
     * @return The string "OK" if the import succeeded.
     */
    @PostMapping
    public ResponseEntity<String> importTransferData(@RequestBody TransferData transferData) {

        transferData.getConnectorJsons().forEach(connectorJson ->
            connectorManager.save(objectMapper.convertValue(connectorJson, Connector.class)));

        transferData.getJobJsons().forEach(jobJson -> jobManager.save(objectMapper.convertValue(jobJson, Job.class)));

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
