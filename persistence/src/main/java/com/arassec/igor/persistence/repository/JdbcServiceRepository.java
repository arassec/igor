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
        Optional<ServiceEntity> serviceEntityOptional = serviceDao.findById(service.getId());
        ServiceEntity serviceEntity;
        if (!serviceEntityOptional.isPresent()) {
            serviceEntity = new ServiceEntity();
            serviceEntity.setId(service.getId());
        } else {
            serviceEntity = serviceEntityOptional.get();
        }
        serviceEntity.setContent(serviceConverter.convert(service));
        serviceDao.save(serviceEntity);
    }

    @Override
    public void deleteById(String id) {
        serviceDao.deleteById(id);
    }

    @Override
    public Service findById(String id) {
        Optional<ServiceEntity> serviceEntityOptional = serviceDao.findById(id);
        if (serviceEntityOptional.isPresent()) {
            return serviceConverter.convert(serviceEntityOptional.get().getContent());
        }
        return null;
    }

    @Override
    public List<Service> findAll() {
        List<Service> result = new LinkedList<>();
        for (ServiceEntity serviceEntity : serviceDao.findAll()) {
            Service service = serviceConverter.convert(serviceEntity.getContent());
            if (service != null) {
                result.add(service);
            }
        }
        return result;
    }

}
