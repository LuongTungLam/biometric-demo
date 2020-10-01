package com.biometrics.demo.controller;

import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.swing.NFaceView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javafx.embed.swing.SwingNode;

import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class FaceViewNodeNew extends SwingNode {
    private final ObjectProperty<NFace> face = new SimpleObjectProperty();
    private NFaceView faceView;
    private String[] faceIds = new String[0];

    public FaceViewNodeNew() {
        SwingUtilities.invokeLater(() -> {
            this.faceView = new NFaceView();
            this.faceView.setFace((NFace)null);
            this.faceView.setAutofit(true);
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setVerticalScrollBarPolicy(20);
            scrollPane.setHorizontalScrollBarPolicy(30);
            scrollPane.setMinimumSize(new Dimension(200, 200));
            scrollPane.setPreferredSize(new Dimension(300, 300));
            scrollPane.setViewportView(this.faceView);
            this.setContent(scrollPane);
        });
    }

    public final NFace getFace() {
        return (NFace)this.face.get();
    }

    public final void setFace(NFace value) {
        this.face.setValue(value);
        SwingUtilities.invokeLater(() -> {
            this.faceView.setFace(value);
        });
    }

    public ObjectProperty<NFace> faceProperty() {
        return this.face;
    }

    public String[] getFaceIds() {
        return (String[])this.faceIds.clone();
    }

    public void setFaceIds(String[] faceIds) {
        String[] oldFaceIds = this.getFaceIds();
        if (faceIds == null) {
            this.faceIds = new String[0];
        } else {
            this.faceIds = (String[])faceIds.clone();
        }
        pcs.firePropertyChange("faceIds", oldFaceIds, faceIds);
//        if (!Arrays.equals(this.faceIds,oldFaceIds)){
//            this.
//        }
    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener){
        pcs.addPropertyChangeListener(listener);
    }
}
