package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.converter.JsonParameterConverter;
import com.arassec.igor.core.application.converter.JsonServiceConverter;
import com.arassec.igor.core.application.factory.ModelDefinition;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for {@link Service}s.
 */
public class ServiceRestController extends BaseRestController {

    /**
     * The service manager.
     */
    @Autowired
    private ServiceManager serviceManager;

    /**
     * Converter for services from and to JSON.
     */
    @Autowired
    private JsonServiceConverter jsonServiceConverter;

    /**
     * Converter for parameters to and from JSON.
     */
    @Autowired
    private JsonParameterConverter jsonParameterConverter;

    /**
     * Returns all service categories as {@link ModelDefinition}s.
     *
     * @return Set of all available service categories.
     */
    @GetMapping("/category/service")
    public Set<ModelDefinition> getServiceCategories() {
        return serviceManager.getCategories();
    }

    /**
     * Returns all service types of a certain category as {@link ModelDefinition}s.
     *
     * @param category The service category to use.
     * @return Set of service types.
     */
    @GetMapping("/type/service/{category}")
    public Set<ModelDefinition> getServiceTypes(@PathVariable("category") String category) {
        return serviceManager.getTypesOfCategory(category);
    }

    /**
     * Returns all configuration parameters of a service type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping("/parameters/service/{type}")
    public String getServiceParameters(@PathVariable("type") String type) {
        JSONArray parameters = jsonParameterConverter.convert(serviceManager.createService(type, null), false);
        return parameters.toString();
    }

    /**
     * Returns all available services.
     *
     * @return List of available services.
     */
    @GetMapping("/service")
    public List<String> getServices() {
        List<Service> services = serviceManager.loadAll();
        return services.stream().map(service -> jsonServiceConverter.convert(service, false).toString()).collect(Collectors.toList());
    }

    /**
     * Returns all services of a certain category.
     *
     * @param category The target category.
     * @return List of services in that category.
     */
    @GetMapping("/service/category/{category}")
    public List<String> getServicesInCategory(@PathVariable("category") String category) {
        List<Service> services = serviceManager.loadAllOfCategory(category);
        return services.stream().map(service -> jsonServiceConverter.convert(service, false).toString()).collect(Collectors.toList());
    }

    /**
     * Returns the service with the given ID.
     *
     * @param id The service's ID.
     * @return The service.
     */
    @GetMapping("/service/{id}")
    public String getService(@PathVariable("id") Long id) {
        Service service = serviceManager.load(id);
        if (service != null) {
            return jsonServiceConverter.convert(service, false).toString();
        }
        return null;
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
        //Map<String, Object> parameters = parameterUtil.convertParameters(properties.getJSONArray("parameters"));
        //Service service = serviceManager.createService(type, parameters);
        //service.setName(name);
        //serviceManager.save(service);
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
        //Map<String, Object> parameters = parameterUtil.convertParameters(properties.getJSONArray("parameters"));
        //Service service = serviceManager.createService(type, parameters);
        //service.setId(id);
        //service.setName(name);
        //serviceManager.save(service);
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
        //Map<String, Object> parameters = parameterUtil.convertParameters(properties.getJSONArray("parameters"));
        //Service service = serviceManager.createService(type, parameters);
        try {
            //  service.testConfiguration();
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
