package com.biometrics.demo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@FxmlView("HomeController.fxml")
public class HomeController {

    @FXML
    private ImageView imageDetect;
    @FXML
    private AnchorPane anchorPaneItems;
    private final FxWeaver fxWeaver;

    public HomeController(FxWeaver fxWeaver) {
        super();
        this.fxWeaver = fxWeaver;
    }

    @FXML
    public void initialize() {

    }

    @FXML
    private void switchToDetectPane() {
        try {
            clear();
            AnchorPane anchorPaneNews = (AnchorPane) FXMLLoader.load(getClass().getResource("DetectFace.fxml"));
            anchorPaneItems.getChildren().add(anchorPaneNews);
            AnchorPane.setRightAnchor(anchorPaneNews, 0.0);
            anchorPaneNews.toFront();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToErollCamera() {
        try {
            clear();
            AnchorPane anchorPaneNews = (AnchorPane) FXMLLoader.load(getClass().getResource("EnrollCamera.fxml"));
            anchorPaneItems.getChildren().add(anchorPaneNews);
            AnchorPane.setRightAnchor(anchorPaneNews, 0.0);
            anchorPaneNews.toFront();
        } catch (IOException e) {
            Logger.getLogger(EnrollCamera.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @FXML
    private void switchToErollImage() {
        try {
            clear();
            AnchorPane anchorPaneNews = (AnchorPane) FXMLLoader.load(getClass().getResource("EnrollImage.fxml"));
            anchorPaneItems.getChildren().add(anchorPaneNews);
            AnchorPane.setRightAnchor(anchorPaneNews, 0.0);
            anchorPaneNews.toFront();
        } catch (IOException e) {
            Logger.getLogger(EnrollCamera.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void clear() {
        anchorPaneItems.getChildren().clear();
    }


}
