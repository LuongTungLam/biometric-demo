package com.biometrics.demo.application;

import com.biometrics.demo.controller.MainController;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final FxWeaver fxWeaver;

    @Autowired
    public PrimaryStageInitializer(FxWeaver fxWeaver){
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
        stage.show();
    }
}

