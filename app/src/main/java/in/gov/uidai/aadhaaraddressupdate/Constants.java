package in.gov.uidai.aadhaaraddressupdate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import in.gov.uidai.aadhaaraddressupdate.Models.Users;
import in.gov.uidai.aadhaaraddressupdate.Requests.FirebaseCallback;
import in.gov.uidai.aadhaaraddressupdate.Requests.FirebaseCallbackForUser;

public class Constants {

    protected static String baseURL = "https://stage1.uidai.gov.in/";
    public static String generateOTP = baseURL + "onlineekyc/getOtp/";
    public static String downloadOfflineXML = baseURL + "onlineekyc/getEkyc/";

    public static String TAG = "AADHAAR_ADDRESS";
    public static String google_api = "AIzaSyCUskCcWYtRAGmx_FmaDtkz9xaaoPXZUGQ";

    public static String getRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    public static void isUserLogined(Context context, FirebaseCallback firebaseCallback) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        String uidNumberEnc = sp.getString("uidNumberEnc", "");
        boolean isLogined = sp.getBoolean("isLogined", false);
        if (!uidNumberEnc.isEmpty() && isLogined) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("encryptedUID/" + uidNumberEnc + "/UID");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String uidNumber = dataSnapshot.getValue(String.class);
                        Intent intent = new Intent(context, NewRequest.class);
                        intent.putExtra("uidNumber", uidNumber);
                        context.startService(intent);
                        firebaseCallback.getResponse(uidNumber);
                    } else {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.remove("uidNumberEnc");
                        editor.remove("isLogined");
                        editor.apply();
                        firebaseCallback.getError("User not logined.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    firebaseCallback.getError(databaseError.getMessage());
                }
            });
        } else {
            firebaseCallback.getError("Error checking login.");
        }
    }

    public static void getUserLogined(Context context, FirebaseCallbackForUser firebaseCallbackForUser) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        String uidNumberEnc = sp.getString("uidNumberEnc", "");
        boolean isLogined = sp.getBoolean("isLogined", false);
        if (!uidNumberEnc.isEmpty() && isLogined) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("encryptedUID/" + uidNumberEnc + "/UID");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String uidNumber = dataSnapshot.getValue(String.class);
                    DatabaseReference myRef1 = database.getReference("users/" + uidNumber);
                    myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Users user = dataSnapshot.getValue(Users.class);
                                firebaseCallbackForUser.getResponse(uidNumber, user);
                            } else {
                                Toast.makeText(context, "User Not Exists.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            firebaseCallbackForUser.getError(databaseError.getMessage());
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    firebaseCallbackForUser.getError(databaseError.getMessage());
                }
            });
        } else {
            firebaseCallbackForUser.getError("Error checking login.");
        }
    }

}
