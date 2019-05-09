package com.cryo;

import com.cryo.entities.FloorSize;
import com.cryo.rooms.Room;

public class Dungeon {

    private Room[][] rooms;

    private FloorSize size;

    public Dungeon(FloorSize size) {
        this.size = size;
        int dungeonSize = size == FloorSize.SMALL ? 9 : size == FloorSize.MEDIUM ? 30 : 50;
        rooms = new Room[dungeonSize][dungeonSize];
    }

    public void loop() {

    }
}
