package com.biometrics.demo.controller;

import com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.images.NImage;
import com.neurotec.licensing.NLicense;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
@FxmlView("EnrollFromImage.fmxl")
public class TokenFaceImage {
    private final FxWeaver fxWeaver;
    private final String greeting;
    private final com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode faceViewNode;
    private final com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode faceViewNodeNew;
    private final NBiometricClient biometricClient;
    private NSubject subject;
    private String fileName;
    private static final String license = "FaceClient";
    private FileChooser fc;
    @FXML
    private Button helloButton;
    @FXML
    private Pane panelHeader;
    @FXML
    private Pane paneDetect;
    @FXML
    private StackPane paneView;
    @FXML
    private StackPane paneView1;

    @FXML
    private Label lblQuality;
    private NFace tokenFace;
    private NImage image;
    private final ImageCreationHandler imageCreationHandler = new ImageCreationHandler();

    public TokenFaceImage(@Value("${spring.application.demo.greeting}") String greeting, FxWeaver fxWeaver, NBiometricClient biometricClient) {
        super();
        this.greeting = greeting;
        this.fxWeaver = fxWeaver;
        this.faceViewNode = new FaceViewNode();
        this.faceViewNodeNew = new FaceViewNode();
//        this.lblQuality = new Label();
        this.biometricClient = biometricClient;

    }


    private void createImage(String imagePath) {
        NFace face = new NFace();
        face.setFileName(imagePath);
        subject = new NSubject();
        subject.getFaces().add(face);
        this.faceViewNode.setFace(face);
        updateFaceTools();
        NBiometricTask task = biometricClient.createTask(EnumSet.of(NBiometricOperation.SEGMENT, NBiometricOperation.ASSESS_QUALITY), subject);
        biometricClient.performTask(task, null, imageCreationHandler);
    }

    private void updateFaceTools() {
        biometricClient.reset();
        boolean faceSegmentsDetectionActivatedNew;
        try {
            faceSegmentsDetectionActivatedNew = NLicense.isComponentActivated("Biometrics.FaceSegmentsDetection");
        } catch (IOException e) {
            e.printStackTrace();
            faceSegmentsDetectionActivatedNew = false;
        }
        biometricClient.setFacesDetectAllFeaturePoints(faceSegmentsDetectionActivatedNew);
        biometricClient.setFacesDetectBaseFeaturePoints(faceSegmentsDetectionActivatedNew);
        biometricClient.setFacesDetermineGender(faceSegmentsDetectionActivatedNew);
        biometricClient.setFacesDetermineAge(faceSegmentsDetectionActivatedNew);
        biometricClient.setFacesDetectProperties(faceSegmentsDetectionActivatedNew);
        biometricClient.setFacesRecognizeExpression(faceSegmentsDetectionActivatedNew);
    }

    @FXML
    public void initialize() {
        this.paneView.getChildren().add(faceViewNode);
        this.paneView.setAlignment(Pos.CENTER);
        this.paneView1.getChildren().addAll(faceViewNodeNew);
        this.paneView1.setAlignment(Pos.CENTER);
        helloButton.setOnAction(
                actionEvent -> {
                    try {
                        openFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public void openFile() throws IOException {
        fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fc.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        File file = fc.showOpenDialog(null);
        if (file != null) {
            fileName = file.getAbsolutePath();
            createImage(fileName);
        }
    }

    private class ImageCreationHandler implements CompletionHandler<NBiometricTask, Void> {
        @Override
        public void completed(final NBiometricTask task, final Void attachment) {
            Platform.runLater(() -> {
                NBiometricStatus status = task.getStatus();
                if (status == NBiometricStatus.OK) {
                    tokenFace = subject.getFaces().get(1);
                    faceViewNodeNew.setFace(tokenFace);
                    showTokenAttributes();
                } else {
//                        JOptionPane.showMessageDialog(CreateTokenFaceImage.this, "Could not create token face image. Status: " + status);
                    tokenFace = null;
                    faceViewNodeNew.setFace(null);
                }
                updateControls();
            });
        }

        @Override
        public void failed(final Throwable th, final Void attachment) {
            Platform.runLater(() -> {
                tokenFace = null;
                faceViewNodeNew.setFace(null);
            });
        }
    }

    private void updateControls() {
        showAttributeLabels(tokenFace != null);
    }

    private void showAttributeLabels(boolean show) {
        lblQuality.setVisible(show);
    }

    private void showTokenAttributes() {
        NLAttributes attributes = tokenFace.getObjects().get(0);
        lblQuality.setText("Quality: " + attributes.getQuality());
    }
}
