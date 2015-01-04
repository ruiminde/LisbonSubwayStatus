package name.ruicoelho.lisbonsubwaystatus;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by Rui on 04/01/2015.
 */
public class MetroStatusConnector {
    private final String TAG = "LisbonSubwayStatus";

    private final String HTTP_CLIENT = "Android HTTP Client";
    private final int CONNECTION_TIMEOUT = 5;
    private final int MAX_REDIRECTS = 5;

    private String url;

    public MetroStatusConnector(String url) {
        this.url = url;
    }

    public String loadData() {
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance(HTTP_CLIENT);
        String jsonText = null;
        HttpParams params = httpClient.getParams();
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT * 1000);
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
        params.setIntParameter(ClientPNames.MAX_REDIRECTS, MAX_REDIRECTS);

        try {
            HttpResponse response = httpClient.execute(new HttpGet(this.url));
            jsonText = EntityUtils.toString(response.getEntity());
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {
            httpClient.close();
        }

        return jsonText;
    }
}
