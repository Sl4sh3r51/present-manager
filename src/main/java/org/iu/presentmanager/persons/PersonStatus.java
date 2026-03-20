package org.iu.presentmanager.persons;

import lombok.Getter;

@Getter
public enum PersonStatus {

    NONE("none"),
    IDEAS("ideas"),
    PLANNED("planned"),
    COMPLETED("completed");

    private final String status;

    PersonStatus(String value) {
        this.status = value;
    }

}
