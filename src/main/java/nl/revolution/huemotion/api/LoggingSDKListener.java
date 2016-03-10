package nl.revolution.huemotion.api;

import com.philips.lighting.hue.sdk.*;
import com.philips.lighting.model.PHBridge;

import java.util.List;

public class LoggingSDKListener implements PHSDKListener {

    private void log(String message) {
        System.out.println(message);
    }

    protected PHHueSDK hueSdk;

    public LoggingSDKListener(PHHueSDK hueSdk) {
        this.hueSdk = hueSdk;
    }

    @Override
    public void onAccessPointsFound(List<PHAccessPoint> list) {
        // Handle your bridge search results here.  Typically if multiple results are returned you will want to display them in a list
        // and let the user select their bridge.   If one is found you may opt to connect automatically to that bridge.
        log("onAccessPointsFound " + list);
    }

    @Override
    public void onCacheUpdated(List cacheNotificationsList, PHBridge bridge) {
        // Here you receive notifications that the BridgeResource Cache was updated. Use the PHMessageType to
        // check which cache was updated, e.g.
//        log("onCacheUpdated " + cacheNotificationsList + " " + bridge);
//        if (cacheNotificationsList.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) {
//            log("Lights Cache Updated");
//        }
    }

    @Override
    public void onBridgeConnected(PHBridge b, String username) {
        // Here it is recommended to set your connected bridge in your sdk object (as above) and start the heartbeat.
        // At this point you are connected to a bridge so you should pass control to your main program/activity.
        // The username is generated randomly by the bridge.
        // Also it is recommended you store the connected IP Address/ Username in your app here.  This will allow easy automatic connection on subsequent use.
        log("onBridgeConnected - got access key: '" + username + "'. Set BridgeAuth.BRIDGE_USERNAME to this value to automatically authenticate.");
        hueSdk.setSelectedBridge(b);
        hueSdk.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint accessPoint) {
        // Arriving here indicates that Pushlinking is required (to prove the User has physical access to the bridge).  Typically here
        // you will display a pushlink image (with a timer) indicating to to the user they need to push the button on their bridge within 30 seconds.
        log("Push to authenticate");
        hueSdk.startPushlinkAuthentication(accessPoint);
    }


    @Override
    public void onConnectionResumed(PHBridge bridge) {
        // log("onConnectionResumed " + bridge);
    }

    @Override
    public void onConnectionLost(PHAccessPoint accessPoint) {
        // Here you would handle the loss of connection to your bridge.
        log("onConnectionLost " + accessPoint);
    }

    @Override
    public void onError(int code, final String message) {
        // Here you can handle events such as Bridge Not Responding, Authentication Failed and Bridge Not Found
        log("onError: " + code + ", " + message);
    }

    @Override
    public void onParsingErrors(List parsingErrorsList) {
        // Any JSON parsing errors are returned here.  Typically your program should never return these.
        log("onParsingErrors: " + parsingErrorsList);
    }
}
