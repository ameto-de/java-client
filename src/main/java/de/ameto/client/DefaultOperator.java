package de.ameto.client;

import de.ameto.client.operators.Operator;
import lombok.Value;

import java.util.List;

@Value
class DefaultOperator implements Operator {
    String name;
    String version;
    List<String> consumes;
    List<String> arguments;
}
