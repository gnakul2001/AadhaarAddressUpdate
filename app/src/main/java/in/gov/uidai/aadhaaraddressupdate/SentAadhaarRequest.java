package in.gov.uidai.aadhaaraddressupdate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.gov.uidai.aadhaaraddressupdate.Models.PendingRequest;
import in.gov.uidai.aadhaaraddressupdate.Models.Users;
import in.gov.uidai.aadhaaraddressupdate.Requests.FirebaseCallback;

public class SentAadhaarRequest extends AppCompatActivity {

    List<String> arrayList;
    HashMap<String, PendingRequest> uidRequests;
    ListView sentAddressUpdateRequestList;
    private String currentUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_aadhaar_request);
        sentAddressUpdateRequestList = findViewById(R.id.sentAddressUpdateRequestList);
        arrayList = new ArrayList<>();
        uidRequests = new HashMap<>();
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        assert intent != null;
                        String address_str = intent.getStringExtra("address");
                        try {
                            JSONObject address = new JSONObject(address_str);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("users/" +currentUID);
                            myRef.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                    if (snapshot.exists()) {
                                        Users user = new Users();
                                        try {
                                            user.setHouse(address.getString("house"));
                                            user.setStreet(address.getString("street"));
                                            user.setLandmark(address.getString("landmark"));
                                            user.setVtc(address.getString("vtc"));
                                            user.setDist(address.getString("dist"));
                                            user.setState(address.getString("state"));
                                            user.setCountry(address.getString("country"));
                                            user.setPc(address.getString("pc"));
                                            DatabaseReference myRef1 = database.getReference("users/" + currentUID);
                                            myRef1.setValue(user);
                                            Toast.makeText(SentAadhaarRequest.this, "Address saved.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } catch (JSONException e) {
                                            Toast.makeText(SentAadhaarRequest.this, "Error saving address.", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(SentAadhaarRequest.this, "Error saving address.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
        Constants.isUserLogined(this, new FirebaseCallback() {
            @Override
            public void getResponse(String uidNumber) {
                currentUID = uidNumber;
                final ArrayAdapter<String> adapter = new ArrayAdapter<>(SentAadhaarRequest.this, android.R.layout.simple_dropdown_item_1line, arrayList);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("PendingRequests/" + uidNumber);
                myRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            String uid = snapshot.getKey();
                            PendingRequest pendingRequest = snapshot.getValue(PendingRequest.class);
                            arrayList.add(uid);
                            uidRequests.put(uid, pendingRequest);
                            adapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(SentAadhaarRequest.this, "Error getting requests.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        String uid = snapshot.getKey();
                        arrayList.remove(uid);
                        uidRequests.remove(uid);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                sentAddressUpdateRequestList.setAdapter(adapter);
                sentAddressUpdateRequestList.setOnItemClickListener((parent, view, position, id) -> {
                    String uid = parent.getItemAtPosition(position).toString();
                    PendingRequest pendingRequest = uidRequests.get(uid);
                    assert pendingRequest != null;
                    if (!pendingRequest.isRequestAccepted()){
                        Toast.makeText(SentAadhaarRequest.this, "Request not approved.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pendingRequest.setUid(uid);
                    Intent intent = new Intent(SentAadhaarRequest.this, UpdateAddress.class);
                    intent.putExtra("user", pendingRequest);
                    someActivityResultLauncher.launch(intent);
                });
            }

            @Override
            public void getError(String string) {
                startActivity(new Intent(SentAadhaarRequest.this, Login.class));
                finish();
            }
        });
    }
}
