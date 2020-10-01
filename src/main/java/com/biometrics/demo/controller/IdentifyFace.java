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
@FxmlView("IdentifyFace.fxml")
public final class IdentifyFace {

    private final FaceViewNodeNew faceView;
    private final NBiometricClient biometricClient;
    private NSubject subject;
    private FileChooser fc;
    private final List<NSubject> subjects;
    private static final List<String> THRESHOLDS = new ArrayList<String>();
    private final TemplateCreationHandler templateCreationHandler = new TemplateCreationHandler();
    private final EnrollHandler enrollHandler = new EnrollHandler();
    private final IdentificationHandler identificationHandler = new IdentificationHandler();
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
    private Label lblTemplateCount;
    @FXML
    private StackPane paneView;
    @FXML
    private TableView<Template> viewTemplate = new TableView<>();

    @FXML
    private ComboBox comboBoxMatchingFarThreshold;

    public IdentifyFace(NBiometricClient biometricClient) {
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
        viewTemplate.getItems().clear();
        File file = fc.showOpenDialog(null);
        if (file != null) {
            viewTemplate.getItems().size();
            subject = null;
            NFace face = null;
            try {
                subject = NSubject.fromFile(file.getAbsolutePath());
                subject.setId(file.getName());
                NSubject.FaceCollection faces = subject.getFaces();
                if (faces.isEmpty()) {
                    subject = null;
                    throw new IllegalArgumentException("Template contains no face records.");
                }
                face = faces.get(0);
                templateCreationHandler.completed(NBiometricStatus.OK, null);
            } catch (UnsupportedOperationException e) {

            }
            if (subject == null) {
                face = new NFace();
                face.setFileName(file.getAbsolutePath());
                subject = new NSubject();
                subject.setId(file.getName());
                subject.getFaces().add(face);
                updateFacesTools();
                biometricClient.createTemplate(subject, null, templateCreationHandler);
            }
            faceView.setFace(face);
        }
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
        NBiometricTask task = null;
        try {
            if (!isSubjectContainFaceTemplate()) {
//                log.error("Subject does not include Template!!!");
                return;
            }
//            log.info("Starting Indenfication on Face MMA.");
            NClusterBiometricConnection conn = (NClusterBiometricConnection) biometricClient.getRemoteConnections().get(0);
//            log.info("Cluster Host: " + conn.getHost());
//            log.info("Cluster Port: " + conn.getPort());
            if ((subject != null) && !subjects.isEmpty()) {
                updateFacesTools();
                task = biometricClient.createTask(EnumSet.of(NBiometricOperation.IDENTIFY), subject);
                for (NSubject s : subjects) {
                    task.getSubjects().add(s);
                }
                biometricClient.performTask(task, null, enrollHandler);
                viewTemplate.getItems().size();
            }
        } catch (Exception ex) {
//            log.error(ex.getMessage());
        }
    }

    @FXML
    public void openTemplate(ActionEvent event) throws IOException {
        List<File> files = fc.showOpenMultipleDialog(null);
        if (files != null) {
            viewTemplate.getItems().size();
            subjects.clear();
            for (File file : files) {
                NSubject s = NSubject.fromFile(file.getAbsolutePath());
                s.setId(file.getName());
                subjects.add(s);
            }
            lblTemplateCount.setText(String.format("Count Template: %s", String.valueOf(subjects.size())));
        }
        updateControls();
    }

    private void updateControls() {
        identify.setDisable(subjects.isEmpty() || (subject == null) || ((subject.getStatus() != NBiometricStatus.OK) && (subject.getStatus() != NBiometricStatus.NONE)));
    }

    private class TemplateCreationHandler implements CompletionHandler<NBiometricStatus, Object> {
        @Override
        public void completed(NBiometricStatus status, Object attachment) {
            Platform.runLater(() -> {
                updateControls();
                if (status != NBiometricStatus.OK) {
                    setSubject(null);
                }
            });
        }

        @Override
        public void failed(Throwable throwable, Object attachment) {
            Platform.runLater(() -> {
                updateControls();
            });
        }
    }

    private class EnrollHandler implements CompletionHandler<NBiometricTask, Object> {
        @Override
        public void completed(NBiometricTask task, Object attachment) {
            if (task.getStatus() == NBiometricStatus.OK) {
                biometricClient.identify(getSubject(), null, identificationHandler);
            }
        }

        @Override
        public void failed(Throwable throwable, Object attachment) {
            Platform.runLater(() -> {
                updateControls();
            });
        }
    }

    private class IdentificationHandler implements CompletionHandler<NBiometricStatus, Object> {
        @Override
        public void completed(NBiometricStatus status, Object attachment) {
            Platform.runLater(() -> {
                if ((status == NBiometricStatus.OK) || (status == NBiometricStatus.MATCH_NOT_FOUND)) {
                    for (NSubject s : getSubjects()) {
                        boolean match = false;
                        for (NMatchingResult result : getSubject().getMatchingResults()) {
                            if (s.getId().equals(result.getId())) {
                                match = true;
                                prependIdentifyResult(result.getId(), result.getScore());
                                break;
                            }
                        }
                        if (!match) {
                            appendIdentifyResult(s.getId(), 0);
                        }
                    }
                } else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Error");
                        alert.setHeaderText("Identification failed: " + status);
                        alert.showAndWait();
                    });
                }
            });
        }

        @Override
        public void failed(Throwable throwable, Object attachment) {
            Platform.runLater(() -> {
                updateControls();
            });
        }
    }

    void appendIdentifyResult(String nameNew, int scoreNew) {
        templatesData.addAll(new Template(nameNew, scoreNew));
        viewTemplate.setItems(templatesData);
    }

    void prependIdentifyResult(String nameNew, int scoreNew) {
        templatesData.addAll(new Template(nameNew, scoreNew));
        viewTemplate.setItems(templatesData);
    }

    NSubject getSubject() {
        return subject;
    }

    void setSubject(NSubject subject) {
        this.subject = subject;
    }

    List<NSubject> getSubjects() {
        return subjects;
    }

    private boolean isSubjectContainFaceTemplate() {
        if (subject.getTemplate() != null && subject.getTemplate().getSize() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
