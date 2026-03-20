package org.iu.presentmanager.occasions;

import lombok.Getter;

@Getter
public enum OccasionType {

    FIXED("fixed"),
    CUSTOM("custom");

    private final String type;

    OccasionType(String value) {
        this.type = value;
    }

}
