package com.biometrics.demo.controller;

import com.kbjung.abis.neurotec.biometrics.utils.Utils;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.client.NClusterBiometricConnection;
import com.neurotec.util.concurrent.CompletionHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import lombok.extern.log4j.Log4j2;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Log4j2
@Component
@FxmlView("IdentifyOnServer.fxml")
public class IdentifyOnServer {
    private final FaceViewNodeNew faceView;
    private final NBiometricClient biometricClient;
    private NSubject subject;
    private FileChooser fc;
    private final List<NSubject> subjects;
    private static final List<String> THRESHOLDS = new ArrayList<String>();
    private final ObservableList<Template> templatesData = FXCollections.observableArrayList();

    static {
        THRESHOLDS.add("1%");
        THRESHOLDS.add("0.1%");
        THRESHOLDS.add("0.01%");
        THRESHOLDS.add("0.001%");
    }

    @FXML
    private Button openTemplate, identify, openFile;
    @FXML
    private Label lblTemplateCount, lblHeader;
    @FXML
    private StackPane paneView;
    @FXML
    private TableView<Template> viewTemplate = new TableView<>();

    @FXML
    private ComboBox comboBoxMatchingFarThreshold;

    public IdentifyOnServer(NBiometricClient biometricClient) {
        super();
        this.biometricClient = biometricClient;
        this.faceView = new FaceViewNodeNew();
        subjects = new ArrayList<NSubject>();
        fc = new FileChooser();
    }

    @FXML
    public void initialize() {
        comboBoxMatchingFarThreshold.getItems().addAll(THRESHOLDS);
        comboBoxMatchingFarThreshold.getSelectionModel().select(2);
        this.paneView.getChildren().add(faceView);
        this.paneView.setAlignment(Pos.CENTER);
        TableColumn<Template, String> name = new TableColumn<>("Name");
        name.setMinWidth(400);
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Template, Integer> score = new TableColumn<>("Score");
        score.setCellValueFactory(new PropertyValueFactory<>("score"));
        score.setMinWidth(400);
        viewTemplate.getColumns().addAll(name, score);
        identify.setDisable(true);
    }

    @FXML
    public void openFile(ActionEvent event) throws IOException {
        File file = fc.showOpenDialog(null);
        if (file != null) {
            NFace face = new NFace();
            face.setFileName(file.getAbsolutePath());
            subject = new NSubject();
            subject.setId(file.getName());
            subject.getFaces().add(face);
            updateFacesTools();
            biometricClient.createTemplate(subject);
            this.faceView.setFace(face);
        }
        updateControls();
    }

    private void updateControls() {
        identify.setDisable((subject == null) && (subject.getStatus() != NBiometricStatus.OK) && (subject.getStatus() == NBiometricStatus.NONE));
    }

    private void updateFacesTools() {
        try {
            biometricClient.setMatchingThreshold(Utils.matchingThresholdFromString((comboBoxMatchingFarThreshold.getSelectionModel().getSelectedItem().toString())));
        } catch (ParseException e) {
            e.printStackTrace();
            biometricClient.setMatchingThreshold(biometricClient.getMatchingThreshold());
            comboBoxMatchingFarThreshold.getSelectionModel().select(biometricClient.getMaximalThreadCount());
        }
    }

    @FXML
    public void identify() {
        try {
            if (!isSubjectContainFaceTemplate()) {
//                log.error("Subject does not include Template!!!");
                return;
            }
//            log.debug("Starting Indenfication on Face MMA.");
            NClusterBiometricConnection conn = (NClusterBiometricConnection) biometricClient.getRemoteConnections().get(0);
//            log.debug("Cluster Host: " + conn.getHost());
//            log.debug("Cluster Port: " + conn.getPort());
            NBiometricTask task = biometricClient.createTask(EnumSet.of(NBiometricOperation.IDENTIFY), subject);
            for (NSubject s : subjects) {
                task.getSubjects().add(s);
            }
            biometricClient.performTask(task);
            if (task.getStatus() != NBiometricStatus.OK) {
                lblHeader.setText("Identification was unsuccessful. Status: " + task.getStatus());
            } else {
                lblHeader.setText("Identification was successful. Status: " + task.getStatus());
            }
            String mes = "";
            for (NMatchingResult matchingResult : subject.getMatchingResults()) {
                if(matchingResult.getId() != null && !matchingResult.getId().isEmpty()){
                    mes += "Matched with ID: [" + matchingResult.getId() + "], with score " + matchingResult.getScore()+"\n";
                }else {
                    mes = "Not Found.";
                }
            }
            if (mes.length() == 0) {
                return;
            } else {
                String finalMes = mes;
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Identify On Server");
                    alert.setHeaderText(finalMes);
                    alert.showAndWait();
                });
            }
        } catch (Exception ex) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Identify On Server");
                alert.setHeaderText(ex.getMessage());
                alert.showAndWait();
            });
        }
    }

    private boolean isSubjectContainFaceTemplate() {
        if (subject.getTemplate() != null && subject.getTemplate().getSize() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
