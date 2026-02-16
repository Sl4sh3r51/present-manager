package org.iu.presentmanager.persons;

public enum PersonStatus {

    NONE("none"),
    IDEAS("ideas"),
    PLANNED("planned"),
    COMPLETED("completed");

    private final String value;

    PersonStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
