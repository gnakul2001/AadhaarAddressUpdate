package in.gov.uidai.aadhaaraddressupdate;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

public class PendingAadhaarRequest extends AppCompatActivity {

    List<String> arrayList;
    HashMap<String, JSONObject> uidRequests;
    HashMap<String, String> uidRequests1;
    ListView pendingAddressUpdateRequestList;
    String currentUID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_aadhaar_request);
        arrayList = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(PendingAadhaarRequest.this, android.R.layout.simple_dropdown_item_1line, arrayList);
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        assert intent != null;
                        Users users = (Users) intent.getSerializableExtra("users");
                        PendingRequest pendingRequest = new PendingRequest();
                        pendingRequest.setName(users.getName());
                        pendingRequest.setDob(users.getDob());
                        pendingRequest.setGender(users.getGender());
                        pendingRequest.setCareof(users.getCareof());
                        pendingRequest.setHouse(users.getHouse());
                        pendingRequest.setStreet(users.getStreet());
                        pendingRequest.setLandmark(users.getLandmark());
                        pendingRequest.setVtc(users.getVtc());
                        pendingRequest.setDist(users.getDist());
                        pendingRequest.setState(users.getState());
                        pendingRequest.setCountry(users.getCountry());
                        pendingRequest.setPc(users.getPc());
                        pendingRequest.setRequestAccepted(true);

                        PendingRequest pendingRequest1 = new PendingRequest();
                        pendingRequest1.setRequestAccepted(true);

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("PendingRequests/" + currentUID + "/" + users.getUid());
                        DatabaseReference myRef1 = database.getReference("NewRequests/" + users.getUid() + "/" + currentUID);
                        myRef.setValue(pendingRequest);
                        myRef1.setValue(pendingRequest1);
                        String uidText = uidRequests1.get(currentUID);
                        arrayList.remove(uidText);
                        uidRequests.remove(uidText);
                        uidRequests1.remove(users.getUid());
                        users.setUid(null);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(PendingAadhaarRequest.this, "Request Accepted.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        pendingAddressUpdateRequestList = findViewById(R.id.pendingAddressUpdateRequestList);
        uidRequests = new HashMap<>();
        uidRequests1 = new HashMap<>();
        Constants.isUserLogined(this, new FirebaseCallback() {
            @Override
            public void getResponse(String uidNumber) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("NewRequests/" + uidNumber);
                myRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            String uid = snapshot.getKey();
                            String name = snapshot.child("name").getValue(String.class);
                            Boolean requestAccepted = snapshot.child("requestAccepted").getValue(Boolean.class);
                            if (requestAccepted != null && !requestAccepted){
                                arrayList.add(name + " (" + uid + ")");
                            }
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("uid", uid);
                                jsonObject.put("name", name);
                                jsonObject.put("requestAccepted", requestAccepted);
                            } catch (JSONException e) {
                                Log.e(Constants.TAG, "JSON Error: " + e.getMessage());
                            }
                            uidRequests.put(name + " (" + uid + ")", jsonObject);
                            uidRequests1.put(uid, name + " (" + uid + ")");
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(PendingAadhaarRequest.this, "Error getting requests.", Toast.LENGTH_SHORT).show();
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
                pendingAddressUpdateRequestList.setAdapter(adapter);
                pendingAddressUpdateRequestList.setOnItemClickListener((parent, view, position, id) -> {
                    String uid = parent.getItemAtPosition(position).toString();
                    try {
                        if (!uidRequests.containsKey(uid)) {
                            Toast.makeText(PendingAadhaarRequest.this, "No Request Found." + uidRequests.toString(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONObject jsonObject = uidRequests.get(uid);
                        assert jsonObject != null;
                        boolean uidRequest = jsonObject.getBoolean("requestAccepted");
                        currentUID = jsonObject.getString("uid");
                        if (uidRequest) {
                            Toast.makeText(PendingAadhaarRequest.this, "Request already approved.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent intent = new Intent(PendingAadhaarRequest.this, AddressUpdateUrban.class);
                        intent.putExtra("uidNumber", currentUID);
                        someActivityResultLauncher.launch(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void getError(String string) {
                startActivity(new Intent(PendingAadhaarRequest.this, Login.class));
                finish();
            }
        });
    }
}
