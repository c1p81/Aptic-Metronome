package com.luca_innocenti.apticmetro;

import android.icu.lang.UCharacter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
//import android.widget.NumberPicker;
import com.shawnlin.numberpicker.NumberPicker;

import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;



// Applicazione Mobile
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

    private static final String TAG = "MainActivity";
    private GoogleApiClient googleApiClient;

    private ImageButton play;
    private NumberPicker bpm;
    private int counter;

    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //play = (ImageButton) findViewById(R.id.imageButton);
        bpm = (NumberPicker) findViewById(R.id.number_picker);


        bpm.setMinValue(40);
        bpm.setMaxValue(150);
        bpm.setValue(80);
        bpm.setWrapSelectorWheel(false);


        bpm.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Display the newly selected number from picker
                Log.d(TAG,Integer.toString(newVal));
                String sendMessage = Integer.toString(newVal);
                new SendToDataLayerThread("/path", sendMessage).start();
            }
        });



        /*play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Inviato", Toast.LENGTH_SHORT).show();
                String sendMessage = "Start";
                counter++;

                new SendToDataLayerThread("/path", sendMessage).start();

                Log.d(TAG, "SendToDataLayerThread()");


            }
        });*/
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        googleApiClient.connect();
    }

    // data layer connection
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected()");
    }

    // Activity stop
    @Override
    protected void onStop() {
        if (null != googleApiClient && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();

        Log.d(TAG, "onStop()");

    }

    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

    private class SendToDataLayerThread extends Thread{
        String path;
        String handheldMessage;

        public SendToDataLayerThread(String pth, String message) {
            //path = "/messaggio";
            path = pth;
            handheldMessage = message;
        }
        public void run() {
            Log.d(TAG, "SendToDataLayerThread()");

            NodeApi.GetConnectedNodesResult nodeResult = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            for (Node node : nodeResult.getNodes()) {
                MessageApi.SendMessageResult result =
                        Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, handheldMessage.getBytes()).await();

                if (result.getStatus().isSuccess()) {
                    Log.d(TAG, "To: " + node.getDisplayName());
                    Log.d(TAG, "Message = " + handheldMessage );
                }
                else {
                    Log.d(TAG, "Send error");
                }
            }
        }
    }
}
