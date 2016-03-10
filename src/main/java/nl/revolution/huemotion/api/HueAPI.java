package nl.revolution.huemotion.api;

import com.philips.lighting.hue.sdk.*;
import com.philips.lighting.hue.sdk.heartbeat.PHHeartbeatManager;
import com.philips.lighting.model.*;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class HueAPI {

    private PHHueSDK hueSdk;
    private PHBridge bridge;

    public HueAPI() {
        hueSdk = PHHueSDK.getInstance();
        hueSdk.setAppName("hueMotion");
        hueSdk.setDeviceName("hueMotion");
    }

    public void connectToBridge(String bridgeId, String username) {
        PHAccessPoint bridgeFound = findBridge(bridgeId);
        log("Found bridge: " + bridgeFound.getIpAddress());

        String ipAddress = bridgeFound.getIpAddress();

        PHAccessPoint accessPoint = new PHAccessPoint();
        accessPoint.setIpAddress(ipAddress);
        accessPoint.setUsername(username);

        HueMotionListener hueMotionListener = new HueMotionListener(hueSdk);
        PHNotificationManager notificationManager = hueSdk.getNotificationManager();
        notificationManager.registerSDKListener(hueMotionListener);

        hueSdk.connect(accessPoint);
        waitForConnection(accessPoint);

        bridge = hueSdk.getSelectedBridge();

        PHHeartbeatManager heartbeatManager = PHHeartbeatManager.getInstance();
        heartbeatManager.enableLightsHeartbeat(bridge, PHHueSDK.HB_INTERVAL);
    }

    public void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            log("sleep interrupted" + e);
        }
    }

    private void log(String message) {
        System.out.println(message);
    }

    public List<PHLight> getReachableLights() {
        return bridge.getResourceCache().getAllLights().stream()
                .filter(light -> light.getLastKnownLightState().isReachable())
                .collect(toList());
    }

    private void setLightState(String lightName, boolean lightOn, Integer transitionTime) {
        PHBridgeResourcesCache cache = bridge.getResourceCache();
        Optional<PHLight> lightOpt = cache.getAllLights().stream()
                .filter(light -> lightName.equals(light.getName()))
                .findFirst();

        if (!lightOpt.isPresent()) {
            throw new IllegalStateException("Light '" + lightName + "' not found.");
        }

        PHLight light = lightOpt.get();
        PHLightState lightState = light.getLastKnownLightState();
        if (!lightState.isReachable()) {
            throw new IllegalStateException("Light '" + lightName + "' found but not reachable.");
        }

        PHLightState newState = new PHLightState();
        newState.setOn(lightOn);
        newState.setTransitionTime(transitionTime);
        bridge.updateLightState(light, newState, new LoggingLightListener());
    }


    public boolean isOn(PHLight light) {
        return light.getLastKnownLightState().isOn();
    }

    public void setBrightness(PHLight light, Integer brightness) {
        PHLightState lightState = light.getLastKnownLightState();
        if (!lightState.isReachable()) {
            throw new IllegalStateException("Light found but not reachable.");
        }

        PHLightState newState = new PHLightState();
        newState.setOn(true);
        newState.setTransitionTime(0);
        newState.setBrightness(brightness);
        bridge.updateLightState(light, newState, new LoggingLightListener());

    }

    public void turnOff(PHLight light) {
        setLightState(light.getName(), false, 0);
    }

    public void turnOn(PHLight light) {
        setLightState(light.getName(), true, 0);
    }

    public void turnOn(String lightName) {
//        log("turning on light '" + lightName + "'.");
        setLightState(lightName, true, 0);
    }

    public void turnOff(String lightName) {
//        log("turning off light '" + lightName + "'.");
        setLightState(lightName, false, 0);
    }

    private void waitForConnection(PHAccessPoint accessPoint) {
        while (true) {
            boolean connected = hueSdk.isAccessPointConnected(accessPoint);
            // log("connected: " + connected);
            sleep(100);
            if(connected) {
                break;
            }
        }
    }

    public void findBridges() {
        hueSdk.getNotificationManager().registerSDKListener(new HueMotionListener(hueSdk));
        ((PHBridgeSearchManager) hueSdk.getSDKService(PHHueSDK.SEARCH_BRIDGE)).search(true, true);
    }

    public PHAccessPoint findBridge(String bridgeId) {
        HueMotionListener hueMotionListener = new HueMotionListener(hueSdk);
        PHNotificationManager notificationManager = hueSdk.getNotificationManager();
        notificationManager.registerSDKListener(hueMotionListener);
        hueMotionListener.setBridgeIdToFind(bridgeId);
        PHBridgeSearchManager sm = (PHBridgeSearchManager) hueSdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);

        for (int i=0; i<50; i++) {
            if (hueMotionListener.getBridge() != null) {
                notificationManager.unregisterSDKListener(hueMotionListener);
                return hueMotionListener.getBridge();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        log("bridge " + bridgeId + " not found.");
        notificationManager.unregisterSDKListener(hueMotionListener);
        return null;
    }


    private class HueMotionListener extends LoggingSDKListener {

        public HueMotionListener(PHHueSDK hueSdk) {
            super(hueSdk);
        }

        public void setBridgeIdToFind(String bridgeIdToFind) {
            this.bridgeIdToFind = bridgeIdToFind;
        }

        private String bridgeIdToFind;

        public PHAccessPoint getBridge() {
            return bridge;
        }

        private PHAccessPoint bridge;

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {
            list.stream().forEach(ap -> {
                log("Found bridge: ip = " + ap.getIpAddress() + ", id = " + ap.getBridgeId());
                // log(bridgeIdToFind + " = " + ap.getBridgeId());
                if (ap.getBridgeId().equals(bridgeIdToFind)) {
                    bridge = ap;
                }
            });
        }

    }


}
