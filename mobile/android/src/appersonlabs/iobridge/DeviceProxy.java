package appersonlabs.iobridge;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONObject;

import android.os.Build;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

@Kroll.proxy(creatableInModule = AppersonlabsIoBridgeModule.class)
public class DeviceProxy extends KrollProxy {

    private static final String    BASE_URL                 = "http://api.realtime.io/v1";

    private static AsyncHttpClient HTTP_CLIENT              = new AsyncHttpClient();

    private static final String    LCAT                     = "DeviceProxy";

    private static final String    PATH_CONNECTION_STATE    = "gateway/request/state";

    private static final String    PATH_READ_GPIO_REGISTER  = "gateway/request/register/read";

    private static final String    PATH_SEND_DATA           = "gateway/send";

    private static final String    PATH_WRITE_GPIO_REGISTER = "gateway/request/register/write";

    private String                 apikey                   = null;

    private String                 serial                   = null;

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
    public void fetchConnectionState(final KrollFunction callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", apikey);
        params.put("serial", serial);
        sendGETRequest(PATH_CONNECTION_STATE, params, callback);
    }

    private String getAbsoluteURL(String path) {
        return BASE_URL + (path.startsWith("/") ? "" : "/") + path;
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

    @Kroll.method(name = "readGPIORegister")
    public void readGPIORegister(int channel, String registerName, KrollFunction callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("serial", serial);
        params.put("channel", String.valueOf(channel));
        params.put("register", registerName);
        sendPOSTRequest(PATH_READ_GPIO_REGISTER, params, callback);
    }

    private void sendGETRequest(final String relativePath, final Map<String, String> params, final KrollFunction callback) {
        RequestParams requestParams = params != null ? new RequestParams(params) : new RequestParams();
        requestParams.put("apikey", apikey);
        requestParams.put("serial", serial);

        final KrollObject thisObject = this.getKrollObject();
        HTTP_CLIENT.get(getAbsoluteURL(relativePath), requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                if (callback != null) {
                    callback.callAsync(thisObject, new Object[] { AppersonlabsIoBridgeModule.throwableToErrorDict(e), KrollDict.fromJSON(errorResponse) });
                }
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (callback != null) {
                    callback.callAsync(thisObject, new Object[] { null, KrollDict.fromJSON(response) });
                }
            }
        });

    }

    private void sendPOSTRequest(final String relativePath, final Map<String, String> params, final KrollFunction callback) {
        try {
            final KrollObject thisObject = this.getKrollObject();
            JSONObject obj = new JSONObject(params);
            HTTP_CLIENT.post(getActivity().getApplicationContext(), getAbsoluteURL(relativePath), new StringEntity(obj.toString()), "application/json",
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(Throwable e, JSONObject errorResponse) {
                            if (callback != null) {
                                callback.callAsync(thisObject,
                                        new Object[] { AppersonlabsIoBridgeModule.throwableToErrorDict(e), KrollDict.fromJSON(errorResponse) });
                            }
                        }

                        @Override
                        public void onSuccess(JSONObject response) {
                            if (callback != null) {
                                callback.callAsync(thisObject, new Object[] { null, KrollDict.fromJSON(response) });
                            }
                        }
                    });
        }
        catch (UnsupportedEncodingException e) {
            Log.e(LCAT, e.getMessage());
        }
    }

    @Kroll.method(name = "writeGPIORegister")
    public void writeGPIORegister(int channel, String registerName, String content, KrollFunction callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("serial", serial);
        params.put("channel", String.valueOf(channel));
        params.put("register", registerName);
        params.put("content", content);
        sendPOSTRequest(PATH_WRITE_GPIO_REGISTER, params, callback);
    }
}
