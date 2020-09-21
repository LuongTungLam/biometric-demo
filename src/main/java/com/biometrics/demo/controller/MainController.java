package com.biometrics.demo.controller;

import com.biometrics.demo.application.PrimaryStageInitializer;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.images.NImage;
import com.neurotec.io.NFile;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.kbjung.abis.neurotec.biometrics.fx.FaceViewNode;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@Log4j2
@Component
@FxmlView("MainController.fxml")
public class MainController {
    private final FxWeaver fxWeaver;
    private final String greeting;
    private final FaceViewNode faceViewNode;
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
    private ComboBox comboBoxMaxRollAngleDeviation;
    @FXML
    private ComboBox comboBoxMaxYawAngleDeviation;

    public MainController(@Value("${spring.application.demo.greeting}") String greeting, FxWeaver fxWeaver, NBiometricClient biometricClient) {
        super();
        this.greeting = greeting;
        this.fxWeaver = fxWeaver;
        this.faceViewNode = new FaceViewNode();
        this.biometricClient = biometricClient;

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
            NImage image = NImage.fromFile(fileName);
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
            NBiometricTask task = biometricClient.createTask(EnumSet.of(NBiometricOperation.DETECT_SEGMENTS), subject);
            biometricClient.performTask(task);
            if (task.getStatus() == NBiometricStatus.OK) {
                NBiometricStatus status = biometricClient.createTemplate(subject);
                if (status == NBiometricStatus.OK) {
                    File file = fc.showSaveDialog(null);
                   if (file != null) {
                    NFile.writeAllBytes(fileName, subject.getTemplateBuffer());
                   }
                }
            }
        }
    }

    private void updateComboBoxes() {
        updateRollAngleDeviationComboBox();
        updateYawAngleDeviationComboBox();
    }

    private void updateYawAngleDeviationComboBox() {
    }

    private void updateRollAngleDeviationComboBox() {
    }

    private void updateComboBoxesValues(DefaultComboBoxModel model, Float item, int min, int max) {
        List<Float> items = new ArrayList<Float>();
        for (float i = min; i <= max; i += 15) {
            items.add((i));
        }

        if (!items.contains(item)) {
            items.add(item);
        }

        Collections.sort(items);
        for (int i = 0; i != items.size(); i++) {
            model.addElement(items.get(i));
        }
        model.setSelectedItem(item);
    }

    private void clear() {
        paneView.getChildren().clear();
    }
}
