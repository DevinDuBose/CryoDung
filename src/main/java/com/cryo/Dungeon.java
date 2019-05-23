package com.cryo;

import com.cryo.entities.Door;
import com.cryo.entities.FloorSize;
import com.cryo.entities.Keys;
import com.cryo.rooms.Room;
import com.cryo.rooms.impl.DefaultRoom;
import com.cryo.rooms.impl.StartingRoom;
import com.cryo.tools.DungeonCounter;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.script.Execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Dungeon {

    public static HashMap<String, Integer> keysMap;

    private Area startArea;

    private Room[][] rooms;

    private int x, y;

    private int startX, startY;

    private FloorSize size;
    private int maxSize;

    private boolean trainSkills;

    private ArrayList<Integer> keys;

    public Dungeon(FloorSize size, boolean trainSkills) {
        this.size = size;
        this.trainSkills = trainSkills;
        keys = new ArrayList<>();
        maxSize = size == FloorSize.SMALL ? 9 : size == FloorSize.MEDIUM ? 30 : 50;
        rooms = new Room[maxSize][maxSize];
        x = maxSize / 2;
        y = maxSize / 2;
        startX = x;
        startY = y;
        DungeonCounter.entered();
        updateCurrentRoom();
    }

    public void addKey(int key) {
        keys.add(key);
    }

    public void removeKey(int key) {
        keys.remove(new Integer(key));
    }

    public boolean hasKey(int key) {
        return keys.contains(key);
    }

    public void start() {
        rooms[x][y] = new StartingRoom(this, x, y);
    }

    public void loop() {
        if (rooms[x][y] != null) {
            boolean progress = !rooms[x][y].loop();
            if (progress) progress();
        }
    }

    public void progress() {
        Door door = getClosestDoor();
        if (door == null) return;
        if (x != door.getRoom().getX() || y != door.getRoom().getY()) {
            travelToRoom(door.getRoom().getX(), door.getRoom().getY());
            return;
        }
        enterRoom(door);
    }

    public void travelToRoom(int x, int y) {

    }

    public void enterRoom(Door door) {
        int[] direction = door.getDirection();
        int x = door.getRoom().getX() + direction[0];
        int y = door.getRoom().getY() + direction[1];
        Area area = getRoomArea(x, y);
        while (GameObjects.getLoadedWithin(area).size() == 0) {
            if (door.getSkillObject() != null && !door.isOpened())
                door.getSkillObject().click();
            else
                door.getObject().click();
            Execution.delay(1000, 3000);
            //TODO add special cases for things like 3 trap room, maze room, etc
            Execution.delayWhile(() -> Players.getLocal().isMoving() || Players.getLocal().getAnimationId() != -1, 3000, 15000);
        }
        door.open();
        while (!area.contains(Players.getLocal().getPosition())) {
            door.getObject().click();
            Execution.delay(1000, 3000);
        }
        if (rooms[x][y] == null)
            rooms[x][y] = new DefaultRoom(this, x, y);
        this.x = x;
        this.y = y;
    }

    public void updateCurrentRoom() {
        CryoDung.INSTANCE.getController().setCurrentRoom(x, y);
    }

    public Door getClosestDoor() {
        Room current = rooms[x][y];
        Door door;
        if ((door = current.getUnopenedDoor()) != null) {
            System.out.println("In current: " + door);
            return door;
        }
        List<Door> doors = new ArrayList<>();
        for (int x = 0; x < maxSize; x++) {
            for (int y = 0; y < maxSize; y++) {
                Room room = rooms[x][y];
                if (room == null || room.getUnopenedDoor() == null) continue;
                doors.add(room.getUnopenedDoor());
            }
        }
        doors.sort((s, s1) -> {
            int sD = s.getRoom().getDistanceTo(current);
            int sD1 = s1.getRoom().getDistanceTo(current);
            return sD - sD1;
        });
        if (doors.size() == 0) return null;
        Door closest = doors.get(0);
        System.out.println("Closest door: " + closest.getRoom().getX() + ", " + closest.getRoom().getY());
        return closest;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public Area getStartArea() {
        return startArea;
    }

    public void setStartArea(Area startArea) {
        this.startArea = startArea;
    }

    public void setTrainSkills(boolean trainSkills) {
        this.trainSkills = trainSkills;
    }

    public boolean shouldTrainSkills() {
        return trainSkills;
    }


    public static String[] KEY_NAMES = {"orange", "silver", "yellow", "green", "blue", "purple", "crimson", "gold"};

    public static void loadKeys() {
        keysMap = new HashMap<>();
        Arrays.stream(Keys.values()).forEach(k -> {
            for (int i = 0; i < k.getIds().length; i++)
                keysMap.put(KEY_NAMES[i] + " " + k.name().toLowerCase(), k.getIds()[i]);
        });
    }

    public static boolean isKey(int key) {
        return keysMap.containsValue(key);
    }

    public static int getKeyId(String name) {
        if (!keysMap.containsKey(name)) return -1;
        return keysMap.get(name);
    }

    public Room[][] getRooms() {
        return rooms;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        for (int x = 0; x < maxSize; x++)
            for (int y = 0; y < maxSize; y++)
                if (this.rooms[x][y] != null) rooms.add(this.rooms[x][y]);
        return rooms;
    }

    public Area getRoomArea(int x, int y) {
        int diffX = x - getStartX();
        int diffY = y - getStartY();
        Coordinate bottomLeft = getStartArea().getArea().getBottomLeft().derive(diffX * 16, diffY * 16);
        Coordinate topRight = bottomLeft.derive(15, 15);
        return new Area.Rectangular(bottomLeft, topRight);
    }
}
