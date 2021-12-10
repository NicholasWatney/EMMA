package com.gradle.backend;

import com.diozero.api.DigitalInputDevice;
import com.diozero.api.DigitalOutputDevice;
import org.tinylog.Logger;

import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.LED;
import com.diozero.sbc.DeviceFactoryHelper;
import com.diozero.util.Diozero;
import com.diozero.util.SleepUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.diozero.api.GpioEventTrigger.BOTH;
import static com.diozero.api.GpioEventTrigger.RISING;
import static com.diozero.api.GpioPullUpDown.NONE;

public class Pi {

    public static DigitalOutputDevice resetPin;

    public static DigitalInputDevice ack;
    public static DigitalOutputDevice bit1;
    public static DigitalOutputDevice bit2;
    public static DigitalOutputDevice bit3;

    public static boolean ack_received;

    public static boolean drive(int value) {

        if (value >= 4) {
            bit3.on();
            value -= 4;
        }

        if (value >= 2) {
            bit2.on();
            value -= 2;
        }

        if (value >= 1) {
            bit1.on();
            value -= 1;
        }

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//            if (ack.isActive()) {
//                break;
//            }
//        }
        bit3.off();
        bit2.off();
        bit1.off();

        return true;
    }

    public static Thread createPiTestThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    System.out.println("ON!");
                    bit1.on();
                    bit2.on();
                    bit3.on();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("OFF!");
                    bit1.off();
                    bit2.off();
                    bit3.off();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        }, "Thread");

    }

    public Pi() {

        ack_received = false;

        resetPin = new DigitalOutputDevice(17);
        resetPin.on();

        ack = new DigitalInputDevice(27, NONE, RISING);

        bit1 = new DigitalOutputDevice(22);
        bit1.off();

        bit2 = new DigitalOutputDevice(23);
        bit2.off();

        bit3 = new DigitalOutputDevice(24);
        bit3.off();
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