package com.arassec.igor.web.api;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.KeyLabelStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST-Controller for category requests.
 */
@RestController()
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryRestController extends BaseRestController {

    /**
     * The igor component registry.
     */
    private final IgorComponentRegistry igorComponentRegistry;

    /**
     * Returns all service categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available service categories.
     */
    @GetMapping("service")
    public List<KeyLabelStore> getServiceCategories() {
        return sortByLabel(igorComponentRegistry.getCategoriesOfComponentType(Service.class));
    }

    /**
     * Returns all action categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("provider")
    public List<KeyLabelStore> getProviderCategories() {
        return sortByLabel(igorComponentRegistry.getCategoriesOfComponentType(Provider.class));
    }

    /**
     * Returns all action categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("action")
    public List<KeyLabelStore> getActionCategories() {
        return sortByLabel(igorComponentRegistry.getCategoriesOfComponentType(Action.class));
    }

    /**
     * Returns all trigger categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available trigger categories.
     */
    @GetMapping("trigger")
    public List<KeyLabelStore> getTriggerCategories() {
        return sortByLabel(igorComponentRegistry.getCategoriesOfComponentType(Trigger.class));
    }

}
