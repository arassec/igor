package com.arassec.igor.web.controller;

import com.arassec.igor.application.manager.ConnectorManager;
import com.arassec.igor.application.manager.JobManager;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.TransferData;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        Job job = jobManager.load(id);
        if (job == null) {
            throw new IllegalArgumentException("No job with ID '" + id + "' found!");
        }
        try {
            TransferData transferData = new TransferData();

            transferData.getJobJsons().add(objectMapper.writeValueAsString(job));

            Set<Pair<String, String>> referencedConnectors = jobManager.getReferencedConnectors(id);
            referencedConnectors.forEach(referencedConnector -> {
                try {
                    transferData.getConnectorJsons().add(
                            objectMapper.writeValueAsString(connectorManager.load(referencedConnector.getKey())));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Could not convert connector to JSON!", e);
                }
            });

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
        Connector connector = connectorManager.load(id);
        if (connector == null) {
            throw new IllegalArgumentException("No connector with ID '" + id + "' found!");
        }

        try {
            TransferData transferData = new TransferData();

            transferData.getConnectorJsons().add(objectMapper.writeValueAsString(connector));

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

        transferData.getConnectorJsons().forEach(connectorJson -> {
            try {
                connectorManager.save(objectMapper.readValue(connectorJson, Connector.class));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Could not import connector data!", e);
            }
        });

        transferData.getJobJsons().forEach(jobJson -> {
            try {
                jobManager.save(objectMapper.readValue(jobJson, Job.class));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Could not import job data!", e);
            }
        });

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
