package org.iu.presentmanager.giftIdeas;

import lombok.Getter;

@Getter
public enum GiftSource {

    MANUAL("manual"),
    AI("ai"),
    SHARED("shared");

    private final String source;

    GiftSource(String source) {
        this.source = source;
    }
}
