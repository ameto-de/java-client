package de.ameto.client.operators;

import java.util.Collections;
import java.util.List;

public class Shrink implements Operator {
    private final String version;

    public Shrink(String version) {
        this.version = version;
    }

    @Override
    public String getName() {
        return "shrink";
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public List<String> getConsumes() {
        return Collections.singletonList("image/jpeg");
    }
}
