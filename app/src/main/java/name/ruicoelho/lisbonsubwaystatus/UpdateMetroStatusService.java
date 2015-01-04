package name.ruicoelho.lisbonsubwaystatus;

import android.app.IntentService;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpdateMetroStatusService extends IntentService {
    private static final int CONNECTION_TIMEOUT = 5;
    private final String TAG = "LisbonSubwayStatus";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String LOAD_METRO_STATUS_ACTION = "name.ruicoelho.lisbonsubwaystatus.action.LOAD_METRO_STATUS";
    public static final String LOAD_METRO_STATUS_RESULT = "name.ruicoelho.lisbonsubwaystatus.LOAD_METRO_STATUS_RESULT";
    public static final String BROADCAST_ACTION = "name.ruicoelho.lisbonsubwaystatus.BROADCAST_ACTION";

    public UpdateMetroStatusService() {
        super("UpdateMetroStatusService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            String url = intent.getDataString();
            if (LOAD_METRO_STATUS_ACTION.equals(action)) {
                handleLoadMetroStatus(url);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleLoadMetroStatus(String url) {
        MetroStatusConnector connector = new MetroStatusConnector(url);
        String jsonText = connector.loadData();
        Intent response = new Intent(BROADCAST_ACTION);
        response.putExtra(LOAD_METRO_STATUS_RESULT, jsonText);
        LocalBroadcastManager.getInstance(this).sendBroadcast(response);
    }
}
