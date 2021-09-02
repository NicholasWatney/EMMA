import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Test {

    public static void main(String[] args) {
        int speed = 20;
        int delay = 1000;
        Timer timer;
        timer = new Timer(speed, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println(running());
            }
        });
        timer.setInitialDelay(delay);
        timer.setRepeats(true);
        timer.start();
        while (true) {}
    }

    public static long oldTime;
    public static long newTime;
    public static long runningTime;

    private static long difference() {
        if (oldTime == -1) {
            return 0;
        } else {
            newTime = System.currentTimeMillis();
            long difference = newTime - oldTime;
            oldTime = newTime;
            return difference;
        }
    }

    private static long running() {
        runningTime += difference();
        if (runningTime > 1000) {
            runningTime = runningTime % 1000;
        }
        return runningTime;
    }
}