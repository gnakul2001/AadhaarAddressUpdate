package in.gov.uidai.aadhaaraddressupdate.Requests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface RequestCallback {
    void getResponse(JSONObject jsonObject, JSONArray jsonArray) throws JSONException;
}
