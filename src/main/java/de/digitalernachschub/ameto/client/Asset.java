package de.digitalernachschub.ameto.client;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class Asset {
    String id;
    byte[] essence;
}
