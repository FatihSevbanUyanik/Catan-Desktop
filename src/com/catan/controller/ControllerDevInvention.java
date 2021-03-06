package com.catan.controller;

import com.catan.Util.Constants;
import com.catan.modal.DevelopmentCard;
import com.catan.modal.Player;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

public class ControllerDevInvention {

    @FXML
    private Label labelTitle;
    @FXML
    private Polygon decrementBrick;
    @FXML
    private Label labelBrick;
    @FXML
    private Polygon incrementBrick;
    @FXML
    private Polygon decrementGrain;
    @FXML
    private Label labelGrain;
    @FXML
    private Polygon incrementGrain;
    @FXML
    private Polygon decrementLumber;
    @FXML
    private Label labelLumber;
    @FXML
    private Polygon incrementLumber;
    @FXML
    private Polygon decrementWool;
    @FXML
    private Label labelWool;
    @FXML
    private Polygon incrementWool;
    @FXML
    private Polygon decrementOre;
    @FXML
    private Label labelOre;
    @FXML
    private Polygon incrementOre;
    @FXML
    private AnchorPane root;
    @FXML
    private ImageView imgBrick;
    @FXML
    private ImageView imgGrain;
    @FXML
    private ImageView imgLumber;
    @FXML
    private ImageView imgWool;
    @FXML
    private ImageView imgOre;
    @FXML
    ResourceBundle resources;
    
    // properties
    private int totalCount = 0;
    private ArrayList<Polygon> incrementShapes;
    private ArrayList<Polygon> decrementShapes;
    private ArrayList<Label> labels;
    private Player currentPlayer;
    private DevelopmentCard developmentCard;
    private HashMap<String, Integer> desiredResources;

    @FXML
    public void initialize() {
        root.setStyle("-fx-background-image: url("+ Constants.PATH_BG_INVENTION() +");\n" +
                      "-fx-background-size: cover;\n");

        ArrayList<ImageView> imgResources = new ArrayList<>(Arrays.asList(
                imgOre, imgBrick, imgLumber,
                imgGrain, imgWool));

        incrementShapes = new ArrayList<>(Arrays.asList(
                incrementOre, incrementBrick,
                incrementLumber, incrementGrain,
                incrementWool));

        decrementShapes = new ArrayList<>(Arrays.asList(
                decrementOre, decrementBrick,
                decrementLumber, decrementGrain,
                decrementWool));

        labels = new ArrayList<>(Arrays.asList(
                labelOre, labelBrick,
                labelLumber, labelGrain,
                labelWool));

        desiredResources = new HashMap<>();
        for (int i = 0; i < Constants.resourceNames.size(); i++) {
            String resourceName = Constants.resourceNames.get(i);
            String resourcePath = Constants.getResourcePaths().get(i);
            desiredResources.put(resourceName, 0);
            Image image = new Image(resourcePath);
            imgResources.get(i).setImage(image);
        }
    }

    public void setProperties(Player currentPlayer, DevelopmentCard developmentCard) {
        this.currentPlayer = currentPlayer;
        this.developmentCard = developmentCard;
    }

    @FXML
    void increment(MouseEvent event) {
        for (int i = 0; i < incrementShapes.size() && totalCount < 2; i++) {
            if (event.getSource() == incrementShapes.get(i)) {
                String key = Constants.resourceNames.get(i);
                int resourceCount = desiredResources.get(key);
                desiredResources.put(key, ++resourceCount);
                labels.get(i).setText(resourceCount + "");
                totalCount++;
                break;
            }
        }
    }

    @FXML
    void decrement(MouseEvent event) {
        for (int i = 0; i < decrementShapes.size() && totalCount > 0; i++) {
            if (event.getSource() == decrementShapes.get(i)) {
                String key = Constants.resourceNames.get(i);
                int resourceCount = desiredResources.get(key);
                desiredResources.put(key, --resourceCount);
                labels.get(i).setText(resourceCount + "");
                totalCount--;
                break;
            }
        }
    }

    @FXML
    void complete(ActionEvent actionEvent) {
        if (totalCount == 2) {
            developmentCard.performInventionCardFeatures(currentPlayer, desiredResources);
            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
            window.close();
        } else {
            labelTitle.setText(resources.getString("inventionDevCardView_ChooseResource"));
            labelTitle.setTextFill(Color.ORANGE);
        }
    }

}