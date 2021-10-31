package in.gov.uidai.aadhaaraddressupdate.Requests;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import in.gov.uidai.aadhaaraddressupdate.Constants;

public class GetRequest {
    protected Context context;
    protected String url;
    protected Boolean isJsonObject;

    public GetRequest(Context context){
        this.context = context;
        this.isJsonObject = true;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void send(RequestCallback requestCallback){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        if (isJsonObject){
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    requestCallback.getResponse(response, null);
                } catch (JSONException e) {
                    Log.e(Constants.TAG, "JSON Error: " + e.getMessage());
                }
            }, Throwable::printStackTrace);
            requestQueue.add(request);
        }else{
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
                try {
                    requestCallback.getResponse(null, response);
                } catch (JSONException e) {
                    Log.e(Constants.TAG, "JSON Error: " + e.getMessage());
                }
            }, Throwable::printStackTrace);
            requestQueue.add(request);
        }
    }
}
