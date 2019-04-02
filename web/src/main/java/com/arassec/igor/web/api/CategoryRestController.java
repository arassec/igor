package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.TriggerManager;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import com.arassec.igor.web.api.util.Sorter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
     * Manager for triggers.
     */
    @Autowired
    private TriggerManager triggerManager;

    /**
     * Returns all service categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available service categories.
     */
    @GetMapping("service")
    public List<KeyLabelStore> getServiceCategories() {
        return Sorter.sortByLabel(serviceManager.getCategories());
    }

    /**
     * Returns all action categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("provider")
    public List<KeyLabelStore> getProviderCategories() {
        return Sorter.sortByLabel(providerManager.getCategories());
    }

    /**
     * Returns all action categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("action")
    public List<KeyLabelStore> getActionCategories() {
        return Sorter.sortByLabel(actionManager.getCategories());
    }

    /**
     * Returns all trigger categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available trigger categories.
     */
    @GetMapping("trigger")
    public List<KeyLabelStore> getTriggerCategories() {
        return Sorter.sortByLabel(triggerManager.getCategories());
    }

}
