package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.schema.ParameterDefinition;
import com.arassec.igor.core.application.schema.ServiceCategory;
import com.arassec.igor.core.application.schema.ServiceType;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.web.api.model.ServiceModel;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: Document class.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api")
public class ServiceRestController {

    @Autowired
    private ServiceManager serviceManager;

    @GetMapping("/service/category")
    public List<ServiceCategory> getServiceCategories() {
        return serviceManager.loadCategories();
    }

    @GetMapping("/service/category/{categoryType}")
    public ResponseEntity<List<ServiceType>> getServiceTypes(@PathVariable("categoryType") String categoryType) {
        return new ResponseEntity<>(serviceManager.loadTypesOfCategory(categoryType), HttpStatus.OK);
    }

    @GetMapping("/service/type/{type}")
    public List<ParameterDefinition> getTypeParameters(@PathVariable("type") String type) {
        return serviceManager.loadParametersOfType(type);
    }

    @GetMapping("/service/ids")
    public List<String> getServiceIds() {
        List<Service> services = serviceManager.loadAll();
        return services.stream().map(service -> service.getId()).collect(Collectors.toList());
    }

    @GetMapping("/service/{id}")
    public ResponseEntity<ServiceModel> getService(@PathVariable("id") String id) {
        Service service = serviceManager.load(id);
        if (service != null) {
            ServiceModel serviceModel = new ServiceModel();
            serviceModel.setId(service.getId());
            serviceModel.setServiceCategory(serviceManager.getCategory(service));
            serviceModel.setServiceType(serviceManager.getType(service));
            serviceModel.setParameters(serviceManager.loadParameters(service));
            return new ResponseEntity<>(serviceModel, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/service")
    public ResponseEntity<String> saveService(@RequestBody String serviceProperties) {
        JSONObject properties = new JSONObject(serviceProperties);
        String id = properties.getString("id");
        if (StringUtils.isEmpty(id)) {
            return new ResponseEntity<>("ID must not be null!", HttpStatus.BAD_REQUEST);
        }
        String type = properties.getString("type");
        Map<String, Object> parameters = jsonToMap(properties.getJSONObject("parameters"));
        Service service = serviceManager.createService(type, parameters);
        service.setId(id);
        serviceManager.save(service);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/service/test")
    public ResponseEntity<String> testService(@RequestBody String serviceProperties) {
        JSONObject properties = new JSONObject(serviceProperties);
        String type = properties.getString("type");
        Service service = serviceManager.createService(type, jsonToMap(properties.getJSONObject("parameters")));
        try {
            service.testConfiguration();
            return new ResponseEntity<>("Test result: OK", HttpStatus.OK);
        } catch (ServiceException e) {
            return new ResponseEntity<>("Test result: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/service/{id}")
    public void deleteService(@PathVariable("id") String id) {
        serviceManager.deleteService(id);
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
