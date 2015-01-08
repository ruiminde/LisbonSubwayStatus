package name.ruicoelho.lisbonsubwaystatus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class AllLinesStatusActivity extends Activity {

    public ListView listView;

    private final String TAG = "LisbonSubwayStatus";

    private final String URL = "http://ruipi-tubeservice.duckdns.org:4442/status";

    String[] values = new String[] { "Azul", "Amarela", "Vermelha", "Verde" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lines_status);

        ArrayAdapter mAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item, values);
        this.listView = (ListView) findViewById(R.id.listView);
        this.listView.setAdapter(mAdapter);

        IntentFilter mStatusIntentFilter = new IntentFilter(UpdateMetroStatusService.BROADCAST_ACTION);
        MetroStatusResponseReceiver mMetroStatusReceiver =
                new MetroStatusResponseReceiver();
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(mMetroStatusReceiver, mStatusIntentFilter);

        final Intent mServiceIntent = new Intent(this, UpdateMetroStatusService.class);
        mServiceIntent.setData(Uri.parse(URL));
        mServiceIntent.setAction(UpdateMetroStatusService.LOAD_METRO_STATUS_ACTION);
        this.startService(mServiceIntent);

        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getApplication().startService(mServiceIntent);
                    }
                }

        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_all_lines_status, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void updateStatusViewError() {
        Toast.makeText(getApplicationContext(), R.string.error_failed_to_update, Toast.LENGTH_SHORT).show();
    }

    private void updateStatusView(String jsonText) {
        try {
            JSONObject json = new JSONObject(jsonText);
            String reason = null;
            reason = json.getJSONArray("red").isNull(1) ? "" : json.getJSONArray("red").getString(1);
            values[0] = json.getJSONArray("red").getString(0).toUpperCase() + " " + reason;

            reason = json.getJSONArray("yellow").isNull(1) ? "" : json.getJSONArray("yellow").getString(1);
            values[1] = json.getJSONArray("yellow").getString(0).toUpperCase() + " " + reason;

            reason = json.getJSONArray("blue").isNull(1) ? "" : json.getJSONArray("blue").getString(1);
            values[2] = json.getJSONArray("blue").getString(0).toUpperCase() + " " + reason;

            reason = json.getJSONArray("green").isNull(1) ? "" : json.getJSONArray("green").getString(1);
            values[3] = json.getJSONArray("green").getString(0).toUpperCase() + " " + reason;
            ((TextView)listView.getChildAt(0)).setTextColor(Color.RED);
            ((TextView)listView.getChildAt(1)).setTextColor(Color.YELLOW);
            ((TextView)listView.getChildAt(2)).setTextColor(Color.BLUE);
            ((TextView)listView.getChildAt(3)).setTextColor(Color.GREEN);
            ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
        } catch (JSONException ex) {
            updateStatusViewError();
        }
    }
    // Broadcast receiver for receiving status updates from the IntentService
    private class MetroStatusResponseReceiver extends BroadcastReceiver
    {
        // Prevents instantiation
        private MetroStatusResponseReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        public void onReceive(Context context, Intent intent) {
            SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
            mSwipeRefreshLayout.setRefreshing(false);
            String jsonText = intent.getStringExtra(UpdateMetroStatusService.LOAD_METRO_STATUS_RESULT);
            if (jsonText != null) {
                updateStatusView(jsonText);
            } else if (intent.getBooleanExtra(UpdateMetroStatusService.STATUS_STILL_FRESH, true)) {
                Toast.makeText(getApplicationContext(), R.string.still_fresh, Toast.LENGTH_SHORT).show();
            } else {
                updateStatusViewError();
            }
        }
    }

    /*
    private class RetrieveMetroStatusTask extends AsyncTask<String, Void, JSONObject> {
        protected JSONObject doInBackground(String... urls) {
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android HTTP Client");
            String jsonText;
            HttpParams params = httpClient.getParams();
            params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT * 1000);
            params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
            params.setIntParameter(ClientPNames.MAX_REDIRECTS, 5);

            JSONObject result = null;
            try {
                HttpResponse response = httpClient.execute(new HttpGet("http://10.0.2.2:5000/status"));
                jsonText = EntityUtils.toString(response.getEntity());
                result = new JSONObject(jsonText);
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage(), ex);
            } catch (JSONException ex) {
                Log.e(TAG, ex.getMessage(), ex);
            } finally {
                httpClient.close();
            }
            return result;
        }

        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                Log.d(TAG, result.toString());
                try {
                    values[0] = "Vermelha: " + result.getString("red");
                    values[1] = "Amarela: " + result.getString("yellow");
                    values[2] = "Azul: " + result.getString("blue");
                    values[3] = "Verde: " + result.getString("green");
                    ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();

                } catch (JSONException ex) {
                    Log.e(TAG, ex.getLocalizedMessage(), ex);
                    Toast.makeText(getApplicationContext(), R.string.error_failed_to_update, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_failed_to_update, Toast.LENGTH_SHORT).show();
            }
        }
    }*/
}
