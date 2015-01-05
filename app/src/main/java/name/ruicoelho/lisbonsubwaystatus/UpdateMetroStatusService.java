package name.ruicoelho.lisbonsubwaystatus;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceManager;
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
import java.security.Timestamp;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class UpdateMetroStatusService extends IntentService {
    private static final String PREFS_LAST_UPDATE_KEY = "LAST_UPDATE_TIMESTAMP";
    private static final long MIN_UPDATE_INTERVAL_MILLIS = 1 * 60 * 1000L; // 1 minute
    private static final String TAG = "LisbonSubwayStatus";

    private long lastUpdate = 0L;

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String LOAD_METRO_STATUS_ACTION = "name.ruicoelho.lisbonsubwaystatus.action.LOAD_METRO_STATUS";
    public static final String LOAD_METRO_STATUS_RESULT = "name.ruicoelho.lisbonsubwaystatus.LOAD_METRO_STATUS_RESULT";
    public static final String STATUS_STILL_FRESH = "name.ruicoelho.lisbonsubwaystatus.STATUS_STILL_FRESH";
    public static final String BROADCAST_ACTION = "name.ruicoelho.lisbonsubwaystatus.BROADCAST_ACTION";

    public UpdateMetroStatusService() {
        super("UpdateMetroStatusService");
    }


    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.lastUpdate = prefs.getLong(PREFS_LAST_UPDATE_KEY, 0L);
    }

    public void onDestroy() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putLong(PREFS_LAST_UPDATE_KEY, this.lastUpdate).commit();
        super.onDestroy();
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
        Intent response = new Intent(BROADCAST_ACTION);

        long currentTimeMillis = System.currentTimeMillis();
        if (this.lastUpdate != 0 && currentTimeMillis < this.lastUpdate + MIN_UPDATE_INTERVAL_MILLIS) {
            Log.d(TAG, "Last status update has not expired");
            response.putExtra(STATUS_STILL_FRESH, true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(response);
            return;
        }

        this.lastUpdate = currentTimeMillis;
        MetroStatusConnector connector = new MetroStatusConnector(url);
        String jsonText = connector.loadData();
        response.putExtra(LOAD_METRO_STATUS_RESULT, jsonText);
        response.putExtra(STATUS_STILL_FRESH, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(response);
    }
}
