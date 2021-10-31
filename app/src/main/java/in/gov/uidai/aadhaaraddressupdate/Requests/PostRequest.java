package in.gov.uidai.aadhaaraddressupdate.Requests;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import in.gov.uidai.aadhaaraddressupdate.Constants;

public class PostRequest {

    protected Context context;
    protected String url;
    protected JSONObject jsonObject;
    protected Boolean isJsonObject;
    protected Map<String, String> headers;

    public PostRequest(Context context){
        this.context = context;
        this.isJsonObject = true;
        this.jsonObject = new JSONObject();
        this.headers = new HashMap<>();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPostData(String key, String value) throws JSONException {
        this.jsonObject.put(key, value);
    }

    public void setIsJsonObject(Boolean isJsonObject){
        this.isJsonObject = isJsonObject;
    }

    public void send(RequestCallback requestCallback){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        if (isJsonObject){
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, this.jsonObject, response -> {
                try {
                    requestCallback.getResponse(response, null);
                } catch (JSONException e) {
                    Log.e(Constants.TAG, "JSON Error: " + e.getMessage());
                }
            }, Throwable::printStackTrace){
                @Override
                public Map<String, String> getHeaders() {
                    return headers;
                }
            };

            requestQueue.add(request);
        }else{
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, url, null, response -> {
                try {
                    requestCallback.getResponse(null, response);
                } catch (JSONException e) {
                    Log.e(Constants.TAG, "JSON Error: " + e.getMessage());
                }
            }, Throwable::printStackTrace){
                @Override
                public Map<String, String> getHeaders() {
                    return headers;
                }
            };
            requestQueue.add(request);
        }
    }
}
