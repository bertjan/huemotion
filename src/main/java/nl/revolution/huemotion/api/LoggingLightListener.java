package nl.revolution.huemotion.api;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.model.*;

import java.util.List;
import java.util.Map;

public class LoggingLightListener implements PHLightListener {

    private void log(String message) {
        System.out.println(message);
    }


    @Override
    public void onReceivingLightDetails(PHLight phLight) {
        log("onReceivingLightDetails " + phLight);
    }

    @Override
    public void onReceivingLights(List<PHBridgeResource> list) {
        log("onReceivingLights " + list);
    }

    @Override
    public void onSearchComplete() {
        log("onSearchComplete");
    }

    @Override
    public void onSuccess() {
        //log("onSuccess");
    }

    @Override
    public void onError(int code, String message) {
        log("onError: " + code + ", " + message);
    }

    @Override
    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
        // log("onStateUpdate " + map + " " + list);
    }
}
