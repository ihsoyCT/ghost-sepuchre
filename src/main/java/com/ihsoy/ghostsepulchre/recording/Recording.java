package com.ihsoy.ghostsepulchre.recording;
import com.ihsoy.ghostsepulchre.Tick;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;

@Slf4j
public class Recording implements Comparable<Recording> {
    @Getter
    private Tick start;
    @Getter
    private Tick end;
    @Getter
    private boolean done = false;
    @Getter
    private final ArrayList<Tile> points;
    @Getter
    private Tile firstTile;


    public Recording() {
        points = new ArrayList<>();
    }

    public void startRecording(Client client) {
        if(done) throw new RuntimeException("Recording was already finalized.");
        if(start != null) throw new RuntimeException("Recording has already started.");
        start = new Tick();
        end = new Tick();
        firstTile = getTile(client);
    }
    /**
     * Runs every tick once a run has been started
     */
    public void run(Client client) {
        if(done) throw new RuntimeException("Recording was already finalized.");
        if(start == null) throw new RuntimeException("Recording has not been started.");
        storeCurrentTile(client);
        end.inc();
    }

    private void storeCurrentTile(Client client) {
        Tile point = getTile(client);
        points.add(point);
    }

    private Tile getTile(Client client) {
        final LocalPoint lp = client.getLocalPlayer().getLocalLocation();
        final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, lp);

        Tile point = new Tile(worldPoint.getRegionID(), worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane());

        log.debug("Adding Point: {} - {}", point, worldPoint);
        return point;
    }

    public void stopRecording() {
        done = true;
    }

    public int recordingSize() {
        return points.size();
    }

    @Override
    public int compareTo(Recording o) {
        if(!done || !o.done) throw new RuntimeException("Both Recording have to be finalized.");
        return start.compareTo(o.start);
    }

    @Override
    public String toString() {
        return points.get(0).toString() + " Size: " + points.size();
    }

}
