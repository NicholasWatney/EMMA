

import com.fazecast.jSerialComm.*;

public class Test {

    public static void main(String[] args) {

        for (SerialPort port : SerialPort.getCommPorts()) {
            System.out.println(port.getSystemPortName());
        }
    }
}