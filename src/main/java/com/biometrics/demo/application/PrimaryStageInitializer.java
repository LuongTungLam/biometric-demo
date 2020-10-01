package com.biometrics.demo.application;

//import com.biometrics.demo.controller.HomeController;
import com.biometrics.demo.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {
    private double x, y;
    private final FxWeaver fxWeaver;
    private static Scene scene;
    @Autowired
    public PrimaryStageInitializer(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.stage;
        stage.setTitle("Biometrics Faces");
        Image icon = new Image(getClass().getResourceAsStream("/images/Logo16x16.png"));
        stage.getIcons().add(icon);
        Parent p = fxWeaver.loadView(MainController.class);
        Scene scene = new Scene(p,800,600);
        stage.setScene(scene);
        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        stage.show();
//        Parent p = fxWeaver.loadView(HomeController.class);
//        stage.setScene(new Scene(p));
//        stage.initStyle(StageStyle.UNDECORATED);
//        p.setOnMousePressed(e -> {
//            x = e.getSceneX();
//            y = e.getSceneY();
//        });
//        p.setOnMouseDragged(e -> {
//            stage.setX(e.getScreenX() - x);
//            stage.setY(e.getScreenY() - y);
//        });
////        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
//        stage.show();
    }
}

