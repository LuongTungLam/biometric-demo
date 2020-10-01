package com.biometrics.demo.controller;

import com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
@FxmlView("MatchMultipleFaces.fxml")
public class MatchMultipleFaces {
    private final FxWeaver fxWeaver;
    private final String greeting;
    private final com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode faceViewNode;
    private final FaceViewNodeNew faceViewNodeNew;
    private final NBiometricClient biometricClient;
    private NSubject reference;
    private NSubject multipleFaces;
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
    private static final String ATTACHMENT_REFERENCE = "reference";
    private static final String ATTACHMENT_MULTIPLE_FACES = "multiple_faces";

    private final EnrollHandler enrollHandler = new EnrollHandler();
    private final IdentificationHandler identificationHandler = new IdentificationHandler();
    private final TemplateCreationHandler templateCreationHandler = new TemplateCreationHandler();

    public MatchMultipleFaces(@Value("${spring.application.demo.greeting}") String greeting, FxWeaver fxWeaver, NBiometricClient biometricClient) {
        super();
        this.greeting = greeting;
        this.fxWeaver = fxWeaver;
        this.faceViewNode = new FaceViewNode();
        this.faceViewNodeNew = new FaceViewNodeNew();
        this.biometricClient = biometricClient;
    }

    @FXML
    public void initialize() {
        this.paneView.getChildren().add(faceViewNode);
        this.paneView.setAlignment(Pos.CENTER);
        this.paneView1.getChildren().addAll(faceViewNodeNew);
        this.paneView1.setAlignment(Pos.CENTER);
    }

    @FXML
    public void openImageFirst(ActionEvent event) {
        fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fc.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        File file = fc.showOpenDialog(null);
        if (file != null) {
            reference = new NSubject();
            NFace face = new NFace();
            fileName = file.getAbsolutePath();
            face.setFileName(fileName);
            reference.getFaces().add(face);
            faceViewNode.setFace(face);
            updateFacesTools();
            biometricClient.setFacesTemplateSize(NTemplateSize.MEDIUM);
            biometricClient.createTemplate(reference, ATTACHMENT_REFERENCE, templateCreationHandler);
        }
    }

    @FXML
    public void openImageAfter(ActionEvent event) {
        fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fc.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        File file = fc.showOpenDialog(null);
        if (file != null) {
            multipleFaces = new NSubject();
            NFace face = new NFace();
            fileName = file.getAbsolutePath();
            face.setFileName(fileName);

            multipleFaces.getFaces().add(face);

            faceViewNodeNew.setFace(face);

            multipleFaces.setMultipleSubjects(true);

            updateFacesTools();
            biometricClient.setFacesTemplateSize(NTemplateSize.LARGE);
            biometricClient.createTemplate(multipleFaces, ATTACHMENT_MULTIPLE_FACES, templateCreationHandler);
        }
    }

    NSubject getReference() {
        return reference;
    }

    void setReference(NSubject reference) {
        this.reference = reference;
    }

    NSubject getMultipleFaces() {
        return multipleFaces;
    }

    void setMultipleFaces(NSubject multipleFaces) {
        this.multipleFaces = multipleFaces;
    }

    void setScores(String[] scores) {
        faceViewNodeNew.setFaceIds(scores);
    }

    void enrollMultipleFacesSubject() {
        NBiometricTask enrollTask = new NBiometricTask(EnumSet.of(NBiometricOperation.ENROLL));

        // Enroll all faces.
        multipleFaces.setId("firstSubject");
        enrollTask.getSubjects().add(multipleFaces);

        int i = 0;
        for (NSubject subject : multipleFaces.getRelatedSubjects()) {
            subject.setId("relatedSubject" + i);
            i++;
            enrollTask.getSubjects().add(subject);
        }
        biometricClient.performTask(enrollTask, null, enrollHandler);
    }

    private void updateFacesTools() {
        biometricClient.setFacesDetectAllFeaturePoints(true);
    }

    private class TemplateCreationHandler implements CompletionHandler<NBiometricStatus, String> {
        @Override
        public void completed(NBiometricStatus status, String subject) {
            if (status == NBiometricStatus.OK) {
                if (ATTACHMENT_REFERENCE.equals(subject)) {
                    if (getMultipleFaces() != null) {
                        biometricClient.identify(getReference(), null, identificationHandler);
                    }
                } else if (ATTACHMENT_MULTIPLE_FACES.equals(subject)) {
                    enrollMultipleFacesSubject();
                } else {
                    throw new AssertionError("Unknown attachment" + subject);
                }
            } else {
                if (ATTACHMENT_REFERENCE.equals(subject)) {
                    setReference(null);
                } else if (ATTACHMENT_MULTIPLE_FACES.equals(subject)) {
                    setMultipleFaces(null);
                } else {
                    throw new AssertionError("Unknown attachment" + subject);
                }
            }
        }

        @Override
        public void failed(Throwable throwable, String s) {

        }
    }

    private class EnrollHandler implements CompletionHandler<NBiometricTask, Object> {
        @Override
        public void completed(NBiometricTask task, Object attachment) {
            if (task.getStatus() == NBiometricStatus.OK) {
                if (getReference() != null) {
                    biometricClient.identify(getReference(), null, identificationHandler);
                }
            } else {

            }
        }

        @Override
        public void failed(Throwable throwable, Object attachment) {

        }
    }

    private class IdentificationHandler implements CompletionHandler<NBiometricStatus, Object> {
        @Override
        public void completed(NBiometricStatus status, Object attachment) {
            Platform.runLater(() -> {
                if ((status == NBiometricStatus.OK) || (status == NBiometricStatus.MATCH_NOT_FOUND)) {
                    int multipleFacesCount = getMultipleFaces().getFaces().size() + getMultipleFaces().getRelatedSubjects().size();
                    String[] results = new String[multipleFacesCount];

                    for (NMatchingResult result : getReference().getMatchingResults()) {
                        int score = result.getScore();
                        if (result.getId().equals(getMultipleFaces().getId())) {
                            results[0] = String.format("Score: %d(match)", score);
                        } else {
                            for (int j = 0; j < getMultipleFaces().getRelatedSubjects().size(); j++) {
                                if (result.getId().equals(getMultipleFaces().getRelatedSubjects().get(j).getId())) {
                                    results[j + 1] = String.format("Score: %d(match)", score);
                                }
                            }
                        }
                    }

                    //All not matched faces have score 0
                    for (int i = 0; i < results.length; i++) {
                        if (results[i] == null) {
                            results[i] = "Score: 0";
                        }
                    }
                    setScores(results);
                }
            });
        }

        @Override
        public void failed(Throwable throwable, Object o) {

        }
    }
}
