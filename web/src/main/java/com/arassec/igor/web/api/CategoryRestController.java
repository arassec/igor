package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * REST-Controller for category requests.
 */
@RestController()
@RequestMapping("/api/category")
public class CategoryRestController {

    /**
     * Manager for services.
     */
    @Autowired
    private ServiceManager serviceManager;

    /**
     * Manager for actions.
     */
    @Autowired
    private ActionManager actionManager;

    /**
     * Manager for providers.
     */
    @Autowired
    private ProviderManager providerManager;

    /**
     * Returns all service categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available service categories.
     */
    @GetMapping("service")
    public Set<KeyLabelStore> getServiceCategories() {
        return serviceManager.getCategories();
    }

    /**
     * Returns all action categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("provider")
    public Set<KeyLabelStore> getProviderCategories() {
        return providerManager.getCategories();
    }

    /**
     * Returns all action categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("action")
    public Set<KeyLabelStore> getActionCategories() {
        return actionManager.getCategories();
    }

}
