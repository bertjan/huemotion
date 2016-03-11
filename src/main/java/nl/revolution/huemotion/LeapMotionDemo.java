package nl.revolution.huemotion;

import com.leapmotion.leap.*;
import nl.revolution.huemotion.api.BridgeAuth;
import nl.revolution.huemotion.api.HueAPI;

import java.io.IOException;

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

        // Turn all reachable lights off.
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
            if (currentTime - lastTimestamp < 350) {
                return;
            }

            lastTimestamp = currentTime;
            Frame frame = controller.frame();
            Vector palmPosition = frame.hands().leftmost().palmPosition();

            // When no hand is in sight, turn off all reachable lights that are on and stop processing.
            if (frame.hands().count() == 0) {
                api.getReachableLights().stream()
                        .filter(light -> api.isOn(light))
                        .forEach(light -> api.turnOff(light));
                return;
            }

            // Translate hand height to brightness between 0 and 254.
            float handHeight = palmPosition.getY();
            final int brightness = Math.max(Math.min(Float.valueOf(handHeight/2).intValue(), 254), 0);

            // Set brightness for all reachable lights.
            log("brightness: " + brightness);
            api.getReachableLights().forEach(light -> api.setBrightness(light, brightness));
        }
    }

    private void log(String message) {
        System.out.println(message);
    }
}
