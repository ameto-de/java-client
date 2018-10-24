package de.ameto.client.operators;

import java.util.Arrays;
import java.util.List;

public class Shrink implements Operator {
    private final String version;

    /**
     * Initializes a shrink operator.
     */
    public Shrink() {
        this.version = "1.1.0";
    }

    /**
     * Inizializes a shrink operator of the specified version.
     * @param version Operator version
     * @deprecated Explicit operator versioning will be removed in the future.
     * Please use {@link Shrink#Shrink()} instead.
     */
    @Deprecated
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
        return Arrays.asList("image/jpeg", "image/png");
    }
}
