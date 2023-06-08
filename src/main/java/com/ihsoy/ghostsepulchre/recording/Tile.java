package com.ihsoy.ghostsepulchre.recording;

import lombok.Value;

@Value
public class Tile {
    private int regionId;
    private int regionX;
    private int regionY;
    private int z;

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(regionId)
                .append(regionX)
                .append(regionY)
                .append(z);
        return sb.toString();
    }
}
