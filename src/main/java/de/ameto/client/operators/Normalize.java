package de.ameto.client.operators;

import java.util.Collections;
import java.util.List;

public class Normalize implements Operator {
    @Override
    public String getName() {
        return "normalize";
    }

    @Override
    public String getVersion() {
        return "0.1.0";
    }

    @Override
    public List<String> getConsumes() {
        return Collections.singletonList("image/jpeg");
    }
}
