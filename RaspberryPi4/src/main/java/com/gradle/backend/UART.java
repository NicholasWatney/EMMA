package com.gradle.backend;

import com.fazecast.jSerialComm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class UART {

    public SerialPort comPort;
    public Map<String, Object> dataMap;

    public UART() {

        String os = System.getProperty("os.name");
        String portName = "";
        if (os.equals("Mac OS X")) {
            portName = "cu.SLAB_USBtoUART";
        } else if (os.equals("Linux")) {
            portName = "ttyUSB0";
        }

        for (SerialPort port : SerialPort.getCommPorts()) {
            if (port.getSystemPortName().equals(portName)) {
                comPort = port;
                break;
            }
        }

        initializeDataSet();
        if (comPort == null || !comPort.openPort()) {
            return;
        }
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
    }

    public void initializeDataSet() {
        dataMap = new HashMap<String, Object>();
        for (Map.Entry<String, Temperature> entry : Temperature.temperatureMap.entrySet()) {
            dataMap.put(entry.getKey(), entry.getValue());
        }
    }

    public void updateReader(String parsed) {
        System.out.println(parsed); //
        try {
            String[] parsedList = parsed.split(":");
            String label = parsedList[0].strip();
            String value = parsedList[1].strip();

            Object sensor = dataMap.get(label);
            if (sensor != null) {
                if ("com.gradle.backend.Temperature".equals(sensor.getClass().getName())) {
                    Temperature temperatureSensor = (Temperature) sensor;
                    temperatureSensor.setTemperatureReading(Double.parseDouble(value));
                    temperatureSensor.updateFPS();
                    temperatureSensor.updateJLabel();
                }
            }
        } catch (Exception e) {
        }
    }

    public void readUART() {
        if (comPort == null || !comPort.openPort()) {
            return;
        }
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        StringBuffer parsed = new StringBuffer();
        InputStream in = comPort.getInputStream();

        try {
            while (true) {
                char parse = (char) in.read();
                if (parse == ';') {
                    updateReader(parsed.toString());
                    parsed = new StringBuffer("");
                    continue;
                }
                parsed.append(parse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public Thread createReadUARTThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                readUART();
            }
        }, "UART Thread");
    }

    public static void main(String[] args) {
        UART uart = new UART();
        Thread thread = uart.createReadUARTThread();
        thread.start();
    }

}

