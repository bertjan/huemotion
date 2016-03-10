package nl.revolution.huemotion;

import com.philips.lighting.model.PHBridgeResource;
import nl.revolution.huemotion.api.BridgeAuth;
import nl.revolution.huemotion.api.HueAPI;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class HueAPIDemo {

    public static void main(String... args) {
        new HueAPIDemo().doDemo();
    }

    private void doDemo() {
        HueAPI api = new HueAPI();

        // Logs all bridges in the network.
        // api.findBridges();

        api.connectToBridge(BridgeAuth.BRIDGE_ID, BridgeAuth.BRIDGE_USERNAME);
        log("Connected to bridge.");

        api.getReachableLights().forEach(light -> {
            log("Found reachable light: " + light.getName());
        });

        // Get a list of all names of reachable lights.
        List<String> lightNames = api.getReachableLights().stream().map(PHBridgeResource::getName).collect(toList());
        // To use specific lights:
        // List<String> lightNames = Arrays.asList("LivingColors 1", "Bank");
        // List<String> lightNames = Arrays.asList("Hue Lamp 5", "Hue Lamp 3", "Hue Lamp 4");

        // Loop over lights and turn them on/off.
        for(int loop=0; loop < 4; loop++) {
            // left-to-right
            for (int index = 0; index < lightNames.size(); index++) {
                final String lightName = lightNames.get(index);
                api.turnOn(lightName);
                api.sleep(350);
                api.turnOff(lightName);
            }

            // right-to-left
            for (int index = lightNames.size() - 2; index > 0; index--) {
                final String lightName = lightNames.get(index);
                api.turnOn(lightName);
                api.sleep(350);
                api.turnOff(lightName);
            }

        }

        // Everything off.
        api.getReachableLights().forEach(api::turnOff);

//        float xy[] = PHUtilities.calculateXYFromRGB(0, 0, 255, light.getModelNumber());
//        lightState.setX(xy[0]);
//        lightState.setY(xy[1]);

    }

    private void log(String message) {
        System.out.println(message);
    }

}
