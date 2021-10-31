package in.gov.uidai.aadhaaraddressupdate;

import static in.gov.uidai.aadhaaraddressupdate.Constants.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class NewRequest extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String uidNumber;

        if (!intent.hasExtra("uidNumber")){
            Toast.makeText(this, "Invalid UID Number.", Toast.LENGTH_SHORT).show();
        }else{
            uidNumber = intent.getStringExtra("uidNumber");
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("NewRequests/" + uidNumber);
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    String CHANNEL_ID = "address_change_notifications";
                    NotificationManager notificationManager = (NotificationManager) NewRequest.this.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        CharSequence name = "address_change";
                        String Description = "New Address Change Request.";
                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                        mChannel.setDescription(Description);
                        mChannel.enableLights(true);
                        mChannel.setLightColor(Color.RED);
                        mChannel.enableVibration(true);
                        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                        mChannel.setShowBadge(false);
                        notificationManager.createNotificationChannel(mChannel);
                    }
                    NotificationCompat.Builder builder  = new NotificationCompat.Builder(NewRequest.this, CHANNEL_ID)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("Address Change.")
                                    .setContentText("New Address Change Request");
                    int NOTIFICATION_ID = 12345;

                    Intent targetIntent = new Intent(NewRequest.this, AddressUpdateUrban.class);
                    targetIntent.putExtra("uidNumber", dataSnapshot.getKey());
                    PendingIntent contentIntent = PendingIntent.getActivity(NewRequest.this, 0, targetIntent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M? PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(contentIntent);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                }
            };
            databaseReference.startAt(new Date().getTime()).addChildEventListener(childEventListener);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
