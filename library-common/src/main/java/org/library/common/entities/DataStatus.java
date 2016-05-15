package org.library.common.entities;

public enum DataStatus {
    INITIALIZING("Initializing", false, false), IDLE("Idle", true, false), REFRESH("Refresh", false, true), CANCELLING("Cancelling", false, false), NO_LIBRARY_SELECTED("No library selected", false, false);
    private final String name;
    private final boolean isRefreshAllowed;
    private final boolean isOperation;

    DataStatus(String name, Boolean isRefreshAllowed, boolean isOperation) {
        this.name = name;
        this.isRefreshAllowed = isRefreshAllowed;
        this.isOperation = isOperation;
    }

    public String getName() {
        return name;
    }

    public boolean getRefreshAllowed() {
        return isRefreshAllowed;
    }

    public boolean getOperation() {
        return isOperation;
    }
}