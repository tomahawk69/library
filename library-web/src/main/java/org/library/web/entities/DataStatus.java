package org.library.web.entities;

public enum DataStatus {
    INITIALIZING("Initializing", false), IDLE("Idle", true), REFRESH("Refresh", false), CANCELLING("Cancelling", false);
    private final String name;
    private final Boolean isRefreshAllowed;

   DataStatus(String name, Boolean isRefreshAllowed) {
        this.name = name;
        this.isRefreshAllowed = isRefreshAllowed;
    }

    public String getName() {
        return name;
    }

    public Boolean getRefreshAllowed() {
        return isRefreshAllowed;
    }
}