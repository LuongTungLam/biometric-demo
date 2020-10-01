package com.biometrics.demo.controller;

import com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.swing.NFaceView;
import com.neurotec.images.NImage;
import com.neurotec.io.NFile;
import com.neurotec.licensing.NLicense;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@Component
@FxmlView("EnrollFromImage.fmxl")
public class EnrollFromImage {
    private final FxWeaver fxWeaver;
    private final String greeting;
    private final com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode faceViewNode;
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
    private NFaceView view;
    private NImage image;
    private final FaceDetectionHandler faceDetectionHandler = new FaceDetectionHandler();
    public EnrollFromImage(@Value("${spring.application.demo.greeting}") String greeting, FxWeaver fxWeaver, NBiometricClient biometricClient) {
        super();
        this.greeting = greeting;
        this.fxWeaver = fxWeaver;
        this.faceViewNode = new FaceViewNode();
        this.biometricClient = biometricClient;

    }


    private void detectFace(NImage faceImage){
        updateFacesTools();
        biometricClient.detectFaces(faceImage,null,faceDetectionHandler);
    }

    @FXML
    public void initialize() {
//        log.info("FacesDetermindeAge ? " + biometricClient.isFacesDetermineAge());
        this.paneView.getChildren().add(faceViewNode);
        this.paneView.setAlignment(Pos.CENTER);
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
            image = NImage.fromFile(fileName);
            subject = new NSubject();
            NFace face = new NFace();
            face.setImage(image);
            subject.getFaces().add(face);
            this.faceViewNode.setFace(face);
            createTemplate();
        }
    }

    private void createTemplate() throws IOException {
        if (subject != null) {
            detectFace(image);
        }
    }

    protected void updateFacesTools(){
        biometricClient.reset();
        boolean faceSegmentsDetectionActivated;
        try {
            faceSegmentsDetectionActivated = NLicense.isComponentActivated("Biometrics.FaceSegmentsDetection");
        } catch (IOException e) {
            e.printStackTrace();
            faceSegmentsDetectionActivated = false;
        }
        biometricClient.setFacesDetectAllFeaturePoints(faceSegmentsDetectionActivated);
        biometricClient.setFacesDetectBaseFeaturePoints(faceSegmentsDetectionActivated);
    }

    private void clear() {
        paneView.getChildren().clear();
    }
    private class FaceDetectionHandler implements CompletionHandler<NFace, Object> {
        @Override
        public void completed(final NFace result, final Object attachment) {
            Platform.runLater(() ->{
                faceViewNode.setFace(result);
            });
        }
        @Override
        public void failed(final Throwable th, final Object attachment) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                }

            });
        }

    }
}
