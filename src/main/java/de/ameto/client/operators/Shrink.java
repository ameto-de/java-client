package de.ameto.client.operators;

import java.util.Collections;
import java.util.List;

public class Shrink implements Operator {
    @Override
    public String getName() {
        return "shrink";
    }

    @Override
    public List<String> getConsumes() {
        return Collections.singletonList("image/jpeg");
    }
}
