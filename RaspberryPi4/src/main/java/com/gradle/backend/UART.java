package com.gradle.backend;

import com.fazecast.jSerialComm.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;

public class UART {

    public SerialPort comPort;
    public Map<String, Object> dataMap;
    public static UART uart;

    public UART() {
        uart = this;
        String os = System.getProperty("os.name");
        ArrayList<String> portNames = new ArrayList<String>();
        if (os.equals("Mac OS X")) {
            portNames.add("cu.SLAB_USBtoUART");
        } else if (os.equals("Linux")) {
            portNames.add("ttyUSB0");
            portNames.add("ttyUSB1");
        }

        for (String portName : portNames) {

            if (comPort != null) {
                break;
            }

            for (SerialPort port : SerialPort.getCommPorts()) {
                if (port.getSystemPortName().equals(portName)) {
                    comPort = port;
                    break;
                }
            }
        }

        initializeDataSet();
        if (comPort == null || !comPort.openPort()) {
            return;
        }
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        comPort.setBaudRate(57600);
    }

    public void initializeDataSet() {
        dataMap = new HashMap<String, Object>();
        for (Map.Entry<String, Temperature> entry : Temperature.temperatureMap.entrySet()) {
            dataMap.put(entry.getKey(), entry.getValue());
        }
    }

    public void updateReader(String parsed) {
        try {
            System.out.println(parsed);
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
            e.printStackTrace();
        }
    }


    static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    public static void writeUART(String message) {
        SerialPort comPort = uart.comPort;
        if (comPort == null) {
            return;
        }
        OutputStream out = comPort.getOutputStream();
        try {
            out.write(toBytes(message.toCharArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readUART() {
        if (comPort == null || !comPort.openPort()) {
            return;
        }
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        StringBuffer parsed = new StringBuffer();
        InputStream in = comPort.getInputStream();

        char parse = ' ';
        try {
            while (true) {
                try {
                    parse = (char) in.read();
                    if (parse == ';') {
                        updateReader(parsed.toString());
                        parsed = new StringBuffer("");
                        continue;
                    }
                } catch (SerialPortTimeoutException spte) {}
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

