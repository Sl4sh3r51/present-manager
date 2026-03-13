package org.iu.presentmanager.gifts;

import lombok.Getter;

@Getter
public enum GiftStatus {

    PLANNED("planned"),
    BOUGHT("bought"),
    GIFTED("gifted");

    private final String status;


    GiftStatus(String status) {
        this.status = status;
    }
}
