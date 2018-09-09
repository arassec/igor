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
 * TODO: Document class.
 */
@RestController
public class ServiceRestController extends BaseRestController {

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private ServiceUtil serviceUtil;

    @Autowired
    private ParameterUtil parameterUtil;

    @GetMapping("/servicecategory")
    public Set<ServiceCategory> getServiceCategories() {
        return serviceUtil.getServiceCategories();
    }

    @GetMapping("/servicetype/{category}")
    public ResponseEntity<Set<ServiceType>> getServiceTypes(@PathVariable("category") String category) {
        return new ResponseEntity<>(serviceUtil.getTypesByCategory(category), HttpStatus.OK);
    }

    @GetMapping("/serviceparams/{type}")
    public List<ParameterDefinition> getServiceParameters(@PathVariable("type") String type) {
        return parameterUtil.getParameters(serviceManager.createService(type, null));
    }

    @GetMapping("/service")
    public List<ServiceModel> getServiceIds() {
        List<Service> services = serviceManager.loadAll();
        return services.stream().map(service ->
                new ServiceModel(service.getId(), service.getName(), serviceUtil.getCategory(service),
                        serviceUtil.getType(service), parameterUtil.getParameters(service))).collect(Collectors.toList());
    }

    @GetMapping("/service/category/{category}")
    public List<ServiceModel> getServiceIdsInCategory(@PathVariable("category") String category) {
        List<Service> services = serviceManager.loadAll();
        return services.stream().map(service ->
                new ServiceModel(service.getId(), service.getName(), serviceUtil.getCategory(service), serviceUtil.getType(service), parameterUtil.getParameters(service)))
                .filter(serviceModel -> serviceModel.getServiceCategory().getType().equals(category))
                .collect(Collectors.toList());
    }

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

    @DeleteMapping("/service/{id}")
    public void deleteService(@PathVariable("id") Long id) {
        serviceManager.deleteService(id);
    }

    @PostMapping("/service")
    public ResponseEntity<String> saveService(@RequestBody String serviceProperties) {
        JSONObject properties = new JSONObject(serviceProperties);
        String name = properties.getString("name");
        String type = properties.getJSONObject("serviceType").getString("type");
        Map<String, Object> parameters = parameterUtil.convertParameters(properties.getJSONArray("parameters"));
        Service service = serviceManager.createService(type, parameters);
        service.setName(name);
        serviceManager.save(service);
        return new ResponseEntity<>(HttpStatus.OK);
    }

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
        return new ResponseEntity<>(HttpStatus.OK);
    }

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
