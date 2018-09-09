package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.persistence.converter.ServiceConverter;
import com.arassec.igor.persistence.dao.ServiceDao;
import com.arassec.igor.persistence.entity.ServiceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Andreas Sensen on 01.05.2017.
 */
@Component
@Transactional
public class JdbcServiceRepository implements ServiceRepository {

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    private ServiceConverter serviceConverter;

    @Override
    public void upsert(Service service) {
        ServiceEntity serviceEntity;
        if (service.getId() == null) {
            serviceEntity = new ServiceEntity();
        } else {
            Optional<ServiceEntity> serviceEntityOptional = serviceDao.findById(service.getId());
            if (!serviceEntityOptional.isPresent()) {
                throw new IllegalStateException("No service with ID " + service.getId() + " available!");
            }
            serviceEntity = serviceEntityOptional.get();
        }
        serviceEntity.setName(service.getName());
        serviceEntity.setContent(serviceConverter.convert(service));
        serviceDao.save(serviceEntity);
    }

    @Override
    public Service findById(Long id) {
        Optional<ServiceEntity> serviceEntityOptional = serviceDao.findById(id);
        if (serviceEntityOptional.isPresent()) {
            Service service = serviceConverter.convert(serviceEntityOptional.get().getContent());
            service.setId(id);
            return service;
        }
        return null;
    }

    @Override
    public List<Service> findAll() {
        List<Service> result = new LinkedList<>();
        for (ServiceEntity serviceEntity : serviceDao.findAll()) {
            Service service = serviceConverter.convert(serviceEntity.getContent());
            service.setId(serviceEntity.getId());
            if (service != null) {
                result.add(service);
            }
        }
        return result;
    }

    @Override
    public void deleteById(Long id) {
        serviceDao.deleteById(id);
    }

}
