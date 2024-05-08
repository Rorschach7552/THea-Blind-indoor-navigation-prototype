package icebucket.theia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EmergencyCallActivity extends AppCompatActivity {
    private final Handler handler = new Handler();
    private int repeatCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repeatCount = 0;
        setContentView(R.layout.activity_emergencycall);
        TextView number = findViewById(R.id.emergencyNumber);
        number.setText(SettingsActivity.getEmergencyNumber());
        scheduleNextAnnouncement();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handler.removeCallbacksAndMessages(null);
            MainActivity.prioritySpeak("Canceled");
            startActivity(new Intent(EmergencyCallActivity.this, MainActivity.class));
        }

        return true;
    }

    private void scheduleNextAnnouncement() {
        if (repeatCount < 4) {
            MainActivity.speak("Calling emergency number");
            repeatCount++;
            handler.postDelayed(this::scheduleNextAnnouncement, 4000); // 5000 milliseconds delay
        }
    }
}
