package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.ServiceListEntry;
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
 * REST controller for {@link Service}s.
 */
@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
public class ServiceRestController {

    /**
     * The service manager.
     */
    private final ServiceManager serviceManager;

    /**
     * The job manager.
     */
    private final JobManager jobManager;

    /**
     * Returns all available services.
     *
     * @return List of available services.
     */
    @GetMapping
    public ModelPage<ServiceListEntry> getServices(
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "2147483647") int pageSize,
            @RequestParam(value = "nameFilter", required = false) String nameFilter) {
        ModelPage<Service> servicesPage = serviceManager.loadPage(pageNumber, pageSize, nameFilter);
        if (servicesPage != null && !servicesPage.getItems().isEmpty()) {
            ModelPage<ServiceListEntry> result = new ModelPage<>(pageNumber, pageSize, servicesPage.getTotalPages(), null);

            result.setItems(servicesPage.getItems().stream().map(service -> {
                ModelPage<Pair<String, String>> referencingJobs = serviceManager.getReferencingJobs(service.getId(), 0, 1);
                return new ServiceListEntry(service.getId(), service.getName(),
                        (referencingJobs != null && !referencingJobs.getItems().isEmpty()));
            }).collect(Collectors.toList()));

            return result;
        }
        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

    /**
     * Returns all services which are a candidate for the supplied type IDs.
     *
     * @param types The required service types.
     *
     * @return List of available services of the given types.
     */
    @GetMapping(value = "candidate/{types}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelPage<Service> getServiceCandidates(@PathVariable("types") String types,
                                                   @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                                                   @RequestParam(value = "pageSize", required = false, defaultValue = "2147483647") int pageSize) {
        // Type-IDs are comma separated and Base64 encoded:
        Set<String> typeIds = new HashSet<>();
        Stream.of(types.split(",")).filter(Objects::nonNull).forEach(typeId -> typeIds.add(new String(Base64.getDecoder().decode(typeId))));
        return serviceManager.loadAllOfType(typeIds, pageNumber, pageSize);
    }

    /**
     * Returns the service with the given ID.
     *
     * @param id The service's ID.
     *
     * @return The service.
     */
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Service getService(@PathVariable("id") String id) {
        Service service = serviceManager.load(id);
        if (service != null) {
            return service;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found");
    }

    /**
     * Deletes the service with the given ID.
     *
     * @param id The service's ID.
     */
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable("id") String id,
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
        serviceManager.deleteService(id);
    }

    /**
     * Creates a new service.
     *
     * @param service The new service configuration.
     *
     * @return The service JSON on success.
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Service createService(@Valid @RequestBody Service service) {
        if (serviceManager.loadByName(service.getName()) == null) {
            return serviceManager.save(service);
        } else {
            throw new IllegalArgumentException(RestControllerExceptionHandler.NAME_ALREADY_EXISTS_ERROR);
        }
    }

    /**
     * Updates an existing service.
     *
     * @param service The new service configuration.
     */
    @PutMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateService(@Valid @RequestBody Service service) {
        Service existingServiceWithSameName = serviceManager.loadByName(service.getName());
        if (existingServiceWithSameName == null || existingServiceWithSameName.getId().equals(service.getId())) {
            serviceManager.save(service);
        } else {
            throw new IllegalArgumentException(RestControllerExceptionHandler.NAME_ALREADY_EXISTS_ERROR);
        }
    }

    /**
     * Tests the supplied service configuration.
     *
     * @param service The service configuration.
     *
     * @return The string 'OK' on success, an error message if the test was not successful.
     */
    @PostMapping("test")
    public ResponseEntity<String> testService(@Valid @RequestBody Service service) {
        try {
            service.testConfiguration();
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
     * Checks whether a service name has already been taken or not.
     *
     * @param encodedName The service's Base64 encoded name.
     * @param id          The service's ID.
     *
     * @return {@code true} if a service with the provided name already exists, {@code false} otherwise.
     */
    @GetMapping("check/{name}/{id}")
    public Boolean checkServiceName(@PathVariable("name") String encodedName, @PathVariable("id") String id) {
        String name = new String(Base64.getDecoder().decode(encodedName));
        Service existingService = serviceManager.loadByName(name);
        return (existingService != null && !(existingService.getId().equals(id)));
    }

    /**
     * Returns the jobs that reference this service.
     *
     * @param id The service's ID.
     *
     * @return The jobs.
     */
    @GetMapping(value = "{id}/job-references", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelPage<Pair<String, String>> getReferencingJobs(@PathVariable("id") String id,
                                                              @RequestParam(value = "pageNumber", required = false, defaultValue
                                                                      = "0") int pageNumber,
                                                              @RequestParam(value = "pageSize", required = false, defaultValue =
                                                                      "2147483647") int pageSize) {
        ModelPage<Pair<String, String>> referencingJobs = serviceManager.getReferencingJobs(id, pageNumber, pageSize);
        if (referencingJobs != null) {
            return referencingJobs;
        }
        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

}
