package ru.ok.timer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;
    private Button startStop;
    private Button reset;
    private TimerService.TimerBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = findViewById(R.id.timer);
        startStop = findViewById(R.id.btn_start_stop);
        reset = findViewById(R.id.btn_reset);

        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ServiceConnection mConnection = new ServiceConnection() {

                    public void onServiceConnected(ComponentName className, IBinder service) {
                        binder = (TimerService.TimerBinder) service;
                        binder.attachActivity(getActivity());
                        binder.startPause();
                    }

                    public void onServiceDisconnected(ComponentName className) {
                        // 😒
                    }
                };

                Intent intent = new Intent(getApplicationContext(), TimerService.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binder != null) binder.stop();
                setTime("00:00");
            }
        });

    }

    public void setTime(final String time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerTextView.setText(time);
            }
        });
    }

    private MainActivity getActivity() {
        return this;
    }
}
