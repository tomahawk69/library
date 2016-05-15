package org.library.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.core.dao.DataStorage;
import org.library.core.dao.DataStorageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

@Component
@Scope("singleton")
class DataServiceFactoryImpl implements DataServiceFactory {
    private static final Logger LOGGER = LogManager.getLogger(DataServiceFactoryImpl.class);
    private final DataStorageFactory dataStorageFactory;

    @Autowired
    DataServiceFactoryImpl(DataStorageFactory dataStorageFactory) {
        this.dataStorageFactory = dataStorageFactory;
    }


    @Override
    public DataService createDataService(String serviceName, String storageName, Path path) {
        DataService dataService = null;
        DataServiceType serviceType = DataServiceType.getDataServiceTypeByName(serviceName);
        DataStorage dataStorage = dataStorageFactory.createDataStorage(storageName);
        if (serviceType != null && dataStorage != null) {
            try {
                Class[] params = {DataStorage.class};
                dataService = serviceType.getClassName().getDeclaredConstructor(params).newInstance(dataStorage);
                dataService.setDatabasePath(path);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                LOGGER.error("Cannot instantiate data service type " + serviceName, e);
            }
        } else {
            LOGGER.warn("DataServiceType is null for " + serviceName);
        }
        return dataService;
    }
}
