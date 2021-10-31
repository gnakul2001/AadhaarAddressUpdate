package in.gov.uidai.aadhaaraddressupdate;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import in.gov.uidai.aadhaaraddressupdate.Requests.FirebaseCallback;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        Constants.isUserLogined(this, new FirebaseCallback() {
            @Override
            public void getResponse(String uid) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void getError(String string) {
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        });
    }
}