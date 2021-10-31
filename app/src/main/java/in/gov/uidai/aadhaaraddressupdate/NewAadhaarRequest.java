package in.gov.uidai.aadhaaraddressupdate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import in.gov.uidai.aadhaaraddressupdate.Models.Users;
import in.gov.uidai.aadhaaraddressupdate.Requests.FirebaseCallback;
import in.gov.uidai.aadhaaraddressupdate.Requests.FirebaseCallbackForUser;
import in.gov.uidai.aadhaaraddressupdate.Models.PendingRequest;

public class NewAadhaarRequest extends AppCompatActivity {

    EditText requirerAadhaar;
    Button sendAddressUpdateRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_aadhaar_request);

        Constants.isUserLogined(this, new FirebaseCallback() {
            @Override
            public void getResponse(String uid) {
                init();
            }

            @Override
            public void getError(String string) {
                startActivity(new Intent(NewAadhaarRequest.this, Login.class));
                finish();
            }
        });
    }

    public void init(){
        requirerAadhaar = findViewById(R.id.requirerAadhaar);
        sendAddressUpdateRequest = findViewById(R.id.sendAddressUpdateRequest);

        sendAddressUpdateRequest.setOnClickListener(view -> {
            String uid = requirerAadhaar.getText().toString().trim();
            uid = uid.replaceAll("\\s", "");
            if (uid.isEmpty()){
                requirerAadhaar.setError("Enter UID.");
                return;
            }
            String finalUid = uid;
            Constants.getUserLogined(this, new FirebaseCallbackForUser() {
                @Override
                public void getResponse(String uidNumber, Users user) {
                    PendingRequest pendingRequest = new PendingRequest();
                    PendingRequest pendingRequest1 = new PendingRequest();
                    pendingRequest.setName(user.getName());
                    pendingRequest.setRequestAccepted(false);
                    pendingRequest1.setRequestAccepted(false);
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("NewRequests/" + finalUid + "/" + uidNumber);
                    DatabaseReference myRef1 = database.getReference("PendingRequests/" + uidNumber + "/" + finalUid);
                    myRef.removeValue();
                    myRef.setValue(pendingRequest);
                    myRef1.removeValue();
                    myRef1.setValue(pendingRequest1);
                    Toast.makeText(NewAadhaarRequest.this, "Request Sent.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(NewAadhaarRequest.this, HomeActivity.class));
                    finish();
                }
                @Override
                public void getError(String string) {
                    Toast.makeText(NewAadhaarRequest.this, "Error Sending Request.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
