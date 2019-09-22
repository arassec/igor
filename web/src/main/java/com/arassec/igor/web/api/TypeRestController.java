package com.arassec.igor.web.api;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.util.KeyLabelStore;
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
public class TypeRestController extends BaseRestController {

    /**
     * The igor component registry.
     */
    private final IgorComponentRegistry igorComponentRegistry;

    /**
     * Returns all service types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The service category to use.
     *
     * @return Set of service types.
     */
    @GetMapping("service/{category}")
    public List<KeyLabelStore> getServiceTypes(@PathVariable("category") String category) {
        return sortByLabel(igorComponentRegistry.getTypesOfCategory(category));
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     *
     * @return Set of action types.
     */
    @GetMapping("action/{category}")
    public List<KeyLabelStore> getActionTypes(@PathVariable("category") String category) {
        return sortByLabel(igorComponentRegistry.getTypesOfCategory(category));
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     *
     * @return Set of action types.
     */
    @GetMapping("provider/{category}")
    public List<KeyLabelStore> getProviderTypes(@PathVariable("category") String category) {
        return sortByLabel(igorComponentRegistry.getTypesOfCategory(category));
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     *
     * @return Set of action types.
     */
    @GetMapping("trigger/{category}")
    public List<KeyLabelStore> getTriggerTypes(@PathVariable("category") String category) {
        return sortByLabel(igorComponentRegistry.getTypesOfCategory(category));
    }

}
