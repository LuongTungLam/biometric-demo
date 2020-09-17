package com.biometrics.demo.controller;

import com.biometrics.demo.controller.MainController;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.swing.NFaceView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import javafx.embed.swing.SwingNode;

public class FaceViewNode extends SwingNode{
    private final ObjectProperty<NFace> face = new SimpleObjectProperty<>();
    private NFaceView faceView;
    public FaceViewNode() {
        SwingUtilities.invokeLater(() -> {
            faceView = new NFaceView();
            faceView.setFace(null);
            faceView.setAutofit(true);
            final SwingNode swingNode = new SwingNode();
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setMinimumSize(new Dimension(200, 200));
            scrollPane.setPreferredSize(new Dimension(300, 300));
            scrollPane.setViewportView(faceView);
            swingNode.setContent(scrollPane);
        });
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
}
