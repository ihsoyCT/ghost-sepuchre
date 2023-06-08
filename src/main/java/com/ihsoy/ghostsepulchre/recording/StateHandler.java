package com.ihsoy.ghostsepulchre.recording;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.VarbitChanged;

import javax.inject.Inject;
import java.util.HashMap;

@Slf4j
public class StateHandler {
    public final static int SEPULCHRE_TIMER = 10413;
    public final static int SEPULCHRE_STARTED = 6719;
    private Recording currentRecording;

    // TODO: Use hashCode() instead of using String to identify recordings
    private HashMap<String, Recording> recordingStorage = new HashMap<>();


    private final Client client;
    private Playback currentPlayback;

    @Getter
    private WorldPoint currentPlaybackPoint;

    @Inject
    public StateHandler(Client client) {
        this.client = client;
        this.state = STATE.OUT_OF_SEPULCHRE;
    }

    public enum STATE {
        IN_LOBBY,
        RUN_STARTED,
        RUN_ACTIVE,
        RUN_ENDED,
        OUT_OF_SEPULCHRE
    }

    private STATE state;

    public void changeState(VarbitChanged varbitChanged) {
        if(varbitChanged.getVarbitId() == SEPULCHRE_TIMER) {
            if(varbitChanged.getValue() == 1) {
                state = STATE.RUN_ENDED;
            }
        }
        if(varbitChanged.getVarbitId() == SEPULCHRE_STARTED) {
            if(varbitChanged.getValue() == 0) {
                state = STATE.RUN_STARTED;
            } else {
                state = STATE.OUT_OF_SEPULCHRE;
            }
        }
    }
    
    public void run() {
        switch (state) {
            case IN_LOBBY:
                log.info("STATE: IN LOBBY");
                break;
            case RUN_STARTED:
                log.info("STATE: RUN STARTED");
                startRecording();
                loadPlayback();
                state = STATE.RUN_ACTIVE;
            case RUN_ACTIVE:
                log.info("STATE: RUN ACTIVE");
                storeCurrentTile();
                if(currentPlayback != null) {
                    currentPlaybackPoint = currentPlayback.getPoint();
                    currentPlayback.nextPoint();
                    if(isPlaybackFinished()) {
                        printYouLostMessage();
                    }
                }
                break;
            case RUN_ENDED:
                log.info("STATE: RUN ENDED");
                stopRecording();
                if(isRecordingFasterThanPlayback()) {
                    storeRecording();
                    printGameMessage(true);
                } else {
                    printGameMessage(false);
                }
                break;
            case OUT_OF_SEPULCHRE:
                log.info("STATE: OUT OF SEPULCHRE");
                stopRecording();
                reset();
                break;                
        }
    }

    private void startRecording() {
        log.info("RECORDING STARED");
        currentRecording = new Recording();
    }

    private void storeCurrentTile() {
        log.info("storeCurrentTile");
        currentRecording.run(client);
    }

    private void stopRecording() {
        log.info("stopRecording");
        currentRecording.stopRecording();
    }

    private boolean isRecordingFasterThanPlayback() {
        log.info("recordingFasterThanPlayback");
        Tile startPoint = currentRecording.getFirstTile();
        Recording stored = recordingStorage.getOrDefault(startPoint.toString(), null);
        if(stored != null) {
            return currentRecording.compareTo(stored) >= 0;
        }

        return true;
    }

    private void storeRecording() {
        log.info("storeRecording");
        recordingStorage.put(currentRecording.getFirstTile().toString(), currentRecording);
    }

    private void printGameMessage(boolean faster) {
        log.info("printGameMessage");
        if(faster) log.info("Faster!");
        else log.info("Slower");
    }

    private void loadPlayback() {
        log.info("loadPlayback");
        Recording playbackRecording = recordingStorage.getOrDefault(currentRecording.getFirstTile().toString(), null);
        if(playbackRecording != null) {
            currentPlayback = new Playback(playbackRecording, client);
        }
        else {
            currentPlayback = null;
        }
    }
    
    private boolean isPlaybackFinished() {
        log.info("playbackIsFinished");
        return currentPlayback.isFinished();
    }

    private void printYouLostMessage() {
        log.info("YOU LOST");
    }
    private void reset() {
        log.info("reset");
        currentPlaybackPoint = null;
        currentPlayback = null;
        currentRecording = null;
    }
}
