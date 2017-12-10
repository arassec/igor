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
        ServiceEntity serviceEntity = serviceDao.findOne(service.getId());
        if (serviceEntity == null) {
            serviceEntity = new ServiceEntity();
            serviceEntity.setId(service.getId());
        }
        serviceEntity.setContent(serviceConverter.convert(service));
        serviceDao.save(serviceEntity);
    }

    @Override
    public Service findById(String id) {
        ServiceEntity serviceEntity = serviceDao.findOne(id);
        return serviceConverter.convert(serviceEntity.getContent());
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
