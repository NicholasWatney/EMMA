package com.gradle.backend;

import javax.swing.*;
import java.util.ArrayList;

public class Light {

    private JButton lightLabel;
    private static int lightCount = 0;
    private static ArrayList<Light> lightList = new ArrayList<>();

    public Light() {
        lightCount++;
        lightLabel = new JButton("Light" + lightCount);
        lightList.add(this);
    }

    public JButton getLightLabel() {
        return lightLabel;
    }
}
