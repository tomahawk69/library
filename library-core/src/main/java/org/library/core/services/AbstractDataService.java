package org.library.core.services;

import org.library.entities.FileUpdateOperation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDataService  implements DataService {
    protected final List<FileUpdateOperation> queue = new ArrayList<>();
    protected Path databasePath;

    public int getQueueSize() {
        return queue.size();
    }

    void addOperationToQueue(FileUpdateOperation modifyOperation) {
        synchronized (queue) {
            queue.add(modifyOperation);
        }
    }

    @Override
    public void setDatabasePath(Path libraryPath) {
        this.databasePath = libraryPath;
    }

}
