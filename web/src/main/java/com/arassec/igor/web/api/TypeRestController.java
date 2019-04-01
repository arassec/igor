package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.TriggerManager;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * REST-Controller for types.
 */
@RestController()
@RequestMapping("/api/type")
public class TypeRestController {

    /**
     * The service manager.
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
     * Manager for triggers.
     */
    @Autowired
    private TriggerManager triggerManager;

    /**
     * Returns all service types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The service category to use.
     * @return Set of service types.
     */
    @GetMapping("service/{category}")
    public Set<KeyLabelStore> getServiceTypes(@PathVariable("category") String category) {
        return serviceManager.getTypesOfCategory(category);
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     * @return Set of action types.
     */
    @GetMapping("action/{category}")
    public Set<KeyLabelStore> getActionTypes(@PathVariable("category") String category) {
        return actionManager.getTypesOfCategory(category);
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     * @return Set of action types.
     */
    @GetMapping("provider/{category}")
    public Set<KeyLabelStore> getProviderTypes(@PathVariable("category") String category) {
        return providerManager.getTypesOfCategory(category);
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     * @return Set of action types.
     */
    @GetMapping("trigger/{category}")
    public Set<KeyLabelStore> getTriggerTypes(@PathVariable("category") String category) {
        return triggerManager.getTypesOfCategory(category);
    }

}
