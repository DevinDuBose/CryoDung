package com.cryo.entities;

public class Floor {

    private final int id;
    private boolean completed;
    private boolean open;

    public Floor(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isOpen() {
        return open;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
