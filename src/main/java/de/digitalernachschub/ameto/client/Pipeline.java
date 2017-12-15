package de.digitalernachschub.ameto.client;

import lombok.Value;

import java.util.List;

@Value
public class Pipeline {
    String name;
    List<Operator> operators;
}
