package com.ihsoy.ghostsepulchre;

public class Tick implements Comparable<Tick> {
    public static float TICK_DURATION = 0.6f;
    private long tick;

    public Tick() {
        tick = 0;
    }

    public long getTick() {
        return tick;
    }

    public float getSeconds() {
        return tick * TICK_DURATION;
    }

    public void inc() {
        ++tick;
    }

    @Override
    public int compareTo(Tick b) {
        if(tick > b.tick)
            return 1;
        else if (tick < b.tick)
            return -1;
        return 0;
    }
}
