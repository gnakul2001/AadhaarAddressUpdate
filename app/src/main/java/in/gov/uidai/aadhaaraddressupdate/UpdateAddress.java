package in.gov.uidai.aadhaaraddressupdate;

import static in.gov.uidai.aadhaaraddressupdate.Constants.TAG;
import static in.gov.uidai.aadhaaraddressupdate.Constants.google_api;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import in.gov.uidai.aadhaaraddressupdate.Models.PendingRequest;
import in.gov.uidai.aadhaaraddressupdate.Requests.FirebaseCallback;
import in.gov.uidai.aadhaaraddressupdate.Requests.GetRequest;

public class UpdateAddress extends AppCompatActivity {

    EditText houseUpdate, streetUpdate, landmarkUpdate, vtcUpdate,
            distUpdate, stateUpdate, countryUpdate, pcUpdate;
    Button updateAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_address);

        Constants.isUserLogined(this, new FirebaseCallback() {
            @Override
            public void getResponse(String uid) {
                init();
            }

            @Override
            public void getError(String string) {
                startActivity(new Intent(UpdateAddress.this, Login.class));
                finish();
            }
        });
    }

    void init() {
        houseUpdate = findViewById(R.id.houseUpdate);
        streetUpdate = findViewById(R.id.streetUpdate);
        landmarkUpdate = findViewById(R.id.landmarkUpdate);
        vtcUpdate = findViewById(R.id.vtcUpdate);
        distUpdate = findViewById(R.id.distUpdate);
        stateUpdate = findViewById(R.id.stateUpdate);
        countryUpdate = findViewById(R.id.countryUpdate);
        pcUpdate = findViewById(R.id.pcUpdate);

        updateAddress = findViewById(R.id.updateAddress);

        PendingRequest user = (PendingRequest) getIntent().getSerializableExtra("user");

        houseUpdate.setText(user.getHouse());
        streetUpdate.setText(user.getStreet());
        landmarkUpdate.setText(user.getLandmark());
        vtcUpdate.setText(user.getVtc());
        distUpdate.setText(user.getDist());
        stateUpdate.setText(user.getState());
        countryUpdate.setText(user.getCountry());
        pcUpdate.setText(user.getPc());

        updateAddress.setOnClickListener(view -> {
            String address = "%s %s, %s, %s, %s, %s-%s";
            String address1 = String.format(address
                    , user.getHouse()
                    , user.getStreet()
                    , user.getLandmark()
                    , user.getVtc()
                    , user.getDist()
                    , user.getState()
                    , user.getCountry()
                    , user.getPc()
            );
            String address2 = String.format(address, houseUpdate.getText().toString().trim()
                    , streetUpdate.getText().toString().trim()
                    , landmarkUpdate.getText().toString().trim()
                    , vtcUpdate.getText().toString().trim()
                    , distUpdate.getText().toString().trim()
                    , stateUpdate.getText().toString().trim()
                    , countryUpdate.getText().toString().trim()
                    , pcUpdate.getText().toString().trim()
            );
            JSONObject address_json = new JSONObject();
            try {
                address_json.put("uid", user.getUid());
                address_json.put("house", houseUpdate.getText().toString().trim());
                address_json.put("street", streetUpdate.getText().toString().trim());
                address_json.put("landmark", landmarkUpdate.getText().toString().trim());
                address_json.put("vtc", vtcUpdate.getText().toString().trim());
                address_json.put("dist", distUpdate.getText().toString().trim());
                address_json.put("state", stateUpdate.getText().toString().trim());
                address_json.put("country", countryUpdate.getText().toString().trim());
                address_json.put("pc", pcUpdate.getText().toString().trim());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";
            GetRequest getRequest = new GetRequest(UpdateAddress.this);
            getRequest.setUrl(String.format(url, address1, google_api));
            getRequest.send((jsonObject, jsonArray) -> {
                GetRequest getRequest1 = new GetRequest(UpdateAddress.this);
                getRequest1.setUrl(String.format(url, address2, google_api));
                getRequest1.send((jsonObject1, jsonArray1) -> {
                    JSONArray jsonArray2 = jsonObject.getJSONArray("results");
                    Log.d(TAG, "Result: " + jsonObject.toString());
                    JSONArray jsonArray3 = jsonObject1.getJSONArray("results");
                    if (jsonArray2.length()<=0){
                        Toast.makeText(UpdateAddress.this, "Address Not Found.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (jsonArray3.length()<=0){
                        Toast.makeText(UpdateAddress.this, "Address Not Found.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONObject geometry1 = (JSONObject) jsonArray2.get(0);
                    JSONObject geometry2 = (JSONObject) jsonArray3.get(0);
                    double lat1 = geometry1.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng1 = geometry1.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    double lat2 = geometry2.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng2 = geometry2.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    Location locationA = new Location("");
                    locationA.setLatitude(lat1);
                    locationA.setLongitude(lng1);

                    Location locationB = new Location("");
                    locationB.setLatitude(lat2);
                    locationB.setLongitude(lng2);

                    float distance = locationA.distanceTo(locationB);
                    if (distance > 500){
                        Toast.makeText(UpdateAddress.this, "Address is far distance than 500 Meters.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(UpdateAddress.this, "Updating Address.", Toast.LENGTH_SHORT).show();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("address", address_json.toString());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                });
            });
        });
    }
}