package de.digitalernachschub.ameto.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Job {
    String id;
    String asset;
    String pipeline;
}
