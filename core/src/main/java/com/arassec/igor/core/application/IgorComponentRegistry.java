package com.arassec.igor.core.application;

import com.arassec.igor.core.model.IgorCategory;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.KeyLabelStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.*;

/**
 * Registry for igor components. Keeps track of the component's types and categories.
 */
@Component
@Slf4j
public class IgorComponentRegistry {

    /**
     * Fallback category if none is configured for a component.
     */
    private static final KeyLabelStore FALLBACK_CATEGORY = new KeyLabelStore(IgorComponentRegistry.class.getName(), "Unknown");

    /**
     * Contains the categories of a certain component type (e.g. Action or Service etc.).
     */
    private Map<Class, Set<KeyLabelStore>> categoriesByComponentType = new HashMap<>();

    /**
     * Contains all available component types in a certain category.
     */
    private Map<String, Set<KeyLabelStore>> typesByCategoryKey = new HashMap<>();

    /**
     * Contains the category of a specific igor component (e.g. CronTrigger).
     */
    private Map<Class, KeyLabelStore> categoryByComponentInstance = new HashMap<>();

    /**
     * Contains the type of a specific igor component (e.g. CronTrigger).
     */
    private Map<Class, KeyLabelStore> typeByComponentInstance = new HashMap<>();

    /**
     * Creates a new registry instance and loads all categories and types.
     */
    public IgorComponentRegistry() {
        initializeComponent(Action.class);
        initializeComponent(Service.class);
        initializeComponent(Trigger.class);
        initializeComponent(Provider.class);
    }

    /**
     * Returns the category of a specific component, e.g. CronTrigger.
     *
     * @param componentInstanceClass The class of the specific igor component to get the category of.
     *
     * @return The category of the component.
     */
    public KeyLabelStore getCategoryOfComponentInstance(Class componentInstanceClass) {
        if (categoryByComponentInstance.containsKey(componentInstanceClass)) {
            return categoryByComponentInstance.get(componentInstanceClass);
        }
        throw new IllegalArgumentException("No category available for igor component: " + componentInstanceClass.getName());
    }

    /**
     * Returns the type of a specific component, e.g. CronTrigger.
     *
     * @param componentInstanceClass The class of the specific component to get the type of.
     *
     * @return The type of the component.
     */
    public KeyLabelStore getTypeOfComponentInstance(Class componentInstanceClass) {
        if (typeByComponentInstance.containsKey(componentInstanceClass)) {
            return typeByComponentInstance.get(componentInstanceClass);
        }
        throw new IllegalArgumentException("No type available for igor component: " + componentInstanceClass.getName());
    }

    /**
     * Returns the categories of a component type, e.g. all categories of {@link Action} components.
     *
     * @param componentType The type of component to get the categories of.
     *
     * @return Set of categories of the specified component type or an empty set, if none are available.
     */
    public Set<KeyLabelStore> getCategoriesOfComponentType(Class componentType) {
        if (categoriesByComponentType.containsKey(componentType)) {
            return categoriesByComponentType.get(componentType);
        }
        return Set.of();
    }

    /**
     * Returns the types of a specific category of components, e.g. all types of {@link Trigger}s.
     *
     * @param categoryKey The category's key.
     *
     * @return Set of types or an empty set, if none are available.
     */
    public Set<KeyLabelStore> getTypesOfCategory(String categoryKey) {
        if (typesByCategoryKey.containsKey(categoryKey)) {
            return typesByCategoryKey.get(categoryKey);
        }
        return Set.of();
    }

    /**
     * Initializes categories and types of a specific igor component, e.g. {@link Service}.
     *
     * @param componentType The type of the component to initialize.
     */
    private void initializeComponent(Class<?> componentType) {
        categoriesByComponentType.put(componentType, new HashSet<>());

        ServiceLoader serviceLoader = ServiceLoader.load(componentType);

        for (Object o : serviceLoader) {
            Class<?> componentInstanceClass = o.getClass();

            // Handling type information
            IgorComponent igorComponentAnnotation = componentInstanceClass.getAnnotation(IgorComponent.class);
            if (igorComponentAnnotation == null) {
                log.warn("{} is configured as igor component but lacks IgorComponent annotation!", componentInstanceClass.getName());
                return;
            }
            KeyLabelStore type = new KeyLabelStore(componentInstanceClass.getName(), igorComponentAnnotation.value());
            typeByComponentInstance.put(componentInstanceClass, type);

            // Determine the category
            KeyLabelStore category = FALLBACK_CATEGORY;
            for (Class<?> anInterface : ClassUtils.getAllInterfacesForClass(componentInstanceClass)) {
                if (anInterface.isAnnotationPresent(IgorCategory.class)) {
                    IgorCategory igorCategoryAnnotation = anInterface.getAnnotation(IgorCategory.class);
                    category = new KeyLabelStore(anInterface.getName(), igorCategoryAnnotation.value());
                }
            }

            Class<?> categoryDeclaringClass = AnnotationUtils.findAnnotationDeclaringClass(IgorCategory.class,
                    componentInstanceClass);
            if (FALLBACK_CATEGORY.equals(category) && categoryDeclaringClass != null) {
                IgorCategory igorCategoryAnnotation = categoryDeclaringClass.getAnnotation(IgorCategory.class);
                category = new KeyLabelStore(categoryDeclaringClass.getName(), igorCategoryAnnotation.value());
            }

            if (FALLBACK_CATEGORY.equals(category)) {
                log.warn("{} is configured as igor component but lacks IgorCategory annotation!", componentInstanceClass.getName());
            }

            categoriesByComponentType.get(componentType).add(category);
            categoryByComponentInstance.put(componentInstanceClass, category);

            if (!typesByCategoryKey.containsKey(category.getKey())) {
                typesByCategoryKey.put(category.getKey(), new HashSet<>());
            }
            typesByCategoryKey.get(category.getKey()).add(type);
        }
    }

}
