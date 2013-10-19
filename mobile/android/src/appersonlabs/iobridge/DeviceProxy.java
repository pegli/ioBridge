package appersonlabs.iobridge;

import java.util.HashMap;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.util.Log;

import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

@Kroll.proxy(creatableInModule = AppersonlabsIoBridgeModule.class)
public class DeviceProxy extends KrollProxy {

    private static class KrollAsyncCallback extends AsyncCallback {

        private KrollFunction callback;

        private KrollObject   thisObject;

        public KrollAsyncCallback(KrollObject thisObject, KrollFunction callback) {
            this.thisObject = thisObject;
            this.callback = callback;
        }

        @Override
        public void onComplete(HttpResponse httpResponse) {
            if (httpResponse != null) {
                Log.i(LCAT, String.format("response status: %d", httpResponse.getStatus()));
                if (callback != null) {
                    try {
                        JSONObject obj = new JSONObject(httpResponse.getBodyAsString());
                        callback.callAsync(thisObject, new Object[] { null, KrollDict.fromJSON(obj) });
                    }
                    catch (JSONException e) {
                        onError(e);
                    }
                }
            }
            else {
                Log.e(LCAT, "error in async callback: " + (httpResponse != null ? httpResponse.getBodyAsString() : "null response"));
            }
        }

        @Override
        public void onError(Exception e) {
            if (callback != null) {
                KrollDict err = new KrollDict();
                err.put("message", e.getMessage());
                callback.callAsync(thisObject, new Object[] { err, null });
            }
        }
    }

    private static final String      BASE_URL                 = "http://api.realtime.io/v1/";

    private static AndroidHttpClient HTTP_CLIENT              = new AndroidHttpClient(BASE_URL);

    private static final String      LCAT                     = "DeviceProxy";

    private static final String      PATH_CONNECTION_STATE    = "gateway/request/state";

    private static final String      PATH_READ_GPIO_REGISTER  = "gateway/request/register/read";

    private static final String      PATH_SEND_DATA           = "gateway/send";

    private static final String      PATH_STREAMING           = "stream";

    private static final String      PATH_WRITE_GPIO_REGISTER = "gateway/request/register/write";

    private String                   apikey                   = null;

    private String                   serial                   = null;

    public DeviceProxy() {
        disableConnectionReuseIfNecessary();
    }

    private void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    @Kroll.method(name = "fetchConnectionState")
    public void fetchConnectionState(@Kroll.argument(optional = true) final KrollFunction callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", apikey);
        params.put("serial", serial);
        sendGETRequest(PATH_CONNECTION_STATE, params, callback);
    }

    @Override
    public void handleCreationDict(KrollDict dict) {
        super.handleCreationDict(dict);
        this.apikey = TiConvert.toString(dict.get("apikey"));
        this.serial = TiConvert.toString(dict.get("serial"));
        // TODO ensure these two properties are present

        // set up HTTP client headers
        HTTP_CLIENT.addHeader("X-APIKEY", apikey);
        HTTP_CLIENT.addHeader("Content-Type", "application/json");
    }

    private StreamingURLConnectionTask streamer = null;

    /**
     * Only called the first time an event of a particular type is added.
     */
    @Override
    public void onHasListenersChanged(String event, boolean hasListeners) {
        Log.i(LCAT, "onHasListenersChanged("+event+","+hasListeners+")");
        if ("stream".equals(event)) {
            if (hasListeners) {
                // TODO start streaming
                streamer = new StreamingURLConnectionTask(this);
                streamer.execute(new String[] { BASE_URL + PATH_STREAMING + "?apikey=" + apikey });
                Log.i(LCAT, "started streamer");
            }
            else {
                // TODO stop streaming
                if (streamer != null) {
                    streamer.cancel();
                    Log.i(LCAT, "cancelled streamer");
                }
            }
        }
        super.onHasListenersChanged(event, hasListeners);
    }

    @Kroll.method(name = "readGPIORegister")
    public void readGPIORegister(int channel, String registerName, @Kroll.argument(optional = true) KrollFunction callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("serial", serial);
        params.put("channel", String.valueOf(channel));
        params.put("register", registerName);
        sendPOSTRequest(PATH_READ_GPIO_REGISTER, params, callback);
    }

    @Kroll.method(name = "sendData")
    public void sendData(int channel, String payload, @Kroll.argument(optional = true) String encoding, @Kroll.argument(optional = true) KrollFunction callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("channel", String.valueOf(channel));
        params.put("serial", serial);
        params.put("encoding", encoding != null ? encoding : "plain");
        params.put("payload", payload);
        sendPOSTRequest(PATH_SEND_DATA, params, callback);
    }

    private void sendGETRequest(final String relativePath, final Map<String, String> params, final KrollFunction callback) {
        ParameterMap paramMap = HTTP_CLIENT.newParams();
        paramMap.put("apikey", apikey);
        paramMap.put("serial", serial);
        for (String key : params.keySet()) {
            paramMap.put(key, params.get(key));
        }

        HTTP_CLIENT.get(relativePath, paramMap, new KrollAsyncCallback(this.getKrollObject(), callback));
    }

    private void sendPOSTRequest(final String relativePath, final Map<String, String> params, final KrollFunction callback) {
        JSONObject obj = new JSONObject(params);

        // POST requests use the X-APIKEY header, so no need to add apikey and
        // serial number to body
        HTTP_CLIENT.post(relativePath, "application/json", obj.toString().getBytes(), new KrollAsyncCallback(this.getKrollObject(), callback));
    }

    @Kroll.method(name = "writeGPIORegister")
    public void writeGPIORegister(int channel, String registerName, String content, @Kroll.argument(optional = true) KrollFunction callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("serial", serial);
        params.put("channel", String.valueOf(channel));
        params.put("register", registerName);
        params.put("content", content);
        sendPOSTRequest(PATH_WRITE_GPIO_REGISTER, params, callback);
    }

}
