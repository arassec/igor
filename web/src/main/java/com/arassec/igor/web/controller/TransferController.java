package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.service.Service;
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
     * Manager for services.
     */
    private final ServiceManager serviceManager;

    /**
     * Object-Mapper for igor data.
     */
    private final ObjectMapper objectMapper;

    /**
     * Exports the job with the given ID.
     *
     * @param id The job's ID.
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

            Set<Pair<String, String>> referencedServices = jobManager.getReferencedServices(id);
            referencedServices.forEach(referencedService -> {
                try {
                    transferData.getServiceJsons().add(
                            objectMapper.writeValueAsString(serviceManager.load(referencedService.getKey())));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Could not convert service to JSON!", e);
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
     * Exports the service with the given ID.
     *
     * @param id The service's ID.
     */
    @GetMapping(value = "service/{id}")
    public void exportService(@PathVariable("id") String id, HttpServletResponse response) {
        Service service = serviceManager.load(id);
        if (service == null) {
            throw new IllegalArgumentException("No service with ID '" + id + "' found!");
        }

        try {
            TransferData transferData = new TransferData();

            transferData.getServiceJsons().add(objectMapper.writeValueAsString(service));

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + service.getId() + ".igor.json");

            response.getOutputStream().write(objectMapper.writeValueAsBytes(transferData));
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new IllegalStateException("Could not convert service to JSON!", e);
        }
    }

    /**
     * Imports transfer data.
     *
     * @param transferData The data to import.
     */
    @PostMapping
    public ResponseEntity<String> importTransferData(@RequestBody TransferData transferData) {

        transferData.getServiceJsons().forEach(serviceJson -> {
            try {
                serviceManager.save(objectMapper.readValue(serviceJson, Service.class));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Could not import service data!", e);
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
