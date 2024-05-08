package icebucket.theia;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    static EditText emergencyNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SeekBar voiceSpeedSeekBar = findViewById(R.id.voiceSpeedSeekBar);
        emergencyNumber = findViewById(R.id.primaryContact);
        voiceSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float speed = progress / 10.0f;
                    MainActivity.tts.setSpeechRate(speed);
                    MainActivity.speak("Changing voice speed");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public static String getEmergencyNumber() {
        return emergencyNumber.getText().toString();
    }
}
