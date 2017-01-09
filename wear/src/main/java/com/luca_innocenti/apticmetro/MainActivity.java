package com.luca_innocenti.apticmetro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.os.Handler;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;



// Lato orologio
public class MainActivity extends WearableActivity {

    private BoxInsetLayout mContainerView;
    private NumberPicker bpm;
    private ImageButton play;
    private int stato;
    long startTime = 0;
    long valore_bpm;

    private String receivedMessage = null;
    private String message = "";
    private static final String TAG = "WearMainActivity";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        Log.v(TAG, "onCreate()");
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(messageReceiver, messageFilter);


        final Handler timerHandler = new Handler();
        final Runnable timerRunnable = new Runnable() {

            @Override
            public void run() {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate((100));
                timerHandler.postDelayed(this, valore_bpm);
            }
        };


        stato = 0;

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        bpm = (NumberPicker) findViewById(R.id.numberPicker);
        play =(ImageButton) findViewById(R.id.imageButton);

        bpm.setMinValue(40);
        bpm.setMaxValue(150);
        bpm.setValue(80);
        bpm.setWrapSelectorWheel(false);

        play.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.d("Aptic","BPM "+bpm.getValue());
                valore_bpm = Math.round((60.0/bpm.getValue())*1000.0);
                Log.d("Valore bmp millisecondi","BMP3 "+valore_bpm);
                if (stato == 0) {
                    play.setImageResource(R.drawable.stop_icon);
                    stato = 1;
                    startTime = System.currentTimeMillis();
                    //Toast.makeText(MainActivity.this, Long.toString(valore_bpm), Toast.LENGTH_SHORT).show();
                    timerHandler.postDelayed(timerRunnable, 0);
                }
                else
                {
                    play.setImageResource(R.drawable.play_icon);
                    stato = 0;
                    timerHandler.removeCallbacks(timerRunnable);

                }
            }
        });

    }

    public class MessageReceiver extends BroadcastReceiver {
        private static final String TAG = "MessageReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            receivedMessage = intent.getStringExtra("message");
            Log.d("MessageReceiver", "onReceive() receivedMessage = "+receivedMessage);

            if (receivedMessage != null) {
                // Display message in UI
                message = receivedMessage;
                Log.d("MessageReceiver", "receivedMessage =" + receivedMessage);
                bpm.setValue(Integer.parseInt(receivedMessage));
            } else {
                receivedMessage = "No Message";
                Log.d("MessageReceiver", "receivedMessage = No Message");
            }


        }
    }




    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            play.setVisibility(View.INVISIBLE);
        } else {
            mContainerView.setBackground(null);
            play.setVisibility(View.VISIBLE);
        }
    }




}

