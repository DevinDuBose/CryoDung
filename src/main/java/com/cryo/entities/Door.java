package com.cryo.entities;

import com.cryo.CryoDung;
import com.cryo.rooms.Room;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.util.calculations.Distance;

import java.util.Optional;

public class Door {

    private GameObject object;
    private GameObject skillObject;

    private boolean opened;

    private int skillRequired = -1;
    private int levelRequired;

    private int keyRequired;

    private boolean guardianDoor;

    private Room room;

    public Door(GameObject object, Room room) {
        this.room = room;
        this.object = object;
    }

    public Door(GameObject object, Room room, int skillRequired, GameObject skillObject) {
        this.room = room;
        this.object = object;
        this.skillObject = skillObject;
        this.skillRequired = skillRequired;
    }

    public Door(GameObject object, Room room, int keyRequired) {
        this.room = room;
        this.object = object;
        this.keyRequired = keyRequired;
    }

    public Door(GameObject object, Room room, boolean guardianDoor) {
        this.room = room;
        this.object = object;
        this.guardianDoor = guardianDoor;
    }

    public int[] getDirection() {
        Area.Rectangular rectangular = (Area.Rectangular) room.getRoomArea();
        int topY = rectangular.getTopRight().getY();
        int bottomY = rectangular.getBottomLeft().getY();
        int topX = rectangular.getTopRight().getX();
        int bottomX = rectangular.getBottomLeft().getX();
        int y = object.getPosition().getY();
        int x = object.getPosition().getX();
        if (Math.abs(topY - y) < 2) return new int[]{0, 1};
        if (Math.abs(bottomY - y) < 2) return new int[]{0, -1};
        if (Math.abs(topX - x) < 2) return new int[]{1, 0};
        if (Math.abs(bottomX - x) < 2) return new int[]{-1, 0};
        return null;
    }

    public boolean isGuardianDoor() {
        return guardianDoor;
    }

    public boolean isSkillDoor() {
        return skillRequired != -1;
    }

    public int getSkillRequired() {
        return skillRequired;
    }

    public int getLevelRequired() {
        return levelRequired;
    }

    public int getKeyRequired() {
        return keyRequired;
    }

    public boolean isKeyDoor() {
        return keyRequired != 0;
    }

    public boolean isOpened() {
        return opened;
    }

    public void open() {
        this.opened = true;
    }

    public Room getRoom() {
        return room;
    }

    public GameObject getObject() {
        return object;
    }

    public GameObject getSkillObject() {
        return skillObject;
    }

    public boolean canBeOpened() {
        if (opened) return false;
        if (guardianDoor) return true; //TODO - check for npcs
        if (isKeyDoor()) return CryoDung.INSTANCE.getDungeon().hasKey(keyRequired);
        return true;
    }

    public boolean isRegularDoor() {
        return !isGuardianDoor() && !isSkillDoor() && !isKeyDoor();
    }

    public static int getSkillId(GameObject door) {
        Object[] data = getSkillDoor(door);
        if (data == null) return -1;
        return (int) data[1];
    }

    public static Object[] getSkillDoor(GameObject door) {
        SkillDoors skill = SkillDoors.getDoor(door.getDefinition().getName());
        if (skill != null)
            return new Object[]{door, skill.getSkillId()};
        Optional<GameObject> optional = GameObjects.getLoaded(o -> Distance.between(o, door) == 1)
                .stream()
                .filter(d -> SkillDoors.getDoor(d.getDefinition().getName()) != null)
                .findFirst();
        if (!optional.isPresent()) return null;
        skill = SkillDoors.getDoor(optional.get().getDefinition().getName());
        if (skill == null) return null;
        return new Object[]{optional.get(), skill.getSkillId()};
    }

}
