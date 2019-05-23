package com.cryo.rooms;

import com.cryo.CryoDung;
import com.cryo.Dungeon;
import com.cryo.entities.Door;
import com.cryo.entities.Foods;
import com.cryo.entities.Node;
import com.cryo.path.AStar;
import com.cryo.puzzles.Puzzle;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.GroundItem;
import com.runemate.game.api.hybrid.entities.Npc;
import com.runemate.game.api.hybrid.input.Mouse;
import com.runemate.game.api.hybrid.local.hud.interfaces.Health;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.GroundItems;
import com.runemate.game.api.hybrid.region.Npcs;
import com.runemate.game.api.script.Execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class Room {

    protected int x, y;
    protected Dungeon dungeon;

    protected Door[] doors;

    private List<Npc> npcs;

    private Puzzle puzzle;

    public Room(Dungeon dungeon, int x, int y) {
        this(dungeon, x, y, true);
    }

    public Room(Dungeon dungeon, int x, int y, boolean loadExtras) {
        this.dungeon = dungeon;
        this.x = x;
        this.y = y;
        npcs = new ArrayList<>();
        if (loadExtras) {
            loadNpcs();
            loadDoors();
            loadPuzzle();
        }
    }

    public boolean loop() {
        if (checkForMonsters()) return true;
        if (checkForKeys()) return true;
        if (checkForFood()) return true;
        return false;
    }

    public boolean checkForKeys() {
        GroundItem item = GroundItems.getLoadedWithin(getRoomArea(), i -> Dungeon.isKey(i.getId())).first();
        if (item == null) return false;
        Mouse.click(item, Mouse.Button.RIGHT);
        Execution.delay(300, 500);
        item.interact("Take");
        Execution.delay(2000, 5000);
        return true;
    }

    public boolean checkForFood() {
        int total = Foods.getTotalHealing();
        int totalhp = Health.getMaximum();
        GroundItem item;
        while (total < totalhp && (item = Foods.getPriorityItem(getRoomArea())) != null) {
            item.interact("Take");
            Execution.delay(1000, 3000);
        }
        return false;
    }

    public boolean checkForMonsters() {
        return false;
    }

    public Door getUnopenedDoor() {
        System.out.println(doors.length);
        Optional<Door> door = Arrays.stream(doors).filter(d -> d.canBeOpened()).findFirst();
        if (!door.isPresent()) return null;
        return door.get();
    }

    public boolean isDoor(GameObject object) {
        String name = object.getDefinition().getName();
        if (name != null && name.toLowerCase().contains("door")) return true;
        if (object.getDefinition().getActions().contains("Unlock")) return false;
        return object.getId() == 50342 || object.getId() == 50346;
    }

    public void loadDoors() {
        List<GameObject> results = GameObjects.getLoadedWithin(getRoomArea())
                .stream()
                .filter(d -> isDoor(d))
                .collect(Collectors.toList());
        GameObject[] doors = results.toArray(new GameObject[results.size()]);
        this.doors = new Door[doors.length];
        for (int i = 0; i < doors.length; i++) {
            GameObject door = doors[i];
            String name = door.getDefinition().getName().toLowerCase();
            if (door.getId() == 50342) {
                Object[] doorResults = Door.getSkillDoor(door);
                if (doorResults != null)
                    this.doors[i] = new Door(door, this, (int) doorResults[1], (GameObject) doorResults[0]);
                else
                    this.doors[i] = new Door(door, this);
            } else if (door.getId() == 50346)
                this.doors[i] = new Door(door, this, true);
            else {
                String keyName = name.replace(" door", "");
                int skillId = Door.getSkillId(door);
                if (skillId != -1) {
                    this.doors[i] = new Door(door, this, skillId, null);
                    continue;
                }
                int keyId = Dungeon.getKeyId(keyName);
                if (keyId == -1) {
                    CryoDung.INSTANCE.stop("Missing skill door: " + name);
                    return;
                }
                this.doors[i] = new Door(door, this, keyId);
            }
        }
    }

    public Room getNeighbour(int[] offsets) {
        return getNeighbour(offsets[0], offsets[1]);
    }

    public Room getNeighbour(int xOffset, int yOffset) {
        int x = this.x + xOffset;
        int y = this.y + yOffset;
        if (x < 0 || x > dungeon.getRooms().length) return null;
        if (y < 0 || y > dungeon.getRooms()[x].length) return null;
        return dungeon.getRooms()[x][y];
    }

    public void loadNpcs() {
        this.npcs = Npcs.newQuery().within(getRoomArea()).actions("Attack").results().asList();
    }

    public void loadPuzzle() {

    }

    public int getDistanceTo(Room room) {
        Node initial = new Node(x, y);
        Node finalNode = new Node(room.x, room.y);
        int rows = dungeon.getMaxSize();
        AStar aStar = new AStar(rows, rows, initial, finalNode);
        int[][] blocks = dungeon.getAllRooms().stream().map(r -> new int[]{r.x, r.y}).toArray(int[][]::new);
        aStar.setBlocks(blocks);
        List<Node> path = aStar.findPath();
        return path.size();
    }

    public Area getRoomArea() {
        return dungeon.getRoomArea(x, y);
    }

    public Door[] getDoors() {
        return doors;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    //returns true to block other loop from running
    //check for monsters
    //check if monsters need to be killed
    //reasons for killing:
    //we have a guardian door
    //room has a puzzle
    //maybe if we're low on food?
    //there are skilling areas we'd like to use (option in bot settings?)
}
