package com.ihsoy.ghostsepulchre.recording;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Set;

import static com.ihsoy.ghostsepulchre.GhostSepulchrePlugin.CONFIG_GROUP;
import static com.ihsoy.ghostsepulchre.GhostSepulchrePlugin.RECORDING_KEY;

@Slf4j
public class StateHandler {
    public final static int SEPULCHRE_TIMER = 10413;
    public final static int SEPULCHRE_LOADING_SCREEN = 6719;
    public final static Set<Integer> SEPULCHRE_MAP_REGIONS = ImmutableSet.of(8797,10077,9308,10074,9050);
    private Recording currentRecording;

    // TODO: Use hashCode() instead of using String to identify recordings
    private HashMap<String, Recording> recordingStorage = new HashMap<>();


    private final Client client;
    @Inject
    private ConfigManager configManager;
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
        if(!isPlayerInSepulchre()) return ;
        if(varbitChanged.getVarbitId() == SEPULCHRE_TIMER) {
            if(varbitChanged.getValue() == 1) {
                state = STATE.RUN_ENDED;
                endRun();
            }
        }
        if(varbitChanged.getVarbitId() == SEPULCHRE_LOADING_SCREEN) {
            if(varbitChanged.getValue() == 0) {
                log.info("10413: "+ client.getVarbitValue(10413));
                state = STATE.RUN_STARTED;
                startRun();
            } else {
                state = STATE.OUT_OF_SEPULCHRE;
                reset();
            }
        }
    }

    private void endRun() {
        stopRecording();
        if(isRecordingFasterThanPlayback()) {
            storeRecording();
            saveRecordings();
            printGameMessage(true);
        } else {
            printGameMessage(false);
        }
    }

    private void startRun() {
        log.info("STATE: RUN STARTED");
        startRecording();
        loadPlayback();
        state = STATE.RUN_ACTIVE;
    }

    private boolean isPlayerInSepulchre() {
        final int[] mapRegions = client.getMapRegions();

        for(int region: mapRegions) {
            if(SEPULCHRE_MAP_REGIONS.contains(region)) {
                return true;
            }
        }
        return false;
    }
    
    public void run() {
        if(state == STATE.RUN_ACTIVE) {
            log.info("ACTIVE");
            storeCurrentTile();
            if(currentPlayback != null) {
                currentPlaybackPoint = currentPlayback.getPoint();
                currentPlayback.nextPoint();
                if(isPlaybackFinished()) {
                    printYouLostMessage();
                }
            }
        }
    }

    private void startRecording() {
        log.info("RECORDING STARED");
        currentRecording = new Recording();
        currentRecording.startRecording(client);
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

    public void saveRecordings() {
        String json = recordingsToJson();
        configManager.setConfiguration(CONFIG_GROUP, RECORDING_KEY, json);

        log.debug("Recordings Saved: {}", recordingStorage.size());
    }

    public void loadRecordings() {
        String json = configManager.getConfiguration(CONFIG_GROUP, RECORDING_KEY);
        loadJson(json);
        log.info("Recordings Loaded: {}", recordingStorage.size());
    }

    private String recordingsToJson() {
        Gson gson = new Gson();
        return gson.toJson(recordingStorage);
    }

    private void loadJson(String json) {
        if(json == null || json.length() == 0) {
            return;
        }
        Gson gson = new Gson();
        try {
            recordingStorage = new HashMap<>();
            recordingStorage = gson.fromJson(json, new TypeToken<HashMap<String, Recording>>(){}.getType());
            recordingStorage.forEach((k,v) -> {
                log.info("Value: {} Size: {} Hash: {}", k, v.getPoints().size(), k.hashCode());
            });
        }
        catch (Exception e) {
            log.error("Could not load Recordings", e);
            //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ef1020>Could not load recordings!</col>", null);
        }
    }
}
