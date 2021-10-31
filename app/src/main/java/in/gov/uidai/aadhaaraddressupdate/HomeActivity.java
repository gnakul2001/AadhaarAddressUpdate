package in.gov.uidai.aadhaaraddressupdate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import in.gov.uidai.aadhaaraddressupdate.Requests.FirebaseCallback;

public class HomeActivity extends AppCompatActivity {

    Button newAddressUpdateRequest, sentAddressUpdateRequest, pendingAddressUpdateRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Constants.isUserLogined(this, new FirebaseCallback() {
            @Override
            public void getResponse(String uid) {
                init();
            }

            @Override
            public void getError(String string) {
                startActivity(new Intent(HomeActivity.this, Login.class));
                finish();
            }
        });
    }

    public void init(){
        newAddressUpdateRequest = findViewById(R.id.newAddressUpdateRequest);
        sentAddressUpdateRequest = findViewById(R.id.sentAddressUpdateRequest);
        pendingAddressUpdateRequest = findViewById(R.id.pendingAddressUpdateRequest);

        newAddressUpdateRequest.setOnClickListener(view -> startActivity(new Intent(this, NewAadhaarRequest.class)));
        sentAddressUpdateRequest.setOnClickListener(view -> startActivity(new Intent(this, SentAadhaarRequest.class)));
        pendingAddressUpdateRequest.setOnClickListener(view -> startActivity(new Intent(this, PendingAadhaarRequest.class)));
    }
}