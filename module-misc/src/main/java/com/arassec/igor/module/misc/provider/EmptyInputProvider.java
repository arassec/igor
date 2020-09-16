package com.arassec.igor.module.misc.provider;


import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Positive;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a configurable amount of empty input data to jobs.
 */
@Getter
@Setter
@IgorComponent
public class EmptyInputProvider extends BaseUtilProvider {

    /**
     * The amount of data items to generate.
     */
    @Positive
    @IgorParam(defaultValue = "1")
    private int amount;

    /**
     * The number of items already generated.
     */
    private int current = 0;

    /**
     * Creates a new component instance.
     */
    public EmptyInputProvider() {
        super("empty-input-provider");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return (current < amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> next() {
        current++;
        return new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        current = 0;
    }

}
