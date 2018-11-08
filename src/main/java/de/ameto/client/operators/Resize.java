package de.ameto.client.operators;

import de.ameto.client.Pipeline;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Resize implements Operator {
    private final String version;
    private final List<String> arguments;

    /**
     * Initializes a resize operation using the specified target dimensions and mode.
     * The resize mode defaults to {@link Pipeline.ResizeMode#FIT}.
     * @param width Target width
     * @param height Target height
     * @param mode Resize mode
     */
    public Resize(int width, int height, Pipeline.ResizeMode mode) {
        this.version = "1.0.0";
        String modeAsString;
        switch (mode) {
            case EXACT:
                modeAsString = "exact";
                break;
            case FILL:
                modeAsString = "fill";
                break;
            case FIT:
                modeAsString = "fit";
                break;
            default:
                throw new IllegalArgumentException("Unsupported value for mode: " + mode);
        }
        arguments = Arrays.asList(
                String.valueOf(width),
                String.valueOf(height),
                "--mode", modeAsString
        );
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
