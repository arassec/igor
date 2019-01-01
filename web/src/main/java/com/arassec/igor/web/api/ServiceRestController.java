package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.web.api.model.ParameterDefinition;
import com.arassec.igor.web.api.model.ServiceCategory;
import com.arassec.igor.web.api.model.ServiceType;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.web.api.model.ServiceModel;
import com.arassec.igor.web.api.util.ParameterUtil;
import com.arassec.igor.web.api.util.ServiceUtil;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for {@link Service}s.
 */
@RestController
public class ServiceRestController extends BaseRestController {

    /**
     * The service manager.
     */
    @Autowired
    private ServiceManager serviceManager;

    /**
     * Utility for service handling.
     */
    @Autowired
    private ServiceUtil serviceUtil;

    /**
     * Utility for parameter handling.
     */
    @Autowired
    private ParameterUtil parameterUtil;

    /**
     * Returns all {@link ServiceCategory}s.
     *
     * @return Set of all available service categories.
     */
    @GetMapping("/servicecategory")
    public Set<ServiceCategory> getServiceCategories() {
        return serviceUtil.getServiceCategories();
    }

    /**
     * Returns all {@link ServiceType}s of a certain {@link ServiceCategory}.
     *
     * @param category The {@link ServiceCategory} to use.
     * @return Set of service types.
     */
    @GetMapping("/servicetype/{category}")
    public ResponseEntity<Set<ServiceType>> getServiceTypes(@PathVariable("category") String category) {
        return new ResponseEntity<>(serviceUtil.getTypesByCategory(category), HttpStatus.OK);
    }

    /**
     * Returns all configuration parameters of a {@link ServiceType}.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping("/serviceparams/{type}")
    public List<ParameterDefinition> getServiceParameters(@PathVariable("type") String type) {
        return parameterUtil.getParameters(serviceManager.createService(type, null));
    }

    /**
     * Returns all available services.
     *
     * @return List of available services.
     */
    @GetMapping("/service")
    public List<ServiceModel> getServices() {
        List<Service> services = serviceManager.loadAll();
        return services.stream().map(service ->
                new ServiceModel(service.getId(), service.getName(), serviceUtil.getCategory(service),
                        serviceUtil.getType(service), parameterUtil.getParameters(service))).collect(Collectors.toList());
    }

    /**
     * Returns all services of a certain category.
     *
     * @param category The target category.
     * @return List of services in that category.
     */
    @GetMapping("/service/category/{category}")
    public List<ServiceModel> getServicesInCategory(@PathVariable("category") String category) {
        List<Service> services = serviceManager.loadAll();
        return services.stream().map(service ->
                new ServiceModel(service.getId(), service.getName(), serviceUtil.getCategory(service), serviceUtil.getType(service), parameterUtil.getParameters(service)))
                .filter(serviceModel -> serviceModel.getServiceCategory().getType().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Returns the service with the given ID.
     *
     * @param id The service's ID.
     * @return The service.
     */
    @GetMapping("/service/{id}")
    public ResponseEntity<ServiceModel> getService(@PathVariable("id") Long id) {
        Service service = serviceManager.load(id);
        if (service != null) {
            ServiceModel serviceModel = new ServiceModel();
            serviceModel.setId(service.getId());
            serviceModel.setName(service.getName());
            serviceModel.setServiceCategory(serviceUtil.getCategory(service));
            serviceModel.setServiceType(serviceUtil.getType(service));
            serviceModel.setParameters(parameterUtil.getParameters(service));
            return new ResponseEntity<>(serviceModel, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Deletes the service with the given ID.
     *
     * @param id The service's ID.
     */
    @DeleteMapping("/service/{id}")
    public void deleteService(@PathVariable("id") Long id) {
        serviceManager.deleteService(id);
    }

    /**
     * Creates a new service.
     *
     * @param serviceProperties The service configuration in JSON form.
     * @return The string 'service created' upon success.
     */
    @PostMapping("/service")
    public ResponseEntity<String> createService(@RequestBody String serviceProperties) {
        JSONObject properties = new JSONObject(serviceProperties);
        String name = properties.getString("name");
        String type = properties.getJSONObject("serviceType").getString("type");
        Map<String, Object> parameters = parameterUtil.convertParameters(properties.getJSONArray("parameters"));
        Service service = serviceManager.createService(type, parameters);
        service.setName(name);
        serviceManager.save(service);
        return new ResponseEntity<>("service created", HttpStatus.OK);
    }

    /**
     * Updates an existing service.
     *
     * @param serviceProperties The service configuration in JSON form.
     * @return The string 'service updated' upon success.
     */
    @PutMapping("/service")
    public ResponseEntity<String> updateService(@RequestBody String serviceProperties) {
        JSONObject properties = new JSONObject(serviceProperties);
        Long id = properties.getLong("id");
        String name = properties.getString("name");
        String type = properties.getJSONObject("serviceType").getString("type");
        Map<String, Object> parameters = parameterUtil.convertParameters(properties.getJSONArray("parameters"));
        Service service = serviceManager.createService(type, parameters);
        service.setId(id);
        service.setName(name);
        serviceManager.save(service);
        return new ResponseEntity<>("service updated", HttpStatus.OK);
    }

    /**
     * Tests the supplied service configuration.
     *
     * @param serviceProperties The service configuration in JSON form.
     * @return The string 'OK' on success, an error message if the test was not successful.
     */
    @PostMapping("/service/test")
    public ResponseEntity<String> testService(@RequestBody String serviceProperties) {
        JSONObject properties = new JSONObject(serviceProperties);
        String type = properties.getJSONObject("serviceType").getString("type");
        Map<String, Object> parameters = parameterUtil.convertParameters(properties.getJSONArray("parameters"));
        Service service = serviceManager.createService(type, parameters);
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

}
