package icebucket.theia;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init setting button
        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(this::goToSettingsEvent);

        // init emergency call button
        Button emergencyButton = findViewById(R.id.callEmergency);
        emergencyButton.setOnClickListener(this::goToCountdownEvent);

        // init navigation button
        Button navButton = findViewById(R.id.navigateButton);
        navButton.setOnClickListener(this::goToNavigationEvent);

        // init tts
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set the language for TTS
                    int result = tts.setLanguage(Locale.getDefault());

                    // Check if the language is supported
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        // Ready to use TTS
                    }
                } else {
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public static void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }
    public static void prioritySpeak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void goToSettingsEvent(View view) {
        speak("Settings");
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    private void goToNavigationEvent(View view) {
        startActivity(new Intent(MainActivity.this, NavigationActivity.class));
    }

    private void goToCountdownEvent(View view) {
        startActivity(new Intent(MainActivity.this, CountdownActivity.class));
    }
}