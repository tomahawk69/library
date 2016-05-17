package org.library.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.core.dao.DataStorage;
import org.library.core.dao.DataStorageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

@Component
@Scope("singleton")
class DataServiceFactoryImpl implements DataServiceFactory {
    private static final Logger LOGGER = LogManager.getLogger(DataServiceFactoryImpl.class);
    private final DataStorageFactory dataStorageFactory;
    private final String dataServiceType;
    private final String dataStorageType;

    @Autowired
    DataServiceFactoryImpl(DataStorageFactory dataStorageFactory,
                           @Value("${library.data.service.type}") String dataServiceType,
                           @Value("${library.data.storage.type}") String dataStorageType) {
        this.dataStorageFactory = dataStorageFactory;
        this.dataServiceType = dataServiceType;
        this.dataStorageType = dataStorageType;
    }

    @Override
    public DataService createDataService(Path path) {
        DataService dataService = null;
        DataServiceType serviceType = DataServiceType.getDataServiceTypeByName(dataServiceType);
        DataStorage dataStorage = dataStorageFactory.createDataStorage(dataStorageType);
        if (serviceType != null && dataStorage != null) {
            try {
                Class[] params = {DataStorage.class};
                dataService = serviceType.getClassName().getDeclaredConstructor(params).newInstance(dataStorage);
                dataService.setDatabasePath(path);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                LOGGER.error("Cannot instantiate data service type " + dataServiceType, e);
            }
        } else {
            LOGGER.warn("DataServiceType is null for " + dataServiceType);
        }
        return dataService;
    }
}
