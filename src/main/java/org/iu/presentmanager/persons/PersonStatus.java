package org.iu.presentmanager.persons;

import lombok.Getter;

@Getter
public enum PersonStatus {

    NONE("none"),
    IDEAS("ideas"),
    PLANNED("planned"),
    COMPLETED("completed");

    private final String value;

    PersonStatus(String value) {
        this.value = value;
    }

}
