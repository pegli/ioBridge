package appersonlabs.iobridge;

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

    private static final String    LCAT                     = "DeviceProxy";

    private String                 apikey                   = null;

    private String                 serial                   = null;

    private static AsyncHttpClient HTTP_CLIENT              = new AsyncHttpClient();

    private static final String    BASE_URL                 = "http://api.realtime.io/v1";

    private static final String    PATH_CONNECTION_STATE    = "gateway/request/state";

    private static final String    PATH_READ_GPIO_REGISTER  = "gateway/request/register/read";

    private static final String    PATH_WRITE_GPIO_REGISTER = "gateway/request/register/write";

    private static final String    PATH_SEND_DATA           = "gateway/send";

    private void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private String getAbsoluteURL(String path) {
        return BASE_URL + (path.startsWith("/") ? "" : "/") + path;
    }

    public DeviceProxy() {
        disableConnectionReuseIfNecessary();
    }

    @Override
    public void handleCreationDict(KrollDict dict) {
        super.handleCreationDict(dict);
        this.apikey = TiConvert.toString(dict.get("apikey"));
        this.serial = TiConvert.toString(dict.get("serial"));
        // TODO ensure these two properties are present
    }

    @Kroll.method(name = "fetchConnectionState")
    public void fetchConnectionState(final KrollFunction callback) {
        final KrollObject thisObject = this.getKrollObject();

        RequestParams params = new RequestParams();
        params.put("apikey", apikey);
        params.put("serial", serial);
        HTTP_CLIENT.get(getAbsoluteURL(PATH_CONNECTION_STATE), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                if (callback != null) {
                    callback.callAsync(thisObject, new Object[] { null, KrollDict.fromJSON(response) });
                }
            }

            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                if (callback != null) {
                    callback.callAsync(thisObject, new Object[] { AppersonlabsIoBridgeModule.throwableToErrorDict(e), KrollDict.fromJSON(errorResponse) });
                }
            }
        });
    }
}
