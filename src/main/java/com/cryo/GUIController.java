package com.cryo;

import com.cryo.entities.FloorSize;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
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
    }
}
