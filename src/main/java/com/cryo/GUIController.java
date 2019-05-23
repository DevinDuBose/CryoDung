package com.cryo;

import com.cryo.entities.FloorSize;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.location.Coordinate;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class GUIController implements Initializable {

    private CryoDung dung;

    @FXML
    private ImageView logo;

    @FXML
    private ComboBox<Integer> complexity;

    @FXML
    private ComboBox<String> size;

    @FXML
    private CheckBox trainSkills;

    @FXML
    private Label startRoomLabel;

    @FXML
    private Label currentRoomLabel;

    @FXML
    private Label dungeonsEnteredLabel;

    @FXML
    private Label dungeonsFinishedLabel;

    public GUIController(CryoDung dung) {
        this.dung = dung;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logo.setImage(new Image(CryoDung.class.getResourceAsStream("/icon.png")));
        complexity.getItems().addAll(1, 2, 3, 4, 5, 6);
        size.getItems().addAll("Small", "Medium");

        complexity.valueProperty().addListener((observable, oldValue, newValue) -> {
            dung.setComplexity(newValue);
        });

        size.valueProperty().addListener((obs, oldV, newV) -> {
            FloorSize size = FloorSize.getSize(newV);
            if (size == null) return;
            dung.setSize(size);
        });

        trainSkills.selectedProperty().addListener((observable, oldValue, newValue) -> {
            dung.setTrainSkills(newValue);
        });

    }

    public void setStartRoom(Area.Rectangular rectangle) {
        Platform.runLater(() -> {
            Coordinate bottomLeft = rectangle.getBottomLeft();
            Coordinate topRight = rectangle.getTopRight();
            startRoomLabel.setText("[" + bottomLeft.getX() + ", " + bottomLeft.getY() + "], [" + topRight.getX() + ", " + topRight.getY() + "]");
        });
    }

    public void setCurrentRoom(int x, int y) {
        Platform.runLater(() -> currentRoomLabel.setText("[" + x + ", " + y + "]"));
    }

    public void setDungeonsEntered(int entered) {
        Platform.runLater(() -> dungeonsEnteredLabel.setText(Integer.toString(entered)));
    }

    public void setDungeonsFinished(int finished) {
        Platform.runLater(() -> dungeonsFinishedLabel.setText(Integer.toString(finished)));
    }
}
