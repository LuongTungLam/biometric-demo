package com.biometrics.demo.controller;

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NSubject.FaceCollection;
import com.neurotec.biometrics.NTemplateSize;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@FxmlView("VerifyFaceController.fxml")
public class VerifyFaceController {

    private static final String SUBJECT_LEFT = "left";
    private static final String SUBJECT_RIGHT = "right";

    private static final List<String> THRESHOLDS = new ArrayList<String>();

    static {
        THRESHOLDS.add("1%");
        THRESHOLDS.add("0.1%");
        THRESHOLDS.add("0.01%");
        THRESHOLDS.add("0.001%");
    }

    private final NBiometricClient biometricClient;
    private FileChooser fc;
    private String fileName;
    private NSubject subjectLeft;
    private NSubject subjectRight;
    private final FaceViewNodeNew faceViewNodeLeft;
    private final FaceViewNodeNew faceViewNodeRight;
    @FXML
    private Button leftButton;
    @FXML
    private Button rightButton;
    @FXML
    private StackPane leftView;
    @FXML
    private StackPane rightView;
    @FXML
    private Button verifyFaces;
    @FXML
    private ComboBox comboBoxMatchingFarThreshold;
    private final TemplateCreationHandler templateCreationHandler = new TemplateCreationHandler();
    private final VerificationHandler verificationHandler = new VerificationHandler();

    public VerifyFaceController(NBiometricClient biometricClient) {
        super();
        this.biometricClient = biometricClient;
        this.faceViewNodeRight = new FaceViewNodeNew();
        this.faceViewNodeLeft = new FaceViewNodeNew();
    }

    @FXML
    public void initialize() {
        comboBoxMatchingFarThreshold.getItems().addAll(THRESHOLDS);
        comboBoxMatchingFarThreshold.getSelectionModel().selectFirst();
        this.leftView.getChildren().add(faceViewNodeLeft);
        this.leftView.setAlignment(Pos.CENTER);
        this.rightView.getChildren().addAll(faceViewNodeRight);
        this.rightView.setAlignment(Pos.CENTER);
        this.verifyFaces.setDisable(true);
    }


    private void loadItem(String position) throws IOException {
        fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fc.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        File file = fc.showOpenDialog(null);
        if (file != null) {
            fileName = file.getAbsolutePath();
            NSubject subjectTmp = null;
            NFace face = null;
            try {
                subjectTmp = NSubject.fromFile(fileName);
                FaceCollection faces = subjectTmp.getFaces();
                if (faces.isEmpty()) {
                    subjectTmp = null;
                    throw new IllegalArgumentException("Template contains no face records.");
                }
                face = faces.get(0);
                templateCreationHandler.completed(NBiometricStatus.OK, position);
            } catch (UnsupportedOperationException e) {
//
            }
            if (subjectTmp == null) {
                face = new NFace();
                face.setFileName(fileName);
                subjectTmp = new NSubject();
                subjectTmp.getFaces().add(face);
                updateFacesTools();
                biometricClient.createTemplate(subjectTmp, position, templateCreationHandler);
            }

            if (SUBJECT_LEFT.equals(position)) {
                subjectLeft = subjectTmp;
                faceViewNodeLeft.setFace(face);
            } else if (SUBJECT_RIGHT.equals(position)) {
                subjectRight = subjectTmp;
                faceViewNodeRight.setFace(face);
            } else {
                throw new AssertionError("Unknown subject position: " + position);
            }
        }
    }

    @FXML
    public void openLeft(ActionEvent event) {
        if (event.getSource().equals(leftButton)) {
            try {
                loadItem(SUBJECT_LEFT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateFacesTools() {
        biometricClient.setFacesDetectAllFeaturePoints(true);
    }

    @FXML
    public void openRight(ActionEvent event) {
        if (event.getSource().equals(rightButton)) {
            try {
                loadItem(SUBJECT_RIGHT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void verify(ActionEvent event) {
        updateFacesTools();
        biometricClient.verify(subjectLeft, subjectRight, null, verificationHandler);
    }

    private class TemplateCreationHandler implements CompletionHandler<NBiometricStatus, String> {

        @Override
        public void completed(final NBiometricStatus status, final String subject) {
            Platform.runLater(() -> {
                if (status != NBiometricStatus.OK) {
                    EventHandler<ActionEvent> event4 = new EventHandler<ActionEvent>() {
                        public void handle(ActionEvent e) {
                            Alert a1 = new Alert(Alert.AlertType.NONE,
                                    "Template was not created: " + status, ButtonType.APPLY);
                            a1.show();
                        }
                    };
                }
                updateControls();
            });
        }

        @Override
        public void failed(final Throwable th, final String subject) {

        }

    }

    private class VerificationHandler implements CompletionHandler<NBiometricStatus, String> {

        @Override
        public void completed(final NBiometricStatus status, final String subject) {
            Platform.runLater(() -> {
                if (status == NBiometricStatus.OK) {
                    int score = getLeft().getMatchingResults().get(0).getScore();
                    String msg = "Score of matched templates: " + score;
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Point match");
                        alert.setHeaderText(msg);
                        alert.showAndWait();
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Point match");
                        alert.setHeaderText("Templates didn't match.");
                        alert.showAndWait();
                    });
                }
            });
        }

        @Override
        public void failed(final Throwable th, final String subject) {

        }

    }

    NSubject getLeft() {
        return subjectLeft;
    }

    NSubject getRight() {
        return subjectRight;
    }

    protected void updateControls() {
        if (subjectLeft.getFaces().isEmpty()
                || subjectLeft.getFaces().get(0).getObjects().isEmpty()
                || (subjectLeft.getFaces().get(0).getObjects().get(0).getTemplate() != null)
                || subjectRight.getFaces().isEmpty()
                || subjectRight.getFaces().get(0).getObjects().isEmpty()
                || (subjectRight.getFaces().get(0).getObjects().get(0).getTemplate() != null)) {
            verifyFaces.setDisable(false);
        } else {
            verifyFaces.setDisable(true);
        }
    }
}
