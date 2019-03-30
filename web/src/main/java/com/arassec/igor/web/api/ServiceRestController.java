package com.arassec.igor.web.api;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.converter.JsonServiceConverter;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.api.model.ServiceListEntry;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
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
    public List<ServiceListEntry> getServices() {
        List<Service> services = serviceManager.loadAll();
        return services.stream().map(service -> {
            ServiceListEntry result = new ServiceListEntry(service.getId(), service.getName());
            Set<Pair<Long, String>> referencingJobs = serviceManager.getReferencingJobs(service.getId());
            if (referencingJobs == null || referencingJobs.isEmpty()) {
                result.setName(service.getName() + " (unused)");
            }
            return result;
        }).collect(Collectors.toList());
    }

    /**
     * Returns all services of a certain category.
     *
     * @param category The target category.
     * @return List of services in that category.
     */
    @GetMapping(value = "category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getServicesInCategory(@PathVariable("category") String category) {
        JSONArray result = new JSONArray();
        serviceManager.loadAllOfCategory(category).stream().forEach(
                service -> result.put(jsonServiceConverter.convert(service, false, true)));
        return result.toString();
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
        List<Pair<Long, String>> referencingJobs = getReferencingJobs(id);
        if (deleteAffectedJobs) {
            referencingJobs.stream().forEach(jobReference -> {
                jobManager.delete(jobReference.getKey());
            });
        } else {
            referencingJobs.stream().forEach(jobReference -> {
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
        Service service = jsonServiceConverter.convert(new JSONObject(serviceJson), false);
        Service savedService = serviceManager.save(service);
        return jsonServiceConverter.convert(savedService, false, true).toString();
    }

    /**
     * Updates an existing service.
     *
     * @param serviceJson The service configuration in JSON form.
     * @return The string 'service updated' upon success.
     */
    @PutMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateService(@RequestBody String serviceJson) {
        Service service = jsonServiceConverter.convert(new JSONObject(serviceJson), false);
        serviceManager.save(service);
    }

    /**
     * Tests the supplied service configuration.
     *
     * @param serviceJson The service configuration in JSON form.
     * @return The string 'OK' on success, an error message if the test was not successful.
     */
    @PostMapping("test")
    public ResponseEntity<String> testService(@RequestBody String serviceJson) {
        Service service = jsonServiceConverter.convert(new JSONObject(serviceJson), false);
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
     * Returns the jobs that reference this service.
     *
     * @param id The service's ID.
     * @return The jobs.
     */
    @GetMapping(value = "{id}/job-references", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Pair<Long, String>> getReferencingJobs(@PathVariable("id") Long id) {
        List<Pair<Long, String>> result = new LinkedList<>();

        Set<Pair<Long, String>> referencingJobs = serviceManager.getReferencingJobs(id);
        if (referencingJobs != null && !referencingJobs.isEmpty()) {
            result.addAll(referencingJobs);
            Collections.sort(result, Comparator.comparing(Pair::getValue));
        }

        return result;
    }

}
