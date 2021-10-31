package in.gov.uidai.aadhaaraddressupdate;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import in.gov.uidai.aadhaaraddressupdate.Models.Users;
import in.gov.uidai.aadhaaraddressupdate.Requests.FirebaseCallback;
import in.gov.uidai.aadhaaraddressupdate.Requests.PostRequest;

public class AddressUpdateUrban extends AppCompatActivity {

    EditText enterUIDUrban, otpMobileUrban;
    Button verifyUrban;
    String txnId = null;
    String uidNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_update_urban);

        Constants.isUserLogined(this, new FirebaseCallback() {
            @Override
            public void getResponse(String uid) {
                init(uid);
                try {
                    generateOTP();
                } catch (JSONException e) {
                    Log.e(Constants.TAG, "OTP Error: " + e.getMessage());
                }
            }

            @Override
            public void getError(String string) {
                startActivity(new Intent(AddressUpdateUrban.this, Login.class));
                finish();
            }
        });
    }

    void init(String uid) {
        if (!getIntent().hasExtra("uidNumber")) {
            Toast.makeText(this, "Invalid UID Number.", Toast.LENGTH_SHORT).show();
            finish();
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(getIntent().getIntExtra("NOTIFICATION_ID", -1));

        enterUIDUrban = findViewById(R.id.enterUIDUrban);
        otpMobileUrban = findViewById(R.id.otpMobileUrban);
        verifyUrban = findViewById(R.id.verifyUrban);

        verifyUrban.setOnClickListener(v -> downloadOfflineXML());
        uidNumber = uid.replaceAll("\\s", "");
        enterUIDUrban.setText(uidNumber);
        enterUIDUrban.setEnabled(false);
        otpMobileUrban.setEnabled(false);
    }

    private void generateOTP() throws JSONException {
        txnId = UUID.randomUUID().toString();

        PostRequest postRequest = new PostRequest(this);

        postRequest.setUrl(Constants.generateOTP);

        postRequest.setPostData("uid", uidNumber);
        postRequest.setPostData("txnId", txnId);
        postRequest.setIsJsonObject(true);
        postRequest.send((jsonObject, jsonArray) -> {
            String status = jsonObject.getString("status");
            if (status.equalsIgnoreCase("y")) {
                Toast.makeText(AddressUpdateUrban.this, "OTP Generated.", Toast.LENGTH_SHORT).show();
                otpMobileUrban.setEnabled(true);
            } else {
//                Toast.makeText(AddressUpdateUrban.this, jsonObject.getString("errCode"), Toast.LENGTH_SHORT).show();
                Toast.makeText(AddressUpdateUrban.this, "Unable to generate OTP.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadOfflineXML() {
        String otp = otpMobileUrban.getText().toString().trim();
        if (otp.isEmpty()) {
            otpMobileUrban.setError("Enter OTP.");
            return;
        }

        try {
            PostRequest postRequest = new PostRequest(this);

            postRequest.setUrl(Constants.downloadOfflineXML);

            postRequest.setPostData("txnId", txnId);
            postRequest.setPostData("otp", otp);
            postRequest.setPostData("uid", uidNumber);
            postRequest.setIsJsonObject(true);
            postRequest.send((jsonObject, jsonArray) -> {
                String status = jsonObject.getString("status");
                if (status.equalsIgnoreCase("y")) {
                    String eKycString = jsonObject.getString("eKycString");
                    Log.d(Constants.TAG, "XML: " + eKycString);
                    XmlToJson xmlToJson = new XmlToJson.Builder(eKycString).build();
                    JSONObject jsonObject1 = xmlToJson.toJson();
                    if (jsonObject1 == null) {
                        Toast.makeText(this, "Invalid Request.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONObject poi = jsonObject1.getJSONObject("KycRes").getJSONObject("UidData").getJSONObject("Poi");
                    JSONObject poa = jsonObject1.getJSONObject("KycRes").getJSONObject("UidData").getJSONObject("Poa");

                    String dob = poi.getString("dob");
                    String gender = poi.getString("gender");
                    String name = poi.getString("name");

                    String careof = poa.getString("co");
                    String country = poa.getString("country");
                    String dist = poa.getString("dist");
                    String house = poa.getString("house");
                    String landmark = poa.getString("lm");
                    String pc = poa.getString("pc");
                    String state = poa.getString("state");
                    String street = poa.getString("street");
                    String vtc = poa.getString("vtc");

                    Users users = new Users();
                    users.setUid(uidNumber);
                    users.setName(name);
                    users.setDob(dob);
                    users.setGender(gender);
                    users.setCareof(careof);
                    users.setHouse(house);
                    users.setStreet(street);
                    users.setLandmark(landmark);
                    users.setVtc(vtc);
                    users.setDist(dist);
                    users.setState(state);
                    users.setCountry(country);
                    users.setPc(pc);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users/" + uidNumber);
                    myRef.setValue(users);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("users", (Serializable) users);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
//                    Toast.makeText(AddressUpdateUrban.this, jsonObject.getString("errCode"), Toast.LENGTH_SHORT).show();
                    Toast.makeText(AddressUpdateUrban.this, "Unable to verify OTP.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.d(Constants.TAG, "Error: " + e.getMessage());
        }
    }

}