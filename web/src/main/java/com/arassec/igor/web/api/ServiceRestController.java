package com.arassec.igor.web.api;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.converter.JsonServiceConverter;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.api.error.RestControllerExceptionHandler;
import com.arassec.igor.web.api.model.ServiceListEntry;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for {@link Service}s.
 */
@RestController
@RequestMapping("/api/service")
public class ServiceRestController {

    /**
     * The service manager.
     */
    @Autowired
    private ServiceManager serviceManager;

    /**
     * The job manager.
     */
    @Autowired
    private JobManager jobManager;

    /**
     * Converter for services from and to JSON.
     */
    @Autowired
    private JsonServiceConverter jsonServiceConverter;

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
                ModelPage<Pair<Long, String>> referencingJobs = serviceManager.getReferencingJobs(service.getId(), 0, 1);
                return new ServiceListEntry(service.getId(), service.getName(),
                        (referencingJobs != null && !referencingJobs.getItems().isEmpty()));
            }).collect(Collectors.toList()));

            return result;
        }

        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

    /**
     * Returns all services of a certain category.
     *
     * @param category The target category.
     * @return List of services in that category.
     */
    @GetMapping(value = "category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getServicesInCategory(@PathVariable("category") String category,
                                        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                                        @RequestParam(value = "pageSize", required = false, defaultValue = "2147483647") int pageSize) {
        ModelPage<Service> serviceModelPage = serviceManager.loadAllOfCategory(category, pageNumber, pageSize);

        if (serviceModelPage.getItems() != null) {
            JSONArray items = new JSONArray();
            serviceModelPage.getItems().forEach(service -> items.put(jsonServiceConverter.convert(service, false, true)));

            JSONObject result = new JSONObject();
            result.put("number", pageNumber);
            result.put("size", pageSize);
            result.put("totalPages", serviceModelPage.getTotalPages());
            result.put("items", items);

            return result.toString();
        }

        return new JSONObject().toString();
    }

    /**
     * Returns the service with the given ID.
     *
     * @param id The service's ID.
     * @return The service.
     */
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getService(@PathVariable("id") Long id) {
        Service service = serviceManager.load(id);
        if (service != null) {
            return jsonServiceConverter.convert(service, false, true).toString();
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
    public void deleteService(@PathVariable("id") Long id, @RequestParam Boolean deleteAffectedJobs) {
        ModelPage<Pair<Long, String>> referencingJobs = getReferencingJobs(id, 0, Integer.MAX_VALUE);
        if (referencingJobs.getItems() != null && deleteAffectedJobs) {
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
     * @param serviceJson The service configuration in JSON form.
     * @return The service JSON on success.
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String createService(@RequestBody String serviceJson) {
        Service service = jsonServiceConverter.convert(new JSONObject(serviceJson), null, false);
        if (serviceManager.loadByName(service.getName()) == null) {
            Service savedService = serviceManager.save(service);
            return jsonServiceConverter.convert(savedService, false, true).toString();
        } else {
            throw new IllegalArgumentException(RestControllerExceptionHandler.NAME_ALREADY_EXISTS_ERROR);
        }
    }

    /**
     * Updates an existing service.
     *
     * @param serviceJson The service configuration in JSON form.
     */
    @PutMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateService(@RequestBody String serviceJson) {
        Service service = jsonServiceConverter.convert(new JSONObject(serviceJson), null, false);
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
     * @param serviceJson The service configuration in JSON form.
     * @return The string 'OK' on success, an error message if the test was not successful.
     */
    @PostMapping("test")
    public ResponseEntity<String> testService(@RequestBody String serviceJson) {
        Service service = jsonServiceConverter.convert(new JSONObject(serviceJson), null, false);
        try {
            service.testConfiguration();
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } catch (ServiceException e) {
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
     * @return {@code true} if a service with the provided name already exists, {@code false} otherwise.
     */
    @GetMapping("check/{name}/{id}")
    public Boolean checkServiceName(@PathVariable("name") String encodedName, @PathVariable("id") Long id) {
        String name = new String(Base64.getDecoder().decode(encodedName));
        Service existingService = serviceManager.loadByName(name);
        return (existingService != null && !(existingService.getId().equals(id)));
    }

    /**
     * Returns the jobs that reference this service.
     *
     * @param id The service's ID.
     * @return The jobs.
     */
    @GetMapping(value = "{id}/job-references", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelPage<Pair<Long, String>> getReferencingJobs(@PathVariable("id") Long id,
                                                            @RequestParam(value = "pageNumber", required = false, defaultValue
                                                                    = "0") int pageNumber,
                                                            @RequestParam(value = "pageSize", required = false, defaultValue =
                                                                    "2147483647") int pageSize) {
        ModelPage<Pair<Long, String>> referencingJobs = serviceManager.getReferencingJobs(id, pageNumber, pageSize);
        if (referencingJobs != null) {
            return referencingJobs;
        }
        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

}
