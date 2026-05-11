package com.example.trabalho_interaocomsensoresdesmartphonesewearebles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private AudioManager audioManager;
    private AudioHelper audioHelper;
    private TextToSpeech tts;
    private TextView statusText;
    private boolean usarTTS = false;

    private void falar(String mensagem) {
        Log.d(TAG, "Falando: " + mensagem);
        runOnUiThread(() -> statusText.setText(mensagem));

        if (usarTTS && tts != null) {
            tts.speak(mensagem, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            // Fallback: MediaPlayer com arquivo de áudio
            MediaPlayer mp = MediaPlayer.create(this, R.raw.iniciado);
            if (mp != null) {
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);
            } else {
                Log.e(TAG, "MediaPlayer também falhou");
            }
        }
    }

    private void verificarSaidaDeAudio() {
        boolean temSpeaker =
                audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
        boolean temBluetooth =
                audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP);

        Log.d(TAG, "Speaker embutido: " + temSpeaker);
        Log.d(TAG, "Bluetooth A2DP: " + temBluetooth);

        if (temSpeaker || temBluetooth) {
            falar("Sistema iniciado com sucesso");
        } else {
            falar("Conecte um fone Bluetooth");
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioHelper = new AudioHelper(this);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("pt", "BR"));
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "pt-BR indisponível, usando inglês");
                    tts.setLanguage(Locale.US);
                }
                tts.setSpeechRate(1.0f);
                tts.setPitch(1.0f);
                usarTTS = true;
                Log.d(TAG, "TTS inicializado com sucesso");
                verificarSaidaDeAudio();
            } else {
                Log.e(TAG, "TTS falhou, usando MediaPlayer");
                usarTTS = false;
                verificarSaidaDeAudio();
            }
        });

        audioManager.registerAudioDeviceCallback(new AudioDeviceCallback() {
            @Override
            public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
                if (audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    falar("Fone Bluetooth conectado");
                }
            }

            @Override
            public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
                if (!audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    falar("Fone Bluetooth desconectado");
                }
            }
        }, null);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}