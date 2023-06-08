package com.ihsoy.ghostsepulchre.recording;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Playback {
    ArrayList<WorldPoint> points;

    @Getter
    private boolean finished;
    private int tick_counter ;
    public Playback(Recording recording, Client client) {
        tick_counter = 0;
        ArrayList<Tile> tiles = recording.getPoints();
        List<WorldPoint> temp;
        if(tiles.isEmpty()) {
            temp = Collections.emptyList();
        }
        else {
            temp =  tiles.stream()
                    .map(tile -> WorldPoint.fromRegion(tile.getRegionId(), tile.getRegionX(), tile.getRegionY(), tile.getZ()))
                    .flatMap(worldPoint ->
                    {
                        final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, worldPoint);
                        return localWorldPoints.stream();
                    })
                    .collect(Collectors.toList());
        }
        points = new ArrayList<>(temp);
    }

    public WorldPoint getPoint() {
        if(finished) return points.get(points.size() - 1);
        return points.get(tick_counter);
    }

    public WorldPoint nextPoint() {
        tick_counter++;
        if(tick_counter >= points.size()) {
            finished = true;
        }

        return getPoint();
    }

    public void rewind() {
        tick_counter = 0;
    }
}
