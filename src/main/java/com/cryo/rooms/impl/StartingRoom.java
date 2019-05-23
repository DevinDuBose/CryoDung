package com.cryo.rooms.impl;

import com.cryo.CryoDung;
import com.cryo.Dungeon;
import com.cryo.rooms.Room;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.Npc;
import com.runemate.game.api.hybrid.input.Mouse;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceContainers;
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.Npcs;
import com.runemate.game.api.hybrid.util.calculations.Random;
import com.runemate.game.api.script.Execution;

import java.util.LinkedHashMap;
import java.util.Optional;

public class StartingRoom extends Room {

    public static int FEATHERS = 17796, ANTIPOISON = 17566;

    private static int[] RANDOM_COMPONENTS = {40, 54, 68, 82, 96,
            159, 173, 187, 201, 215,
            285, 299, 313, 327, 341};

    private boolean purchasedItems;

    public StartingRoom(Dungeon dungeon, int x, int y) {
        super(dungeon, x, y, false);
        dungeon.setStartArea(getStartArea());
        loadNpcs();
        loadDoors();
    }

    @Override
    public boolean loop() {
        if (checkForKeys()) return true;
//        int complexity = CryoDung.INSTANCE.getComplexity();
//        boolean trainSkills = dungeon.shouldTrainSkills();
//        LinkedHashMap<Integer, Integer> items = new LinkedHashMap<>();
//        if (!purchasedItems) {
//            System.out.println("Buying items.");
//            if (complexity == 1) {
//                purchasedItems = true;
//                return true;
//            }
//            System.out.println(complexity);
//            if (complexity == 6)
//                items.put(ANTIPOISON, 2);
//            if (complexity >= 2 && trainSkills)
//                items.put(FEATHERS, 50);
//            buyFromSmuggler(items);
//            purchasedItems = true;
//        }
//        if (checkForFood()) return true;
//        System.out.println("progressing");
        //progress()
        //room only progresses once loop returns false?
        //how do i handle progressing through the dungeon?
        return false;
    }

    private void buyFromSmuggler(LinkedHashMap<Integer, Integer> items) {
        boolean scrolled = false;
        for (int id : items.keySet()) {
            int amount = items.get(id);
            System.out.println(id + ": " + amount);
            InterfaceComponent component = Interfaces.getAt(956, 0);
            while (component == null || !component.isVisible()) {
                Npc npc = Npcs.newQuery().ids(11226).within(getRoomArea()).results().first();
                if (npc == null) {
                    CryoDung.INSTANCE.stop("Unable to find smuggler in starting room.");
                    return;
                }
                component = Interfaces.getAt(956, 0);
                npc.interact("Trade");
                Execution.delay(2000, 3000);
            }
            Optional<InterfaceComponent> optional = InterfaceContainers.getAt(956)
                    .getComponent(2)
                    .getChildren(i -> i.getContainedItemId() == id)
                    .stream()
                    .findFirst();
            if (!optional.isPresent()) {
                System.out.println("No item found");
                continue;
            }
            InterfaceComponent item = optional.get();
            InterfaceComponent viewport = Interfaces.getAt(956, 2);
            if (!scrolled) {
                int randomId = RANDOM_COMPONENTS[Random.nextInt(RANDOM_COMPONENTS.length)];
                InterfaceComponent rItem = Interfaces.getAt(956, 2, randomId);
                while (!rItem.hover()) Execution.delay(1000, 3000);
                for (int i = 0; i < Random.nextInt(10, 20); i++)
                    Mouse.scroll(true);
                scrolled = true;
            }
            Execution.delay(3000, 5000);
            int bought = 0;
            int remaining = amount;
            while (bought < amount) {
                int buying = 0;
                if (remaining >= 250)
                    buying = 250;
                else if (remaining >= 50)
                    buying = 50;
                else if (remaining >= 10)
                    buying = 10;
                else if (remaining >= 5)
                    buying = 5;
                else if (remaining >= 1)
                    buying = 1;
                while (!item.interact("Buy " + buying))
                    Execution.delay(1000, 3000);
                bought += buying;
                remaining -= buying;
                System.out.println("Buying: " + buying);
                //TODO - get price/max amount i can buy
            }
        }
        InterfaceComponent close = Interfaces.getAt(956, 9);
        if (close != null && close.isVisible())
            while (!close.click())
                Execution.delay(1000, 3000);
    }

    public Area getStartArea() {
        GameObject rift = GameObjects.newQuery().ids(2342).results().first();
        GameObject table = GameObjects.newQuery().ids(51577).results().first();
        if (rift == null || table == null) {
            CryoDung.INSTANCE.stop("Unable to determine starting area.");
            return null;
        }
        int rX = rift.getPosition().getX();
        int rY = rift.getPosition().getY();
        int tX = table.getPosition().getX();
        int tY = table.getPosition().getY();
        Coordinate bottomLeft = null;
        Coordinate topRight = null;
        if (rX < tX && rY < tY) {
            bottomLeft = rift.getPosition().derive(-2, -2);
            topRight = bottomLeft.derive(15, 15);
        } else if (rX < tX && rY > tY) {
            Coordinate topLeft = rift.getPosition().derive(-2, 2);
            topRight = topLeft.derive(15, 0);
            bottomLeft = topRight.derive(-15, -15);
        } else if (rX > tX && rY > tY) {
            topRight = rift.getPosition().derive(2, 2);
            bottomLeft = topRight.getPosition().derive(-15, -15);
        } else if (rX > tX && rY < tY) {
            Coordinate bottomRight = rift.getPosition().derive(2, -2);
            topRight = bottomRight.derive(0, 15);
            bottomLeft = topRight.getPosition().derive(-15, -15);
        }
        Area.Rectangular rectangular = new Area.Rectangular(bottomLeft, topRight);
        CryoDung.INSTANCE.getController().setStartRoom(rectangular);
        return rectangular;
    }

    public boolean hasItemsToSell() {
        return false;
    }

    public boolean needsSupplies() {
        return false;
    }

    public boolean hasItemsToPickup() {
        return false;
    }

}
