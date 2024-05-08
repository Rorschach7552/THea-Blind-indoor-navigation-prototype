package icebucket.theia;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class CountdownActivity extends AppCompatActivity {

    private CountDownTimer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        TextView digitView = findViewById(R.id.digitView);

        MainActivity.speak("Countdown to emergency call. Tap screen to cancel");
        sleep(3);
        timer = new CountDownTimer(6000, 1000) {
            int i = 5;
            @Override
            public void onTick(long millisUntilFinished) {
                MainActivity.speak(String.valueOf(i));
                digitView.setText(String.valueOf(i));
                i--;
            }

            @Override
            public void onFinish() {
                startActivity(new Intent(CountdownActivity.this, EmergencyCallActivity.class));
            }
        };

        timer.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            timer.cancel();
            MainActivity.prioritySpeak("Canceled");
            startActivity(new Intent(CountdownActivity.this, MainActivity.class));
        }

        return true;
    }
    private void sleep(int i) {
        try {
            TimeUnit.SECONDS.sleep(i);} catch (InterruptedException e) {}
    }
}
