package com.gradle.backend;

import com.fazecast.jSerialComm.*;
import org.junit.runner.OrderWith;

import java.io.IOException;
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

    private boolean connectToESP32() {
        String os = System.getProperty("os.name");
        ArrayList<String> portNames = new ArrayList<>();
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
            return false;
        }
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        comPort.setBaudRate(57600);
        return true;
    }

    private void connectToUARTHelper() {
        boolean attemptToConnect = false;
        while (true) {
            if (connectToESP32()) {
                System.out.println("Connected to " + uart.comPort);
                createReadUARTThread().start();
                break;
            }
            if (!attemptToConnect) {
                System.err.println("ESP32 Microcontroller is not connected RPi4." +
                        "\nWaiting on connection.");
                attemptToConnect = true;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                System.err.println("InterruptedException: Thread interrupted initializing" +
                        "UART properties.");
            }
        }
    }

    public Thread createConnectToUARTThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                connectToUARTHelper();
            }
        }, "ConnectToUARTHelper Thread");
    }

    public UART() {
        uart = this;
    }

    public void initializeDataSet() {
        dataMap = new HashMap<String, Object>();
        for (Map.Entry<String, Temperature> entry : Temperature.temperatureMap.entrySet()) {
            dataMap.put(entry.getKey(), entry.getValue());
        }
    }

    public void updateReader(String parsed) {
        try {
            String[] parsedList = parsed.split(":");
            String label = "";
            String value = "";
            try {
                label = parsedList[0].strip();
                value = parsedList[1].strip();
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                System.err.println("ArrayIndexOutOfBoundsException: high stream" +
                        " not parseable characters detected and filtered out.");
                parsed = "";
            }

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
        System.out.println(parsed);
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

    private void restartESP32() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.err.println("InterruptedException: Thread to restart ESP32" +
                    " interrupted while sleeping.");
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

        int consecutiveSleepCount = 0;
        int bufferCount = 0;
        boolean attemptReconnection = false;
        while (true) {

            try {
                bufferCount = in.available();
            } catch (IOException ioe) {
                if (attemptReconnection == false) {
                    System.err.println("IOException: Error reading buffer size" +
                            " of inputStream.\n" +
                            "Attempting to reconnect to: " + uart.comPort);
                    attemptReconnection = true;
                } else if (attemptReconnection == true) {
                    if (connectToESP32()) {
                        attemptReconnection = false;
                    }
                }
                try {
                    Thread.sleep(100);
                } catch(InterruptedException ie) {
                    ie.printStackTrace();
                }
            }

            if (bufferCount == 0) {
                ++consecutiveSleepCount;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ioe) {
                    System.err.println("InterruptedException: Thread interrupted while \"sleeping\"." +
                            " Thread is waiting for input stream to reinitialize.");
                }
                if (consecutiveSleepCount == 20) { // 2000 milliseconds of no response
                    System.err.println("UART Bus silent for more than 2000 milliseconds." +
                            "\nRestarting ESP32.");
                    restartESP32();
                }
                continue;
            }
            boolean parseableAttempt = false;
            while (bufferCount-- > 0) {
                consecutiveSleepCount = 0;
                try {
                    parse = (char) in.read();
                    if (!isParseableLetterOrDigit(parse)) {
                        if (!parseableAttempt) {
                            System.err.println("InputStream character is not parseable." +
                                    "\nESP32 UART bus corrupted. Restarting ESP32");
                            restartESP32();
                            parseableAttempt = true;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("IOException: Error reading a character from the inputStream.");
                }

                if (parse == ';') {
                    parseableAttempt = false;
                    updateReader(parsed.toString());
                    parsed = new StringBuffer("");
                    continue;
                }

                parsed.append(parse);
            }
        }
    }

    public boolean isParseableLetterOrDigit(Character character) {
        return ((character >= 'a' && character <= 'z') ||
                (character >= 'A' && character <= 'Z') ||
                (character >= '0' && character <= '9') ||
                (character == ':') || (character == ';') ||
                (character == '.') || (character == '\n'));
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

