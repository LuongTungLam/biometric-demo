package com.biometrics.demo.controller;

import com.biometrics.demo.application.FaceTools;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.swing.NFaceView;
import com.neurotec.images.NImage;
import com.neurotec.licensing.NLicense;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
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
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@FxmlView("MainController.fxml")
public class MainController  {
    private final FxWeaver fxWeaver;
    private final String greeting;
    private NImage image;
    private File file;
    private NFaceView faceView;
    @FXML
    private SwingNode swingNode;
    @FXML
    private Button helloButton;
    @FXML
    private Pane panelHeader;
    @FXML
    private Pane paneDetect;
    @FXML
    private Pane paneview;
    @FXML
    private StackPane imageView;
    @FXML
    private ComboBox comboBoxMaxRollAngleDeviation;
    @FXML
    private ComboBox comboBoxMaxYawAngleDeviation;
    private final FaceDetectionHandler faceDetectionHandler = new FaceDetectionHandler();
    private final ObjectProperty<NFace> face = new SimpleObjectProperty<>();

    private JScrollPane scrollPane;

    public MainController(@Value("${spring.application.demo.greeting}") String greeting, FxWeaver fxWeaver) {
        super();
        this.greeting = greeting;
        this.fxWeaver = fxWeaver;
        this.swingNode = new FaceViewNode();

    }
    public void openFile() throws IOException {
        paneview.getChildren().clear();
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fc.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        file = fc.showOpenDialog(null);
        if (file != null) {
            image = NImage.fromFile(file.getAbsolutePath());
            NFace face = new NFace();
            face.setImage(image);
//            detectFace(image);
            ((FaceViewNode)this.swingNode).setFace(face);
//            createAndSetSwingScrollPane(this.swingNode);
        }
    }
    private void createAndSetSwingScrollPane(final SwingNode swingNode) {
    }

    public final NFace getFace() {
        return face.get();
    }

    public final void setFace(final NFace value) {
        face.setValue(value);
        SwingUtilities.invokeLater(() -> {
            faceView.setFace(value);
        });
    }

    public ObjectProperty<NFace> faceProperty() {
        return face;
    }

    private void detectFace(NImage faceImage) {
        NBiometricClient client = FaceTools.getInstance().getClient();
        updateFacesTools();
        client.detectFaces(faceImage, null, faceDetectionHandler);
    }

    private void updateComboBoxes() {
        updateRollAngleDeviationComboBox();
        updateYawAngleDeviationComboBox();
    }

    private void updateYawAngleDeviationComboBox() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) comboBoxMaxRollAngleDeviation.getSelectionModel().getSelectedItem();
        Float item = FaceTools.getInstance().getClient().getFacesMaximalRoll();
        updateComboBoxesValues(model, item, 0, 180);
    }

    private void updateRollAngleDeviationComboBox() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) comboBoxMaxYawAngleDeviation.getSelectionModel().getSelectedItem();
        Float item = FaceTools.getInstance().getClient().getFacesMaximalRoll();
        updateComboBoxesValues(model, item, 0, 180);
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


    public void updateFacesTools() {
        NBiometricClient client = FaceTools.getInstance().getClient();
        client.reset();
        ;
        boolean faceSegmentsDetectionActivated;
        try {
            faceSegmentsDetectionActivated = NLicense.isComponentActivated("Biometrics.FaceSegmentsDetection");
        } catch (IOException e) {
            e.printStackTrace();
            faceSegmentsDetectionActivated = false;
        }
        client.setFacesDetectAllFeaturePoints(faceSegmentsDetectionActivated);
        client.setFacesDetectBaseFeaturePoints(faceSegmentsDetectionActivated);
    }





    @FXML
    public void initialize() {
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

    private class FaceDetectionHandler implements CompletionHandler<NFace, Object> {

        @Override
        public void completed(final NFace result, final Object attachment) {

        }

        @Override
        public void failed(final Throwable th, final Object attachment) {

        }

    }
}
