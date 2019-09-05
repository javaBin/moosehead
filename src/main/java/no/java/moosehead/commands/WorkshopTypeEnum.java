package no.java.moosehead.commands;

import java.util.Optional;

public enum WorkshopTypeEnum {
    KIDSAKODER_WORKSHOP,
    NORMAL_WORKSHOP,
    BEER_WORKSHOP;

    public static Optional<WorkshopTypeEnum> fromValue(String value) {
        for (WorkshopTypeEnum workshopTypeEnum : values()) {
            if (workshopTypeEnum.toString().equals(value)) {
                return Optional.of(workshopTypeEnum);
            }
        }
        return Optional.empty();
    }
}