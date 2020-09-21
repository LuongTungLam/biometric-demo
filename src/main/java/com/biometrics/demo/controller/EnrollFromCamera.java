package com.biometrics.demo.controller;

import com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode;
import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import lombok.extern.log4j.Log4j2;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.EnumSet;

@Log4j2
@Component
@FxmlView("EnrollFromCamera")
public class EnrollFromCamera {
    private boolean capturing;
    private FileChooser fc;

    @FXML
    private Button cameraChoose;
    @FXML
    private Pane cameraView;
    @FXML
    private ComboBox comboBoxCamera;
    @FXML
    private StackPane paneView;
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
//        updateCamerasList();
    }

    @FXML
    public void initialize() {
        for (NDevice device : deviceManager.getDevices()) {
//            ObservableList<NDevice> options = FXCollections.observableArrayList(device);
            comboBoxCamera.getItems().addAll(device);
        }

        NCamera camera = biometricClient.getFaceCaptureDevice();
        if (camera == null) {
            comboBoxCamera.getSelectionModel().select(0);
            comboBoxCamera.getSelectionModel().getSelectedIndex();
        } else if (camera != null) {
            comboBoxCamera.getSelectionModel().getSelectedItem();
        }
        this.paneView.getChildren().add(faceViewNode);
        this.paneView.setAlignment(Pos.CENTER);

        cameraChoose.setOnAction(
                actionEvent -> {
                    try {
                        cameraChose();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void cameraChose() throws IOException {
        Alert a = new Alert(Alert.AlertType.NONE);
        if (biometricClient.getFaceCaptureDevice() == null){
            EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    a.setAlertType(Alert.AlertType.ERROR);

                    a.setContentText("Face Capture Device Null");

                    a.show();
                }
            };
            return;
        }
        NFace face = new NFace();
        EnumSet<NBiometricCaptureOption> options = EnumSet.of(NBiometricCaptureOption.STREAM);
        face.setCaptureOptions(options);
        subject = new NSubject();
        subject.getFaces().add(face);
        biometricClient.capture(subject);
        this.faceViewNode.setFace(face);
        capturing = true;
    }

}
