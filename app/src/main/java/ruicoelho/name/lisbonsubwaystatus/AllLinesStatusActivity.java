package ruicoelho.name.lisbonsubwaystatus;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    private final int CONNECTION_TIMEOUT = 5;

    String[] values = new String[] { "Azul", "Amarela", "Vermelha", "Verde" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lines_status);

        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, values);
        this.listView = (ListView) findViewById(R.id.listView);
        this.listView.setAdapter(mAdapter);

        new RetrieveMetroStatusTask().execute();
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
    }
}
