package com.gradle.backend;

import com.fazecast.jSerialComm.*;
import com.gradle.swing.MainGUI;
import org.junit.runner.OrderWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;

public class UART {

    public Pi pi;
    public SerialPort comPort;
    public Map<String, Object> dataMap;
    public static UART uart;
    public static String os;
    public static boolean resetCommand = false;

    private boolean connectToESP32() {
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

    private void updateConsole(StringBuilder message) {
        MainGUI.mainGUI.updateConsole(message);
    }

    private void updateConsole(String message) {
        MainGUI.mainGUI.updateConsole(message);
    }

    private void connectToUARTHelper() {
        boolean attemptToConnect = false;
        while (true) {
            if (connectToESP32()) {
                updateConsole("Connected to " + uart.comPort);
                createReadUARTThread().start();
                break;
            }
            if (!attemptToConnect) {
                updateConsole("ESP32 Microcontroller is not connected RPi4. Waiting on connection...");
                attemptToConnect = true;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                updateConsole("InterruptedException: Thread interrupted initializing" +
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
        os = System.getProperty("os.name");
        if (os.equals("Linux")) {
            pi = new Pi();
        } else {
            pi = null;
        }
    }

    public void initializeDataSet() {
        dataMap = new HashMap<String, Object>();
        for (Map.Entry<String, Temperature> entry : Temperature.temperatureMap.entrySet()) {
            dataMap.put(entry.getKey(), entry.getValue());
        }
    }

    public void updateReader(String parsed) {
        String[] parsedList = parsed.split(":");

        if (parsedList.length != 2) {
            return;
        }

        String label = "";
        String value = "";
        label = parsedList[0].strip();
        value = parsedList[1].strip();
        parsed = "";

        Object sensor = dataMap.get(label);
        if (sensor != null) {
            if ("com.gradle.backend.Temperature".equals(sensor.getClass().getName())) {
                Temperature temperatureSensor = (Temperature) sensor;
                try {
                    Double getTemperatureValue = Double.parseDouble(value);
                } catch (NumberFormatException nfe) {
                    return;
                }
                temperatureSensor.setTemperatureReading(Double.parseDouble(value));
                temperatureSensor.addTemperatureHistory();

                temperatureSensor.updateAvgFps();
                temperatureSensor.updateJLabel();
                temperatureSensor.updatePrecautionLabel();
            } else if ("com.gradle.backend.Current".equals(sensor.getClass().getName())) {
                Current currentSensor = (Current) sensor;

                try {
                    Double getCurrentValue = Double.parseDouble(value);
                } catch (NumberFormatException nfe) {
                    return;
                }
                currentSensor.setCurrentReading(Double.parseDouble(value));
                currentSensor.addCurrentHistory();

                currentSensor.updateAvgFps();
                currentSensor.updateJLabel();
                currentSensor.updatePrecautionLabel();
            } else if ("com.gradle.backend.Voltage".equals(sensor.getClass().getName())) {
                Voltage voltageSensor = (Voltage) sensor;

                try {
                    Double getVoltageValue = Double.parseDouble(value);
                } catch (NumberFormatException nfe) {
                    return;
                }
                voltageSensor.setVoltageReading(Double.parseDouble(value));
                voltageSensor.addVoltageHistory();

                voltageSensor.updateAvgFps();
                voltageSensor.updateJLabel();
                voltageSensor.updatePrecautionLabel();
            }
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

    public void restartESP32() {
        try {
            pi.resetPin.off();
            Thread.sleep(100);
            pi.resetPin.on();
            Thread.sleep(200);

            comPort.closePort();
            if (!comPort.openPort()) {
                return;
            }

        } catch (InterruptedException e) {
            updateConsole("InterruptedException: Thread to restart ESP32" +
                    " interrupted while sleeping.");
        }
    }

    public void nullifyEverything() {
        for (String key : dataMap.keySet()) {
            Object sensor = dataMap.get(key);
            if ("com.gradle.backend.Temperature".equals(sensor.getClass().getName())) {
                Temperature temperatureSensor = (Temperature) sensor;
                temperatureSensor.nullifyFps();
//                temperatureSensor.nullifyTemp();
                temperatureSensor.updateJLabel();
                temperatureSensor.updatePrecautionLabel();
            } else if ("com.gradle.backend.Current".equals(sensor.getClass().getName())) {
                Current currentSensor = (Current) sensor;
                currentSensor.nullifyFps();
                currentSensor.updateJLabel();
                currentSensor.updatePrecautionLabel();
            } else if ("com.gradle.backend.Voltage".equals(sensor.getClass().getName())) {
                Voltage voltageSensor = (Voltage) sensor;
                voltageSensor.nullifyFps();
                voltageSensor.updateJLabel();
                voltageSensor.updatePrecautionLabel();
            }
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
        int connectCount = 0;
        int parseableCount = 0;
        while (true) {

            try {
                bufferCount = in.available();
            } catch (IOException ioe) {

                ++connectCount;
                if (connectCount == 1) {
                    nullifyEverything();
                    updateConsole("Attempting to reconnect to: " + uart.comPort + "...");
                }

                if (connectCount > 0) {
                    connectToESP32();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }

                continue;
            }

            if (connectCount > 0) {
                connectCount = 0;
                updateConsole("Connected to: " + uart.comPort + " successfully!");
            }

            if (bufferCount <= 0) {

                ++consecutiveSleepCount;
                if (consecutiveSleepCount == 20) {
                    updateConsole("Attempting to restart ESP32...");
                    restartESP32();
                    continue;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                continue;
            }

            while (bufferCount-- > 0) {

                if (consecutiveSleepCount >= 20) {
                    updateConsole("Restarted ESP32 successfully!");
                }

                if (consecutiveSleepCount > 0) {
                    consecutiveSleepCount = 0;
                }

                try {
                    parse = (char) in.read();
                } catch (SerialPortIOException spioe) {
                    continue;
                } catch (SerialPortTimeoutException spte) {
                    continue;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                if (!isParseableLetterOrDigit(parse)) {
                    ++parseableCount;

                    if (parseableCount == 1) {
                        if (resetCommand) {
                            updateConsole("Restart Requested. Restarting ESP32...");
                            resetCommand = false;
                        } else {
                            updateConsole("Corrupted Serial Bus. Restarting ESP32...");
                        }
                    }

                    if (parseableCount > 0) {
                        restartESP32();
                    }

                    continue;
                }

                if (parseableCount > 0) {
                    parseableCount = 0;
                    updateConsole("Restarted ESP32 Successfully!");
                }

                if (parse == ';') {
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
                (character == '.') || (character == '\n') ||
                (character == '-'));
    }



    public Thread createReadUARTThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                readUART();
            }
        }, "UART Thread");
    }

}

