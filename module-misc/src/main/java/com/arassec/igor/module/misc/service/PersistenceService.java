package com.arassec.igor.module.misc.service;

import com.arassec.igor.core.model.service.Service;

import java.util.List;

/**
 * Service for persisting data.
 */
public interface PersistenceService extends Service {

    void save(String value);

    List<String> loadAll();

}
