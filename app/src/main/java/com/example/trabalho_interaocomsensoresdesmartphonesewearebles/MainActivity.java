package com.example.trabalho_interaocomsensoresdesmartphonesewearebles;

import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private AudioManager audioManager;
    private AudioHelper audioHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicialização
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioHelper = new AudioHelper(this);

        // Verificação inicial
        boolean isBluetoothConnected =
                audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP);

        // Se NÃO tiver Bluetooth, abre configurações
        if (!isBluetoothConnected) {
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("EXTRA_CONNECTION_ONLY", true);
            intent.putExtra("EXTRA_CLOSE_ON_CONNECT", true);
            intent.putExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 1);

            startActivity(intent);
        }

        // Callback de detecção dinâmica
        audioManager.registerAudioDeviceCallback(new AudioDeviceCallback() {

            @Override
            public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
                super.onAudioDevicesAdded(addedDevices);

                if (audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    // Bluetooth conectado
                }
            }

            @Override
            public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
                super.onAudioDevicesRemoved(removedDevices);

                if (!audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    // Bluetooth desconectado
                }
            }

        }, null);
    }
}