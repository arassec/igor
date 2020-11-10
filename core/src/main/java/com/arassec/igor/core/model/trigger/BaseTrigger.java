package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.BaseIgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.validation.ValidJsonObject;
import lombok.Setter;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Baseclass for Triggers.
 */
public abstract class BaseTrigger extends BaseIgorComponent implements Trigger {

    /**
     * Contains user configured event data that is added to the initial data item.
     */
    @ValidJsonObject
    @Setter
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE, advanced = true, value = 101)
    private String metaData;

    /**
     * Contains user configured event data that is added to the initial data item.
     */
    @ValidJsonObject
    @Setter
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE, advanced = true, value = 102)
    private String data;

    /**
     * Creates a new component instance.
     *
     * @param categoryId The category ID.
     * @param typeId     The type ID.
     */
    protected BaseTrigger(String categoryId, String typeId) {
        super(categoryId, typeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getMetaData() {
        return convertJsonString(metaData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getData() {
        return convertJsonString(data);
    }

    /**
     * Converts a JSON-Object-String into a Map.
     *
     * @param input The input to convert.
     *
     * @return A Map containing the JSON data.
     */
    protected Map<String, Object> convertJsonString(String input) {
        if (StringUtils.hasText(input)) {
            Object parsed = JSONValue.parse(input);
            if (parsed instanceof JSONObject) {
                return ((JSONObject) parsed);
            }
        }
        return new HashMap<>();
    }

}
