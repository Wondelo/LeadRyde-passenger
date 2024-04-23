package com.general.call;

public interface V3CallListener {

    void onCallEnded();

    void onCallEstablished();

    void onCallProgressing();

    void onUIChanges();

    void onMuteView(boolean isTrue);

    void onSpeakerView(boolean isTrue);

    void onCameraView(String data, boolean isTrue);

    void onEstablishedAfterUI();
}