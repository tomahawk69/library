package org.library.core.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class DataStorageFactory {
    private static final Logger LOGGER = LogManager.getLogger(DataStorageFactory.class);

    public DataStorage createDataStorage(String name) {
        DataStorage dataStorage = null;
        DataStorageType dataStorageType = DataStorageType.getDataStorageTypeByName(name);
        if (dataStorageType != null) {
            try {
                dataStorage = dataStorageType.getDataStorageClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("Cannot instantiate storage type " + name, e);
            }
        } else {
            LOGGER.warn("StorageType is null for " + name);
        }
        return dataStorage;
    }

    enum DataStorageType {
        SQLITE("sqlite", DataStorageSQLite.class);

        private final Class<? extends DataStorage> dataStorageClass;
        private final String name;

        DataStorageType(String name, Class<? extends DataStorage> dataStorageClass) {
            this.name = name;
            this.dataStorageClass = dataStorageClass;
        }

        public Class<? extends DataStorage> getDataStorageClass() {
            return dataStorageClass;
        }

        public String getName() {
            return name;
        }

        public static DataStorageType getDataStorageTypeByName(String name) {
            for (DataStorageType dataStorageType : values()) {
                if (dataStorageType.getName().equalsIgnoreCase(name)) {
                    return dataStorageType;
                }
            }
            return null;
        }

        ;
    }

}
