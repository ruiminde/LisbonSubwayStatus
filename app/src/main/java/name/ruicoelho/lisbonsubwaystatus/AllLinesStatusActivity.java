package name.ruicoelho.lisbonsubwaystatus;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
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

    private MetroStatusResponseReceiver mMetroStatusReceiver = null;

    String[] values = new String[] { "Azul", "Amarela", "Vermelha", "Verde" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lines_status);

        ArrayAdapter mAdapter = new ArrayAdapter<String>(this,
                R.layout.list_item, R.id.status_list, values);
        this.listView = (ListView) findViewById(R.id.listView);
        this.listView.setAdapter(mAdapter);

        IntentFilter mStatusIntentFilter = new IntentFilter(UpdateMetroStatusService.BROADCAST_ACTION);
        mMetroStatusReceiver = new MetroStatusResponseReceiver();
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(mMetroStatusReceiver, mStatusIntentFilter);

        final Intent mServiceIntent = new Intent(this, UpdateMetroStatusService.class);
        mServiceIntent.setData(Uri.parse(URL));
        mServiceIntent.setAction(UpdateMetroStatusService.LOAD_METRO_STATUS_ACTION);
        this.startService(mServiceIntent);


        this.listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(AllLinesStatusActivity.this.getApplicationContext(), ImagesActivity.class);
                startActivity(intent);
                return true;
            };
        });

        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = cm.getActiveNetworkInfo();
                        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                            getApplication().startService(mServiceIntent);
                        } else {
                            mSwipeRefreshLayout.setRefreshing(false);
                            updateStatusViewError(R.string.error_no_network_connection);
                        }
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
        updateStatusViewError(R.string.error_failed_to_update);
    }

    private void updateStatusViewError(int stringResourceId) {
        Toast.makeText(getApplicationContext(),stringResourceId, Toast.LENGTH_SHORT).show();
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
            /*((TextView)listView.getChildAt(1)).setTextColor(Color.YELLOW);
            ((TextView)listView.getChildAt(2)).setTextColor(Color.BLUE);
            listView.getChildAt(0).setBackground(getDrawable(R.drawable.blue_line));
            ((TextView)listView.getChildAt(3)).setTextColor(Color.GREEN);*/
            ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
        } catch (JSONException ex) {
            updateStatusViewError();
        }
    }

    @Override
    public void onPause() {
        if (this.mMetroStatusReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mMetroStatusReceiver);
        }
        super.onPause();  // Always call the superclass method first

    }

    @Override
    public void onResume() {
        IntentFilter mStatusIntentFilter = new IntentFilter(UpdateMetroStatusService.BROADCAST_ACTION);
        mMetroStatusReceiver = new MetroStatusResponseReceiver();
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(mMetroStatusReceiver, mStatusIntentFilter);

        super.onResume();
    }

    @Override
    public void onStop() {
        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        mSwipeRefreshLayout.setRefreshing(false);

        super.onStop();
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
}
