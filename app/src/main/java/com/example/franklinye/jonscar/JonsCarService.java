package com.example.franklinye.jonscar;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Map;
import java.util.Random;

        import com.google.android.glass.timeline.LiveCard;
        import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.widget.CardBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.openxc.VehicleManager;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.Measurement;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.PendingIntent;
        import android.app.Service;
        import android.content.Intent;
        import android.os.Handler;
        import android.os.IBinder;
        import android.widget.RemoteViews;

public class JonsCarService extends Service {

    private static final String LIVE_CARD_TAG = "JonsCarService";
    private DatabaseReference mDatabaseReference;
    private double[][] points = {
            { 42.337018, -83.051045 },
            { 42.331517, -83.067166 },
            { 42.379595, -83.035065},
            {42.369006, -83.059527},
            {42.365074, -83.067466},
            {42.359561, -83.077733},
            {42.356260, -83.081405},
            {42.350834, -83.101002},
            {42.359752, -83.138955},
            {42.379651, -83.034468}

    };

    ValueEventListener carListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Map<String, Object> carData = (Map<String, Object>) dataSnapshot.getValue();
            try {
                mLiveCardView.setTextViewText(R.id.speed, Math.round(.62137*Double.parseDouble(carData.get("speed").toString())) + " mph");
            } catch (Exception e) {

            }

            try {
                mLiveCardView.setProgressBar(R.id.accel, 100, (int) (Double.parseDouble(carData.get("accel").toString())), false);
            } catch (Exception e) {}

            try {
                String trans = carData.get("transmission") + "";
                if (trans.equals("FIRST")) {
                    mLiveCardView.setTextViewText(R.id.gear, "1");
                } else if (trans.equals("SECOND")) {
                    mLiveCardView.setTextViewText(R.id.gear, "2");
                } else if (trans.equals("THIRD")) {
                    mLiveCardView.setTextViewText(R.id.gear, "3");
                } else if (trans.equals("FOURTH")) {
                    mLiveCardView.setTextViewText(R.id.gear, "4");
                } else if (trans.equals("FIFTH")) {
                    mLiveCardView.setTextViewText(R.id.gear, "5");
                } else if (trans.equals("REVERSE")) {
                    mLiveCardView.setTextViewText(R.id.gear, "R");
                } else {
                    mLiveCardView.setTextViewText(R.id.gear, "N");
                }
            } catch (Exception e) {

            }

            try {
                mLiveCardView.setTextViewText(R.id.fuel_consumed, String.format("%1$.1f", carData.get("fuel_consumed")) + "");
            } catch (Exception e) {}



            try {
                if (carData.get("brake").toString().equals("true")) {
                    mLiveCardView.setTextColor(R.id.brake, Color.parseColor("#FF1111"));
                } else {
                    mLiveCardView.setTextColor(R.id.brake, Color.parseColor("#000000"));
                }
            } catch (Exception e) {}

            try {
                double avx = 0;
                double avy = 0;
                double lat = Double.parseDouble(carData.get("latitude").toString());
                double lon = Double.parseDouble(carData.get("longitude").toString());
                for (double c[]: points) {
                    avx+=1/(c[0]-lat);
                    avy+=1/(c[1]-lon);
                }
                double off = 255/Math.max(Math.abs(avx),Math.abs(avy));
                if (avx>=0) {
                    mLiveCardView.setTextColor(R.id.e, Color.rgb((int)(avx*(off)), (int)(avx*(255-off)), 25));
                    mLiveCardView.setTextColor(R.id.w, Color.rgb((int)(avx*(255-off)), (int)(avx*(off)), 25));
                } else {
                    avx*=-1;
                    mLiveCardView.setTextColor(R.id.w, Color.rgb((int)(avx*(off)), (int)(avx*(255-off)), 25));
                    mLiveCardView.setTextColor(R.id.e, Color.rgb((int)(avx*(255-off)), (int)(avx*(off)), 25));
                }
                if (avy>=0) {
                    mLiveCardView.setTextColor(R.id.n, Color.rgb((int)(avy*(off)), (int)(avy*(255-off)), 25));
                    mLiveCardView.setTextColor(R.id.s, Color.rgb((int)(avy*(255-off)), (int)(avy*(off)), 25));
                } else {
                    avy*=-1;
                    mLiveCardView.setTextColor(R.id.s, Color.rgb((int)(avy*(off)), (int)(avy*(255-off)), 25));
                    mLiveCardView.setTextColor(R.id.n, Color.rgb((int)(avy*(255-off)), (int)(avy*(off)), 25));
                }
            } catch (Exception e) {}


            try {
                mLiveCard.setViews(mLiveCardView);
            } catch (Exception e) {}
            try {
                Thread.sleep(50);
            } catch (Exception e) {}
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            System.out.println("we failed");
        }
    };



    private LiveCard mLiveCard;
    private RemoteViews mLiveCardView;

    private final Handler mHandler = new Handler();

    private static final long DELAY_MILLIS = 30000;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {

            // Get an instance of a live card
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            // Inflate a layout into a remote view
            mLiveCardView = new RemoteViews(getPackageName(),
                    R.layout.jons_car);

            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(
                    this, 0, menuIntent, 0));

            // Publish the live card
            mLiveCard.publish(PublishMode.REVEAL);

            // Queue the update text runnable
            while (true) {
                try {
                    connectToFirebase();
                    break;
                } catch (Exception e) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception f) {}
                }
            }
        }
        return START_STICKY;
    }

    private void connectToFirebase() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.addValueEventListener(carListener);
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            //Stop the handler from queuing more Runnable jobs

            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }



    @Override
    public IBinder onBind(Intent intent) {
      /*
       * If you need to set up interprocess communication
       * (activity to a service, for instance), return a binder object
       * so that the client can receive and modify data in this service.
       *
       * A typical use is to give a menu activity access to a binder object
       * if it is trying to change a setting that is managed by the live card
       * service. The menu activity in this sample does not require any
       * of these capabilities, so this just returns null.
       */
        return null;
    }
}