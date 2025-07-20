package net.nullved.pmweatherapi.util;

import dev.protomanly.pmweather.weather.Storm;

public enum StormType {
    SUPERCELL(0),
    TORNADO(0, 3),
    SQUALL(1),
    CYCLONE(2);

    public final int idx, stage;

    StormType(int idx, int stage) {
        this.idx = idx;
        this.stage = stage;
    }

    StormType(int idx) {
        this.idx = idx;
        this.stage = -1;
    }

    public int idx() {
        return idx;
    }

    public int stage() {
        return stage;
    }

    public boolean matches(Storm storm) {
        return storm.stormType == idx && storm.stage >= stage;
    }
}
