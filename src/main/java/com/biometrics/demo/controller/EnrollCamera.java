package com.biometrics.demo.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode;
import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import lombok.extern.log4j.Log4j2;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Log4j2
@Component
@FxmlView("EnrollCamera.fxml")
public class EnrollCamera {
    private boolean capturing;
    private FileChooser fileChooser;
    @FXML
    private JFXButton startCamera,stopCamera;
    @FXML
    private JFXComboBox<NCamera> comboBoxCamera;
    @FXML
    private StackPane paneView;

    private NSubject subject;

    public EnrollCamera(){

    }
    public void stopCapture(ActionEvent event) {
    }

    public void startCapture(ActionEvent event) {
    }
}
