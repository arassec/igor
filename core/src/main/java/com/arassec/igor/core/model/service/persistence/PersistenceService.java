package com.arassec.igor.core.model.service.persistence;

import com.arassec.igor.core.model.IgorServiceCategory;
import com.arassec.igor.core.model.service.Service;

import java.util.List;

/**
 * Service for persisting data.
 */
@IgorServiceCategory(label = "Persistence")
public interface PersistenceService extends Service {

    void save(String jobId, String taskName, String value);

    List<String> loadAll(String jobId, String taskName);

    boolean isPersisted(String jobId, String taskName, String value);

    void cleanup(String jobId, String taskName, int numEntriesToKeep);

}
