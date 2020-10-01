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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.log4j.Log4j2;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

@Log4j2
@Component
@FxmlView("EnrollFromCamera")
public class EnrollFromCamera {

    private boolean capturing;
    private FileChooser fc;
    @FXML
    private Button stopCamera;
    @FXML
    private Button cameraChoose;
    @FXML
    private ComboBox<NCamera> comboBoxCamera;
    @FXML
    private StackPane paneView;
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

    private final CaptureCompletionHandler captureCompletionHandler = new CaptureCompletionHandler();
    private Stage mainStage;
    private final NDeviceManager deviceManager;
    private final NBiometricClient biometricClient;
    private final FaceViewNode faceViewNode;
    private NSubject subject;


    public EnrollFromCamera(NBiometricClient biometricClient) {
        super();
        this.biometricClient = biometricClient;
        biometricClient.setUseDeviceManager(true);
        deviceManager = biometricClient.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));
        deviceManager.initialize();
        fc = new FileChooser();
        this.faceViewNode = new FaceViewNode();
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

    @FXML
    public void initialize() throws IOException {
        comboBoxCamera.valueProperty().addListener(new CameraSelectionListener());
        updateCamerasList();
        this.paneView.getChildren().add(faceViewNode);
        this.paneView.setAlignment(Pos.CENTER);
        saveTemplate.setDisable(true);
        saveImage.setDisable(true);
        stopCamera.setDisable(true);
    }

    @FXML
    public void onLoad(ActionEvent event) {
        if (biometricClient.getFaceCaptureDevice() == null) {
            Alert a = new Alert(Alert.AlertType.NONE);
            a.setContentText("Please selecte camera from the list");
            a.setHeaderText("No camera selected");
            a.show();
            return;
        }

        NFace face = new NFace();
        EnumSet<NBiometricCaptureOption> options = EnumSet.of(NBiometricCaptureOption.STREAM);
        if (!cbAutomatic.isSelected()){
            options.add(NBiometricCaptureOption.MANUAL);
        }
        if (!cbCheckLiveness.isSelected()){
            biometricClient.setFacesLivenessMode(NLivenessMode.PASSIVE_AND_ACTIVE);
        }else {
            biometricClient.setFacesLivenessMode(NLivenessMode.NONE);
        }
        face.setCaptureOptions(options);
        subject = new NSubject();
        subject.getFaces().add(face);
        this.faceViewNode.setFace(face);
//        subject.setId("11111111");
        capturing = true;
        biometricClient.capture(subject, null, captureCompletionHandler);
        updateControls();
    }

    protected void updateControls() {
        comboBoxCamera.setDisable(capturing);
        cameraChoose.setDisable(capturing);
        stopCamera.setDisable(!capturing);
        startExtraction.setDisable(!capturing || cbAutomatic.isSelected());
        cbAutomatic.setDisable(capturing);
        cbCheckLiveness.setDisable(capturing);

        saveImage.setDisable(capturing || (subject == null) || (subject.getStatus() != NBiometricStatus.OK));
        saveTemplate.setDisable(capturing || (subject == null) || (subject.getStatus() != NBiometricStatus.OK));
    }

    @FXML
    public void stopCamera(ActionEvent event) {
        biometricClient.cancel();
    }

    @FXML
    public void saveImage(ActionEvent event) throws IOException {
        if (subject != null) {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "png"));
            File file = fc.showSaveDialog(mainStage);
            if (file != null) {
                subject.getFaces().get(0).getImage().save(file.getAbsolutePath());
            }
        }
    }

    @FXML
    public void startExtraction(ActionEvent event) {
        updateFacesTools();
        biometricClient.forceStart();
    }

    private void updateFacesTools() {
        biometricClient.setUseDeviceManager(true);
    }

    @FXML
    public void saveTemplate(ActionEvent event) throws IOException {
        if (subject != null){
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "png"));
            File file = fc.showSaveDialog(mainStage);
            if (file != null) {
                NFile.writeAllBytes(file.getAbsolutePath(),subject.getTemplateBuffer());
            }
        }

    }

    private class CameraSelectionListener implements ChangeListener<NCamera> {

        @Override
        public void changed(ObservableValue<? extends NCamera> observableValue, NCamera oldCamera, NCamera newCamera) {
            if (newCamera != null) {
                biometricClient.setFaceCaptureDevice(newCamera);
            }
        }
    }

    NSubject getSubject() {
        return subject;
    }

    private class CaptureCompletionHandler implements CompletionHandler<NBiometricStatus, Object> {

        @Override
        public void completed(final NBiometricStatus result, Object attachment) {
            Platform.runLater(() -> {
                capturing = false;
                if ((result == NBiometricStatus.OK) || (result == NBiometricStatus.CANCELED)) {
                    updateControls();
                } else {
                    getSubject().getFaces().get(0).setImage(null);
                    biometricClient.capture(getSubject(), null, captureCompletionHandler);
                }
            });
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            Platform.runLater(() -> {
                capturing = false;
                updateControls();
            });
        }
    }
}
