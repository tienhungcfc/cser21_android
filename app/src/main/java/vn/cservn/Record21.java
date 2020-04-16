package vn.cservn;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.IOException;

public class Record21 {

    private MediaPlayer player = null;
    private MediaRecorder recorder = null;
    private Result result;
    private App21 app21;
    private String recordFilename = "";

    private void error(IOException e) {
        if (result != null && app21 != null) {
            result.success = false;
            result.error = e.getLocalizedMessage();
            app21.App21Result(result);
        }
    }

    public void RecordAudio(Result result, App21 app21) {
        this.result = result;
        this.app21 = app21;

        switch (result.params) {
            case "record":
                DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
                downloadFilesTask.app21 = app21;
                recordFilename = downloadFilesTask.filenameFrom("RECORD_AUDIO.mp3");
                startRecording();
                break;
            case "record_stop":
                stopRecording();
            case "play":
                startPlaying(result.params);
                break;
            case "play_stop":
                stopPlaying();
        }


    }

    private void startPlaying(String fileName) {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            // Log.e(LOG_TAG, "prepare() failed");
            error(e);
        }
    }

    private void stopPlaying() {
        if (player == null) return;
        player.release();
        player = null;
        result = null;
        app21 = null;
    }


    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFilename);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {

        }

        recorder.start();
    }

    private void stopRecording() {


        recorder.stop();
        recorder.release();
        recorder = null;

        if (result != null && app21 != null) {
            result.data = recordFilename;
            app21.App21Result(result);
        }

    }

    void release() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }
}
