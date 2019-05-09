package com.cryo.entities;

import java.util.Arrays;
import java.util.Optional;

public enum FloorSize {

    SMALL("Small", 6),
    MEDIUM("Medium", 4),
    LARGE("Large", 2);

    private String name;
    private int componentId;

    FloorSize(String name, int componentId) {
        this.name = name;
        this.componentId = componentId;
    }

    public int getComponentId() {
        return componentId;
    }

    public static FloorSize getSize(String name) {
        Optional<FloorSize> optional = Arrays.stream(FloorSize.values()).filter(s -> s.name.equals(name)).findFirst();
        if (!optional.isPresent()) return null;
        return optional.get();
    }

    public static FloorSize getSize(int componentId) {
        Optional<FloorSize> optional = Arrays.stream(FloorSize.values()).filter(s -> s.componentId == componentId).findFirst();
        if (!optional.isPresent()) return null;
        return optional.get();
    }

    public boolean equals(FloorSize size) {
        if (size == null) return false;
        return name == size.name;
    }
}
