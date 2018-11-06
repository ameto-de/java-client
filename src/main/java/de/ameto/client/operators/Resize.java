package de.ameto.client.operators;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Resize implements Operator {
    private final String version;
    private final List<String> arguments;

    /**
     * Specifies the mode of the resize operation.
     */
    public enum Mode {
        /**
         * Resizes the input image exactly to the specified dimensions.
         */
        EXACT,
        /**
         * Resizes the input image while preserving the aspect ratio and making the
         * image at least as large as the specified dimensions.
         */
        FILL,
        /**
         * Resizes the input image while preserving the aspect ratio and making the
         * image no larger than the specified dimensions.
         */
        FIT
    }

    /**
     * Initializes a resize operation using the specified target dimensions.
     * The resize mode defaults to {@link Mode#FIT}.
     * @param width Target width
     * @param height Target height
     */
    public Resize(int width, int height) {
        this(width, height, Mode.FIT);
    }

    /**
     * Initializes a resize operation using the specified target dimensions and mode.
     * The resize mode defaults to {@link Mode#FIT}.
     * @param width Target width
     * @param height Target height
     * @param mode Resize mode
     */
    public Resize(int width, int height, Mode mode) {
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
