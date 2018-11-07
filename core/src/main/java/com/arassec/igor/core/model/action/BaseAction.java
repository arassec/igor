package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.dryrun.DryRunActionResult;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.IgorParam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Base action that implements common functionality of an action. Specific actions should be derived from this class.
 * <p>
 * Created by Andreas Sensen on 01.04.2017.
 */
public abstract class BaseAction implements Action {

    /**
     * Contains the number which denotes that the number of threads this action should execute with is undefined.
     */
    public static final int NUM_THREADS_UNDEFINED = -1;

    /**
     * Key into the {@link IgorData} that identifies the property to process.
     */
    @IgorParam
    protected String dataKey = "data";

    /**
     * Defines the number of threads the action should be processed with.
     */
    @IgorParam
    protected int numThreads = NUM_THREADS_UNDEFINED;

    /**
     * Contains the provided data keys.
     */
    private Set<String> providedDataKeys = new HashSet<>();

    /**
     * Contains the required data keys.
     */
    private Set<String> requiredDataKeys = new HashSet<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        // Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dryRun(IgorData data) {
        return process(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete(String jobId, String taskName) {
        // Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumThreads() {
        return numThreads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> provides() {
        return providedDataKeys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> requires() {
        return requiredDataKeys;
    }

    /**
     * Adds all supplied keys to the set of provided data keys.
     *
     * @param keys The keys to add.
     */
    protected void addProvidedDataKeys(String... keys) {
        providedDataKeys.addAll(Arrays.asList(keys));
    }

    /**
     * Adds all supplied keys to the set of required data keys.
     *
     * @param keys The keys to add.
     */
    protected void addRequiredDataKeys(String... keys) {
        requiredDataKeys.addAll(Arrays.asList(keys));
    }

    /**
     * Returns whether the supplied {@link IgorData} is valid or not.
     *
     * @param data The data to validate.
     * @return {@code true} if the data is valid for processing, {@code false} otherwise.
     */
    protected boolean isValid(IgorData data) {
        if (!data.containsKey(dataKey) || !(data.get(dataKey) instanceof String)) {
            return false;
        }
        return true;
    }

}
