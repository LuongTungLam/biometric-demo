package com.biometrics.demo.controller;

import com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.images.NImage;
import com.neurotec.io.NFile;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
@FxmlView("GeneralizeFace.fxml")
public class GeneralizeFace {
    private final NBiometricClient biometricClient;
    private NSubject subject;
    private String fileName;
    private final FxWeaver fxWeaver;
    private final String greeting;
    private final FaceViewNodeNew faceView;
    private FileChooser fc;
    private NImage image;
    @FXML
    private Button openFile,saveTemplate;
    @FXML
    private StackPane paneView;
    @FXML
    private Label lblStatus, lblImageCount;

    private final TemplateCreationHandler templateCreationHandler = new TemplateCreationHandler();

    public GeneralizeFace(@Value("${spring.application.demo.greeting}") String greeting, FxWeaver fxWeaver, NBiometricClient biometricClient) {
        super();
        this.greeting = greeting;
        this.fxWeaver = fxWeaver;
        this.faceView = new FaceViewNodeNew();
        this.biometricClient = biometricClient;
    }

    @FXML
    public void initialize() {
        this.paneView.getChildren().add(faceView);
        this.paneView.setAlignment(Pos.CENTER);
        saveTemplate.setDisable(true);
    }

    @FXML
    public void openFile(ActionEvent event) throws IOException {
        subject = null;
        faceView.setFace(null);
        fc = new FileChooser();
        List<File> files = fc.showOpenMultipleDialog(null);
        if (files != null) {
            subject = new NSubject();
            for (File file : files) {
                NFace face = new NFace();
                face.setImage(NImage.fromFile(file.getAbsolutePath()));
                face.setSessionId(1); // all faces with same session will be generalized
                subject.getFaces().add(face);
            }
            lblImageCount.setText(String.format("Count Image: %s", String.valueOf(files.size())));
            lblStatus.setText("Status: performing extraction and generalization");
            lblStatus.setVisible(true);
            updateControls();
            updateFacesTools();
            biometricClient.createTemplate(subject, null, templateCreationHandler);
        }
    }

    private void updateFacesTools() {
        biometricClient.reset();
    }

    private void updateControls() {
        saveTemplate.setDisable((subject == null) && (subject.getStatus() == NBiometricStatus.OK));
    }

    @FXML
    public void saveTemplate(ActionEvent event) throws IOException {
        if (subject != null) {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Template files (*.dat)", "*.dat"));
            File file = fc.showSaveDialog(null);
            if (file != null) {
                NFile.writeAllBytes(file.getAbsolutePath(), subject.getTemplateBuffer());
            }
        }
    }

    private class TemplateCreationHandler implements CompletionHandler<NBiometricStatus, Object> {
        @Override
        public void completed(NBiometricStatus status, Object o) {
            Platform.runLater(() -> {
                if (status == NBiometricStatus.OK) {
//                    openFile.setDisable(false);
                    faceView.setFace(subject.getFaces().get(subject.getFaces().size() - 1));
                    lblStatus.setText("Status: OK");
                } else {
                    lblStatus.setText(String.format("Status: %s", status));
                }
                openFile.setDisable(false);
            });
        }

        @Override
        public void failed(Throwable throwable, Object o) {
            Platform.runLater(() -> {
                lblStatus.setText("Status: Error occurred");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Point match");
                    alert.setHeaderText("Templates didn't match.");
                    alert.showAndWait();
                });
                openFile.setDisable(false);
            });
        }
    }
}
