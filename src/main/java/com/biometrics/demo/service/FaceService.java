package com.biometrics.demo.service;

import com.biometrics.demo.BiometricsUiApplication;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.client.NClusterBiometricConnection;
import com.neurotec.images.NImage;
import com.neurotec.io.NStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;


@Slf4j
@Data
@Service
public class FaceService {
    private final NBiometricClient faceClient;

    private final NSubject subject;

    @Autowired
    public FaceService(NBiometricClient faceClient) {
        this.faceClient = faceClient;
        subject = new NSubject();

    }

    public NSubject getSubject() {
        return subject;
    }
    public void identifyOnServer(String id) {
        NBiometricTask task = null;
        Logger log = LoggerFactory.getLogger(BiometricsUiApplication.class);
        try {
            if (!isSubjectContainFaceTemplate()) {
                log.error("Subject does not include Template!!!");
                return;
            }
            log.debug("Starting Indenfication on Face MMA.");
            NClusterBiometricConnection conn =
                    (NClusterBiometricConnection) faceClient.getRemoteConnections().get(0);
            log.debug("Cluster Host: " + conn.getHost());
            log.debug("Cluster Port: " + conn.getPort());

            subject.setId(id);

            task = faceClient.createTask(
                    EnumSet.of(NBiometricOperation.IDENTIFY),
                    subject);
            faceClient.performTask(task);

            if (task.getStatus() != NBiometricStatus.OK) {
                log.error(
                        "[id: " +
                                subject.getId() +
                                "] Identification was unsuccessful. Status: " +
                                task.getStatus());
            }
            log.debug("[id: " +
                    subject.getId() +
                    "] Identification was successful.");
            for(NMatchingResult matchingResult : subject.getMatchingResults()) {
                log.debug("Matched with ID: [" + matchingResult.getId() + "], with score " + matchingResult.getScore());
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            if (task != null) task.dispose();
        }
    }

    private boolean isSubjectContainFaceTemplate() {
        if (subject.getTemplate() != null
                && subject.getTemplate().getSize() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
