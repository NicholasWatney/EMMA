package com.gradle.backend;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Light {

    private JButton lightLabel;
    private static int lightCount = 0;
    private int lightNumber;
    private boolean state;
    private static ArrayList<Light> lightList = new ArrayList<>();

    public Light() {
        lightNumber = ++lightCount;
        state = false;
        lightLabel = new JButton("Light" + lightCount);
        lightLabel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                state = !state;
                UART.writeUART("L" + lightNumber + ":" + state + ";");
            }
        });
        lightList.add(this);
    }

    public JButton getLightLabel() {
        return lightLabel;
    }
}
