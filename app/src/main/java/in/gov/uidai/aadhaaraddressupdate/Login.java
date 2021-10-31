package in.gov.uidai.aadhaaraddressupdate;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.UUID;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import in.gov.uidai.aadhaaraddressupdate.Models.Users;
import in.gov.uidai.aadhaaraddressupdate.Requests.FirebaseCallback;
import in.gov.uidai.aadhaaraddressupdate.Requests.PostRequest;

public class Login extends AppCompatActivity {

    EditText enterUIDUrbanLogin, otpMobileUrbanLogin;
    Button verifyUrbanLogin;
    String txnId = null;
    String uidNumber = null;
    boolean isOTPSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Constants.isUserLogined(this, new FirebaseCallback() {
            @Override
            public void getResponse(String uid) {
                startActivity(new Intent(Login.this, HomeActivity.class));
                finish();
            }

            @Override
            public void getError(String string) {
                init();
            }
        });
    }

    void init() {
        enterUIDUrbanLogin = findViewById(R.id.enterUIDUrbanLogin);
        otpMobileUrbanLogin = findViewById(R.id.otpMobileUrbanLogin);

        verifyUrbanLogin = findViewById(R.id.verifyUrbanLogin);

        verifyUrbanLogin.setOnClickListener(v -> {
            try {
                if (!isOTPSent)
                    generateOTP();
                else
                    verifyOTP();
            } catch (JSONException e) {
                Log.e(Constants.TAG, "OTP Error: " + e.getMessage());
            }
        });
        enterUIDUrbanLogin.setEnabled(true);
        otpMobileUrbanLogin.setEnabled(false);
    }

    private void loginUser(Users users) {
        String uidNumberEnc = Constants.getRandomString() + (System.currentTimeMillis() % 1000);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/" + uidNumber);
        DatabaseReference myRef1 = database.getReference("encryptedUID/" + uidNumberEnc + "/UID");
        myRef.removeValue();
        myRef.setValue(users);
        myRef1.removeValue();
        myRef1.setValue(uidNumber);
        SharedPreferences sp = this.getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("uidNumberEnc", uidNumberEnc);
        editor.putBoolean("isLogined", true);
        editor.apply();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void generateOTP() throws JSONException {
        uidNumber = enterUIDUrbanLogin.getText().toString().trim();
        if (uidNumber.isEmpty()) {
            enterUIDUrbanLogin.setError("Enter UID Number.");
            return;
        }

        uidNumber = uidNumber.replaceAll("\\s", "");

        txnId = UUID.randomUUID().toString();

        PostRequest postRequest = new PostRequest(this);

        postRequest.setUrl(Constants.generateOTP);

        postRequest.setPostData("uid", uidNumber);
        postRequest.setPostData("txnId", txnId);
        postRequest.setIsJsonObject(true);
        postRequest.send((jsonObject, jsonArray) -> {
            String status = jsonObject.getString("status");
            if (status.equalsIgnoreCase("y")) {
                isOTPSent = true;
                enterUIDUrbanLogin.setEnabled(false);
                otpMobileUrbanLogin.setEnabled(true);
                Toast.makeText(Login.this, "OTP Generated.", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(Login.this, jsonObject.getString("errCode"), Toast.LENGTH_SHORT).show();
                Toast.makeText(Login.this, "Unable to generate OTP.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOTP() throws JSONException {
        String otp = otpMobileUrbanLogin.getText().toString().trim();
        if (otp.isEmpty()) {
            otpMobileUrbanLogin.setError("Enter OTP.");
            return;
        }

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
                XmlToJson xmlToJson = new XmlToJson.Builder(eKycString).build();
                JSONObject jsonObject1 = xmlToJson.toJson();
                if (jsonObject1 == null) {
                    Toast.makeText(this, "Invalid Request.", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject poi = jsonObject1.getJSONObject("KycRes").getJSONObject("UidData").getJSONObject("Poi");

                String name = poi.getString("name");

                Users users = new Users();
                users.setName(name);

                loginUser(users);
            } else {
//                Toast.makeText(Login.this, jsonObject.getString("errCode"), Toast.LENGTH_SHORT).show();
                Toast.makeText(Login.this, "Unable to verify OTP.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}