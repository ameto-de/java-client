package de.ameto.client.operators;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Resize implements Operator {
    private final String version;
    private final List<String> arguments;

    public Resize(String version, int width, int height) {
        this.version = version;
        arguments = Arrays.asList(String.valueOf(width), String.valueOf(height));
    }

    @Override
    public String getName() {
        return "resize";
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public List<String> getConsumes() {
        return Collections.singletonList("image/jpeg");
    }

    @Override
    public List<String> getArguments() {
        return Collections.unmodifiableList(arguments);
    }
}
