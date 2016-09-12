import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static GpioController instance = GpioFactory.getInstance();
    private static GpioPinDigitalOutput PIN_GREEN =
            instance.provisionDigitalOutputPin(RaspiPin.GPIO_04, PinState.LOW);
    private static GpioPinDigitalInput GPIO_PIN_DIGITAL_INPUT =
            instance.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_UP);

    private static Thread th;
    private static AtomicBoolean works = new AtomicBoolean(false);


    public static void main(String[] args) throws InterruptedException {
        System.out.println("main: " + Thread.currentThread().getName());
        new Main().execute();
        while (true) {
            Thread.sleep(1000);
        }
    }

    private void execute() throws InterruptedException {
        GPIO_PIN_DIGITAL_INPUT.addListener(new HatButton());
    }

    private class HatButton implements GpioPinListenerDigital {

        @Override
        public synchronized void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            if(event.getState().isLow()) {
                if (works.get()) {
                    System.out.println("Interrupt the Toggler: " + Thread.currentThread().getName());
                    th.interrupt();
                } else {
                    System.out.println("Create a new Toggler: " + Thread.currentThread().getName());
                    th = new Thread(new Toggler());
                    th.start();
                }
                works.set(!works.get());
            }
        }

    }

    private class Toggler implements Runnable {

        @Override
        public void run() {
            do {
                PIN_GREEN.toggle();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) { return; }
            } while (!Thread.currentThread().isInterrupted());
        }

    }
}
