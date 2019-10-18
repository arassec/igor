package com.arassec.igor.module.misc.provider;


import com.arassec.igor.core.model.IgorParam;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.PositiveOrZero;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a configurable amount of empty input data to tasks.
 */
@Component
@Scope("prototype")
@Data
public class EmptyInputProvider extends BaseUtilProvider {

    /**
     * The amount of data items to generate.
     */
    @PositiveOrZero
    @IgorParam
    private int amount = 1;

    /**
     * The number of items already generated.
     */
    private int current = 0;

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
    public String getTypeId() {
        return "5e1969ae-fd31-47f5-9194-1b58546d129c";
    }
}
