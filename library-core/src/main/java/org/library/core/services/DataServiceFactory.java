package org.library.core.services;

import java.nio.file.Path;

public interface DataServiceFactory {

    DataService createDataService(String serviceName, String storageName, Path path);

    enum DataServiceType {
        DB("db", DataServiceDBImpl.class);

        private final Class<? extends AbstractDataService> className;
        private final String name;

        DataServiceType(String name, Class<? extends AbstractDataService> dataServiceClass) {
            this.className = dataServiceClass;
            this.name = name;
        }

        public Class<? extends AbstractDataService> getClassName() {
            return className;
        }

        public static DataServiceType getDataServiceTypeByName(String name) {
            for (DataServiceType dataServiceType : values()) {
                if (dataServiceType.name.equalsIgnoreCase(name)) {
                    return dataServiceType;
                }
            }
            return null;
        }
    }

}
