package com.gradle.backend;

import com.diozero.api.DigitalOutputDevice;
import org.tinylog.Logger;

import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.LED;
import com.diozero.sbc.DeviceFactoryHelper;
import com.diozero.util.Diozero;
import com.diozero.util.SleepUtil;

public class Pi {

    DigitalOutputDevice resetPin;

    public Pi() {
        resetPin = new DigitalOutputDevice(17);
        resetPin.on();
    }

    public static void main(String[] args) {
        Pi pi = new Pi();
        while (true) {
            pi.resetPin.off();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pi.resetPin.on();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}