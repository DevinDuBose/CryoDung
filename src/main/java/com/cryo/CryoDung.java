package com.cryo;

import com.cryo.entities.Floor;
import com.cryo.entities.FloorSize;
import com.runemate.game.api.client.embeddable.EmbeddableUI;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.input.Keyboard;
import com.runemate.game.api.hybrid.local.hud.interfaces.*;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.location.navigation.cognizant.RegionPath;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.LoopingBot;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.util.Arrays;

public class CryoDung extends LoopingBot implements EmbeddableUI {

    private Area ENTRANCE_AREA = Area.rectangular(new Coordinate(3446, 3722, 0), new Coordinate(3454, 3726, 0));

    private String status;

    private boolean inDung;

    private Dungeon dungeon;

    private FloorSize size;
    private int complexity = -1;

    private Floor[] floors;
    private Floor currentFloor;

    private boolean started;

    private ObjectProperty<Node> interfaceProperty;

    @Override
    public ObjectProperty<? extends Node> botInterfaceProperty() {
        if (interfaceProperty == null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setController(new GUIController(this));
            Node node;
            try {
                node = loader.load(CryoDung.class.getResourceAsStream("/GUI.fxml"));
                interfaceProperty = new SimpleObjectProperty<>(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return interfaceProperty;
    }

    public enum State {
        TELEPORTING_TO_DUNG, WALKING_TO_ENTRANCE, FORMING_PARTY, SETTING_COMPLEXITY, SETTING_FLOOR, STARTING_DUNGEON, WAITING
    }

    public CryoDung() {
        setEmbeddableUI(this);
        floors = new Floor[60];
    }

    @Override
    public void onStart(String... args) {
        status = "Started CryoDung";
    }

    @Override
    public void onLoop() {
        if (Players.getLocal() == null)
            return;
        if (!started) {
            started = true;
            Execution.delay(10000);
            return;
        }
        if (dungeon != null) {
            dungeon.loop();
            return;
        }
        State state = checkState();
        if (state == null) {
            stop("Unable to determine next step.");
            return;
        }
        System.out.println(state);
        switch (state) {
            case TELEPORTING_TO_DUNG:
                if (!interactWithRing("Teleport to Daemonheim")) {
                    stop("You need to start with a ring of kinship equipped or in your inventory.");
                    return;
                }
                break;
            case WALKING_TO_ENTRANCE:
                RegionPath path = RegionPath.buildTo(ENTRANCE_AREA.getRandomCoordinate());
                if (path != null) path.setStepDeviation(3);
                if (path != null && path.step())
                    Execution.delayWhile(() -> Players.getLocal().isMoving(), 500, 2000);
                break;
            case FORMING_PARTY:
                openDungInterface();
                if (!isInParty()) joinParty();
                break;
            case SETTING_COMPLEXITY:
                openDungInterface();
                openComplexityInterface();
                InterfaceComponent confirm = Interfaces.getAt(938, 66);
                InterfaceComponent complexityButton = Interfaces.getAt(938, 19 + complexity - 1);
                InterfaceComponent realComplexityComponent = Interfaces.getAt(938, 10);
                int realComplexity = Integer.parseInt(realComplexityComponent.getText());
                while (realComplexity != complexity) {
                    complexityButton.click();
                    Execution.delay(1000, 3000);
                }
                while (confirm != null && confirm.isVisible()) {
                    confirm.click();
                    Execution.delay(1000, 3000);
                }
                break;
            case SETTING_FLOOR:
                openDungInterface();
                openFloorSelection();
                checkFloors();
                if (resetRequired()) {
                    reset();
                    openFloorSelection();
                    checkFloors();
                }
                Floor lowest = getLowestFloor();
                if (lowest == null) return;
                InterfaceComponent floor = Interfaces.getAt(947, 89 + lowest.getId() - 1);
                InterfaceComponent realFloorComponent = Interfaces.getAt(947, 727);
                int realFloor = Integer.parseInt(realFloorComponent.getText());
                while (realFloor != lowest.getId()) {
                    floor.click();
                    Execution.delay(1000, 3000);
                }
                currentFloor = lowest;
                Keyboard.pressKey(27);
                break;
            case STARTING_DUNGEON:
                GameObject entrance = GameObjects.newQuery().ids(48496).results().nearest();
                InterfaceComponent enterDungeon = Interfaces.getAt(1049, 50, 10);
                while (enterDungeon == null || !enterDungeon.isVisible()) {
                    entrance.click();
                    Execution.delay(5000, 10000);
                    enterDungeon = Interfaces.getAt(1049, 50, 4);
                }
                while (!size.equals(getSelectedSize())) {
                    InterfaceComponent correct = Interfaces.getAt(1049, size.getComponentId() + 1);
                    correct.click();
                    Execution.delay(1000, 3000);
                }
                while (enterDungeon != null && enterDungeon.isVisible())
                    enterDungeon.click();
                inDung = true;
                break;
            case WAITING:
                Execution.delay(1000, 3000);
                break;
        }
    }

    public FloorSize getSelectedSize() {
        for (FloorSize size : FloorSize.values()) {
            InterfaceComponent component = Interfaces.getAt(1049, size.getComponentId());
            Integer textureId = component.getTextureId();
            if (textureId != null && textureId == 18525) return size;
        }
        return null;
    }

    public boolean resetRequired() {
        return Arrays.stream(floors).filter(f -> f.isOpen() && !f.isCompleted()).count() == 0;
    }

    public void reset() {
        openDungInterface();
        InterfaceComponent resetButton = Interfaces.getAt(91, 0);
        if (resetButton == null || !resetButton.isVisible()) return;
        while (!ChatDialog.isOpen()) {
            resetButton.click();
            Execution.delay(1000, 3000);
        }
        ChatDialog.getContinue().select();
        Execution.delay(300, 500);
        ChatDialog.getOption(1).select();
        Execution.delay(1000, 3000);
    }

    public Floor getLowestFloor() {
        for (int i = 0; i < floors.length; i++)
            if (floors[i].isOpen() && !floors[i].isCompleted()) return floors[i];
        return null;
    }

    public void checkFloors() {
        for (int i = 0; i < 60; i++) {
            InterfaceComponent floor = Interfaces.getAt(947, 89 + i);
            floors[i] = new Floor(i + 1);
            floors[i].setOpen(floor.isVisible());
            floor = Interfaces.getAt(947, 150 + i);
            floors[i].setCompleted(floor.isVisible());
        }
    }

    public boolean interactWithRing() {
        return interactWithRing(null, true);
    }

    public boolean interactWithRing(String interaction) {
        return interactWithRing(interaction, false);
    }

    public boolean interactWithRing(String interaction, boolean click) {
        SpriteItem item = null;
        if (Equipment.contains("Ring of kinship")) item = Equipment.getItems("Ring of kinship").first();
        else if (Inventory.contains("Ring of kinship")) item = Inventory.getItems("Ring of kinship").first();
        if (item == null) return false;
        Coordinate coords = Players.getLocal().getPosition();
        boolean success = click ? item.click() : item.interact(interaction);
        if (success) {
            Execution.delayWhile(() -> Players.getLocal().getPosition().equals(coords), 15000, 30000);
            return true;
        }
        return true;
    }

    public boolean isDungInterfaceOpen() {
        InterfaceComponent formPartyComponent = Interfaces.getAt(91, 35);
        InterfaceComponent leavePartyComponent = Interfaces.getAt(91, 17);
        return (formPartyComponent != null && formPartyComponent.isVisible()) || (leavePartyComponent != null && leavePartyComponent.isVisible());
    }

    public boolean isInParty() {
        InterfaceComponent formPartyComponent = Interfaces.getAt(91, 35);
        return !(formPartyComponent != null && formPartyComponent.isVisible());
    }

    public void joinParty() {
        if (isInParty()) return;
        InterfaceComponent formPartyComponent = Interfaces.getAt(91, 35);
        while (!isInParty()) {
            formPartyComponent.click();
            Execution.delay(1000, 3000);
        }
    }

    public void openFloorSelection() {
        InterfaceComponent component = Interfaces.getAt(91, 46);
        InterfaceComponent floorConfirm = Interfaces.getAt(947, 721);
        while (floorConfirm == null || !floorConfirm.isVisible()) {
            component.click();
            Execution.delay(1000, 3000);
            floorConfirm = Interfaces.getAt(947, 721);
        }
    }

    public void openComplexityInterface() {
        InterfaceComponent component = Interfaces.getAt(91, 53);
        InterfaceComponent confirm = Interfaces.getAt(938, 66);
        while (confirm == null || !confirm.isVisible()) {
            component.click();
            Execution.delay(1000, 3000);
        }
    }

    public void openDungInterface() {
        if (!isDungInterfaceOpen())
            while (!interactWithRing()) Execution.delay(1000, 3000);
    }

    public int getComplexity() {
        openDungInterface();
        InterfaceComponent component = Interfaces.getAt(91, 52);
        String text = component.getText();
        try {
            return Integer.parseInt(text);
        } catch (Exception e) {

        }
        return -1;
    }

    public State checkState() {
        int regionId = Players.getLocal().getPosition().getContainingRegionId();
        if (!inDung && regionId != 13625 && regionId != 13626)
            return State.TELEPORTING_TO_DUNG;
        else if ((regionId == 13625 || regionId == 13626) && !ENTRANCE_AREA.contains(Players.getLocal()))
            return State.WALKING_TO_ENTRANCE;
        else if (ENTRANCE_AREA.contains(Players.getLocal())) {
            if (!isDungInterfaceOpen() || !isInParty())
                return State.FORMING_PARTY;
            if (complexity == -1 || size == null) return State.WAITING;
            if (currentFloor == null) return State.SETTING_FLOOR;
            if (complexity != getComplexity()) return State.SETTING_COMPLEXITY;
            return State.STARTING_DUNGEON;
        }
        return null;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public void setSize(FloorSize size) {
        this.size = size;
    }
}
