package com.biometrics.demo.controller;

import com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.io.NFile;
import com.neurotec.util.concurrent.CompletionHandler;
import com.sun.javafx.collections.ChangeHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

@Component
@FxmlView("CaptureIcaoCompliantImage.")
public class CaptureIcaoCompliantImage {
    private final FxWeaver fxWeaver;
    private final String greeting;
    private final FaceViewNodeNew faceViewNodeNew;
    private final IcaoWarningsPanel warningsPanel;
    private final NDeviceManager deviceManager;
    private final NBiometricClient biometricClient;
    private NSubject subject;
    private boolean capturing;
    private FileChooser fc;
    private final CaptureCompletionHandler captureCompletionHandler = new CaptureCompletionHandler();

    @FXML
    private Button stopCamera;
    @FXML
    private Button startCamera;
    @FXML
    private ComboBox<NCamera> comboBoxCamera;
    @FXML
    private StackPane paneView;
    @FXML
    private StackPane paneView1;
    @FXML
    private Button saveImage;
    @FXML
    private Button startExtraction;
    @FXML
    private Button saveTemplate;
    @FXML
    private Pane cameraEnroll;
    @FXML
    private CheckBox cbAutomatic;
    @FXML
    private CheckBox cbCheckLiveness;

    public CaptureIcaoCompliantImage(@Value("${spring.application.demo.greeting}") String greeting, FxWeaver fxWeaver, NBiometricClient biometricClient) {
        super();
        this.greeting = greeting;
        this.fxWeaver = fxWeaver;
        this.warningsPanel = new IcaoWarningsPanel();
        this.faceViewNodeNew = new FaceViewNodeNew();
        this.biometricClient = biometricClient;
        biometricClient.setUseDeviceManager(true);
        deviceManager = biometricClient.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));
        deviceManager.initialize();
        fc = new FileChooser();
    }

    @FXML
    public void initialize() {
        updateCamerasList();
        this.paneView.getChildren().add(faceViewNodeNew);
        this.paneView.setAlignment(Pos.CENTER);
        this.paneView1.getChildren().addAll(warningsPanel);
        this.paneView1.setAlignment(Pos.CENTER);
        updateControls();
    }


    private void updateCamerasList() {
        for (NDevice device : deviceManager.getDevices()) {
            comboBoxCamera.getItems().add((NCamera) device);
        }
        NCamera camera = biometricClient.getFaceCaptureDevice();
        if (camera == null) {
            comboBoxCamera.getSelectionModel().selectFirst();
        } else if (camera != null) {
            comboBoxCamera.getSelectionModel().select(camera);
        }
    }

    public void saveTemplate(ActionEvent event) throws IOException {
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Template files (*.dat)", "*.dat"));
        File file = fc.showSaveDialog(null);
        if (file != null) {
            NFile.writeAllBytes(file.getAbsolutePath(), subject.getTemplateBuffer());
        }
    }

    public void startExtraction(ActionEvent event) {
        biometricClient.force();
    }

    public void saveImage(ActionEvent event) throws IOException {
        if (subject != null) {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "png"));
            File file = fc.showSaveDialog(null);
            if (file != null) {
                subject.getFaces().get(0).getImage().save(file.getAbsolutePath());
            }
        }
    }

    public void stopCamera(ActionEvent event) {
        biometricClient.cancel();
        updateControls();
    }

    public void startCamera(ActionEvent event) {
        updateFacesTools();
        if (biometricClient.getFaceCaptureDevice() == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("No camera selected");
                alert.setHeaderText("Please select camera from the list");
                alert.showAndWait();
            });
            return;
        }
        NFace face = new NFace();
        face.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.STREAM));
        subject = new NSubject();
        subject.getFaces().add(face);
        faceViewNodeNew.setFace(face);
        warningsPanel.setFace(face);
        capturing = true;

        NBiometricTask task = biometricClient.createTask(EnumSet.of(NBiometricOperation.CAPTURE, NBiometricOperation.SEGMENT, NBiometricOperation.CREATE_TEMPLATE), subject);
        biometricClient.performTask(task, null, captureCompletionHandler);
        updateControls();
    }

    private class CaptureCompletionHandler implements CompletionHandler<NBiometricTask, Object> {

        @Override
        public void completed(final NBiometricTask task, final Object attachment) {
            Platform.runLater(() -> {
                if (task.getError() != null) {
                    failed(task.getError(), attachment);
                    return;
                }
                final NBiometricStatus status = task.getStatus();
                if (status == NBiometricStatus.OK) {
                    faceViewNodeNew.setFace(subject.getFaces().get(1));
                    warningsPanel.setFace(subject.getFaces().get(1));
                }

                Platform.runLater(() -> {
                    capturing = false;
                    updateControls();
                });
            });
        }

        @Override
        public void failed(Throwable throwable, Object o) {
            Platform.runLater(() -> {
                capturing = false;
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Point match");
                    alert.setHeaderText("Templates didn't match.");
                    alert.showAndWait();
                });
                updateControls();
            });
        }
    }

    private void updateControls() {
        comboBoxCamera.setDisable(capturing);
        startCamera.setDisable(capturing);
        stopCamera.setDisable(!capturing);
        startExtraction.setDisable(!capturing);

        saveImage.setDisable(capturing || (subject == null) || (subject.getStatus() != NBiometricStatus.OK));
        saveTemplate.setDisable(capturing || (subject == null) || (subject.getStatus() != NBiometricStatus.OK));
    }

    private void updateFacesTools() {
        biometricClient.setUseDeviceManager(true);
        biometricClient.setFacesCheckIcaoCompliance(true);
    }
}
