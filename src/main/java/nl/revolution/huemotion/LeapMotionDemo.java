package nl.revolution.huemotion;

import com.leapmotion.leap.*;
import com.philips.lighting.model.PHLight;
import nl.revolution.huemotion.api.BridgeAuth;
import nl.revolution.huemotion.api.HueAPI;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LeapMotionDemo {

    private HueAPI api;

    public static void main(String[] args) {
        // to use, set vm arg to point to os native libs:
        // -Djava.library.path=/Users/bertjan/IdeaProjects/sandbox/huemotion/src/main/resources/lib/osx
        new LeapMotionDemo().doTest();
    }

    public void doTest() {

        // init hue
        api = new HueAPI();

        api.connectToBridge(BridgeAuth.BRIDGE_ID, BridgeAuth.BRIDGE_USERNAME);
        log("Connected to bridge.");

        api.getReachableLights().forEach(light -> {
            log("Found reachable light: " + light.getName());
            api.turnOff(light);
        });

        // Create a sample listener and controller
        SampleListener listener = new SampleListener();
        Controller controller = new Controller();
        controller.addListener(listener);

        // Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the sample listener when done
        controller.removeListener(listener);
    }



    class SampleListener extends Listener {

        public void onConnect(Controller controller) {
            System.out.println("Connected");
            controller.enableGesture(Gesture.Type.TYPE_SWIPE);
        }

        private long lastTimestamp = 0;

        public void onFrame(Controller controller) {
            // only get frames every n milliseconds, to prevent overloading the hue api.
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTimestamp < 100) {
                return;
            }

            lastTimestamp = currentTime;

            PHLight left = api.getReachableLights().stream().filter(light -> light.getName().equals("Hue Lamp 5")).findFirst().get();
            PHLight center = api.getReachableLights().stream().filter(light -> light.getName().equals("Hue Lamp 3")).findFirst().get();
            PHLight right = api.getReachableLights().stream().filter(light -> light.getName().equals("Hue Lamp 4")).findFirst().get();

            List<PHLight> lights = Arrays.asList(left, center, right);

            Frame frame = controller.frame();
            Vector palmPosition = frame.hands().leftmost().palmPosition();

            if (frame.hands().count() == 0) {
                lights.forEach(light -> {
                    if (api.isOn(light)) {
                        log("off");
                        api.turnOff(light);
                    }
                });
                return;
            }

            float xPos = palmPosition.getX();
            float yPos = palmPosition.getY();

            Integer brightness = Float.valueOf(yPos/2).intValue();
            if (brightness < 0) brightness = 0;
            if (brightness > 254) brightness = 254;

            int centerThreshold = 60;
            if (xPos < -centerThreshold) {
                if (!api.isOn(left)) {
                    log("left");
                    api.turnOn(left);
                    api.turnOff(center);
                    api.turnOff(right);
                }
            } else if (xPos >= -centerThreshold && xPos <= centerThreshold) {
                if (!api.isOn(center)) {
                    log("center");
                    api.turnOn(center);
                    api.turnOff(left);
                    api.turnOff(right);
                }
            } else {
                if (!api.isOn(right)) {
                    log("right");
                    api.turnOn(right);
                    api.turnOff(center);
                    api.turnOff(left);
                }
            }
            log("brightness " + brightness);
            api.setBrightness(left, brightness);


        }
    }

    private void log(String message) {
        System.out.println(message);
    }
}
