package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.TriggerManager;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import com.arassec.igor.web.api.util.Sorter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST-Controller for types.
 */
@RestController()
@RequestMapping("/api/type")
@RequiredArgsConstructor
public class TypeRestController {

    /**
     * The service manager.
     */
    private final ServiceManager serviceManager;

    /**
     * Manager for actions.
     */
    private final ActionManager actionManager;

    /**
     * Manager for providers.
     */
    private final ProviderManager providerManager;

    /**
     * Manager for triggers.
     */
    private final TriggerManager triggerManager;

    /**
     * Returns all service types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The service category to use.
     * @return Set of service types.
     */
    @GetMapping("service/{category}")
    public List<KeyLabelStore> getServiceTypes(@PathVariable("category") String category) {
        return Sorter.sortByLabel(serviceManager.getTypesOfCategory(category));
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     * @return Set of action types.
     */
    @GetMapping("action/{category}")
    public List<KeyLabelStore> getActionTypes(@PathVariable("category") String category) {
        return Sorter.sortByLabel(actionManager.getTypesOfCategory(category));
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     * @return Set of action types.
     */
    @GetMapping("provider/{category}")
    public List<KeyLabelStore> getProviderTypes(@PathVariable("category") String category) {
        return Sorter.sortByLabel(providerManager.getTypesOfCategory(category));
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     * @return Set of action types.
     */
    @GetMapping("trigger/{category}")
    public List<KeyLabelStore> getTriggerTypes(@PathVariable("category") String category) {
        return Sorter.sortByLabel(triggerManager.getTypesOfCategory(category));
    }

}
