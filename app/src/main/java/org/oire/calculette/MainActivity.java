package org.oire.calculette;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
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

        expressionEditBox.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    String expressionString = expressionEditBox.getText().toString();
                    Expression expression = new Expression(expressionString);

                    double result = 0.0;

                    try {
                        result = expression.calculate();
                        String resultString = String.valueOf(result);
                        String message = getString(R.string.result_announcement, resultString);
                        Log.d(TAG, String.format("Result message: %s", message));
                        expressionEditBox.setText(resultString);
                        expressionEditBox.setSelection(expressionEditBox.getText().length());
                        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Unable to calculate", e);
                        tts.speak(getString(R.string.error), TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                } else if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_M) {
                    Dialog pseudoMenu = new Dialog(MainActivity.this);
                    pseudoMenu.setContentView(R.layout.pseudo_popup_menu);

                    ListView menuList = pseudoMenu.findViewById(R.id.menuList);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, new String[]{"+", "-", "*", "/"});
                    menuList.setAdapter(adapter);

                    menuList.setOnKeyListener((mv, mKeyCode, mEvent) -> {
                        if (mEvent.getAction() == KeyEvent.ACTION_UP) {
                            int position = menuList.getSelectedItemPosition();
                            int newPosition = 0;
                            switch (mKeyCode) {
                                case KeyEvent.KEYCODE_DPAD_DOWN:
                                    if (position < adapter.getCount() - 1) {
                                        newPosition = position + 1;
                                    } else {
                                        newPosition = 0;
                                    }
                                    menuList.setSelection(newPosition);
                                    tts.speak(adapter.getItem(newPosition), TextToSpeech.QUEUE_FLUSH, null, null);
                                    break;
                                case KeyEvent.KEYCODE_DPAD_UP:
                                    if (position > 0) {
newPosition = position - 1;
                                    } else {
                                        newPosition = adapter.getCount() - 1;
                                    }
                                    menuList.setSelection(newPosition);
                                    tts.speak(adapter.getItem(newPosition), TextToSpeech.QUEUE_FLUSH, null, null);
                                    break;
                                case KeyEvent.KEYCODE_ENTER:
                                    String operator = adapter.getItem(position);
                                    int cursorPosition = expressionEditBox.getSelectionStart();
                                    expressionEditBox.getText().insert(cursorPosition, operator);
                                    expressionEditBox.setSelection(cursorPosition + 1);
                                    tts.speak(operator, TextToSpeech.QUEUE_FLUSH, null, null);
                                    pseudoMenu.dismiss();
                                    break;
                                case KeyEvent.KEYCODE_ESCAPE:
                                    pseudoMenu.dismiss();
                                    return true;
                            }
                        }
                        return true;
                    });

                    pseudoMenu.show();
                    tts.speak(getString(R.string.context_menu), TextToSpeech.QUEUE_FLUSH, null, null);
                    tts.speak("+", TextToSpeech.QUEUE_FLUSH, null, null);
                } else if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (expressionEditBox.getText().length() > 0) {
                        int start = Math.max(expressionEditBox.getSelectionStart(), 0);
                        String deletedChar = String.valueOf(expressionEditBox.getText().toString().charAt(start));
                        tts.speak(getString(R.string.char_deleted, deletedChar), TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    int textLength = expressionEditBox.getText().length();
                    if (textLength > 0) {
                        int start = expressionEditBox.getSelectionEnd();
                        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && start > 0) {
                            start--;
                        }
                        // Check that start is within the bounds of the text
                        if (start < textLength) {
                            String charAtCursor = String.valueOf(expressionEditBox.getText().toString().charAt(start));
                            tts.speak(charAtCursor, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                    return false;
                } else {
                    // Get the pressed key character and speak it
                    char keyChar = (char) event.getUnicodeChar();
                    tts.speak(String.valueOf(keyChar), TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }
}
