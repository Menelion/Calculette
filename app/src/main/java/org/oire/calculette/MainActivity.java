package org.oire.calculette;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Locale;
import org.mariuszgromada.math.mxparser.Expression;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "OireCalculette";
    EditText expressionEditBox;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        expressionEditBox = (EditText)findViewById(R.id.expression);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    Locale loc = new Locale(Locale.getDefault().getISO3Language(), Locale.getDefault().getISO3Country());
                    int result = tts.setLanguage(loc);
                    Log.i(TAG, String.format("TTS language set to %s", loc.getDisplayName()));

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "TTS data missing or language not supported, setting to US English");
                        tts.setLanguage(Locale.US);
                    }
                }
            }
        });

        expressionEditBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence seq, int start, int before, int count) {
                String text = seq.toString();

                if (text.equals("")) return;

                int index = expressionEditBox.getSelectionStart() - 1;

                Character charToSpeak = text.charAt(index);
                tts.speak(String.valueOf(charToSpeak), TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        expressionEditBox.setOnEditorActionListener((v, actionId, event)-> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Log.d(TAG, "done button pressed");
                String expressionString = expressionEditBox.getText().toString();
                Expression expression = new Expression(expressionString);

                double result = 0.0;
                try {
                    result = expression.calculate();
                } catch (Exception e) {
                    Log.e(TAG, "Unable to calculate", e);
                }
                String resultString = String.valueOf(result);
                String message = getString(R.string.result_announcement, result);
                Log.d(TAG, String.format("Result message: %s", message));
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                expressionEditBox.setText(resultString);

                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }
}
