package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.web.api.model.ParameterDefinition;
import com.arassec.igor.web.api.model.ProviderModel;
import com.arassec.igor.web.api.util.ParameterUtil;
import com.arassec.igor.web.api.util.ProviderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProviderRestController extends BaseRestController {

    @Autowired
    private ProviderUtil providerUtil;

    @Autowired
    private ProviderManager providerManager;

    @Autowired
    private ParameterUtil parameterUtil;

    @GetMapping("/providertype")
    public List<ProviderModel> getProviderTypes() {
        return providerUtil.getProviderModels();
    }

    @GetMapping("/providerparams/{type}")
    public List<ParameterDefinition> getProviderParameters(@PathVariable("type") String type) {
        return parameterUtil.getParameters(providerManager.createProvider(type, null));
    }

}
