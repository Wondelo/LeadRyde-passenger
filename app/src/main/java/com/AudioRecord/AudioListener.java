package com.AudioRecord;

/**
 * @author netodevel
 */
public interface AudioListener {

    void onStop(RecordingItem recordingItem);

    void onCancel();

    void onError(Exception e);
}
