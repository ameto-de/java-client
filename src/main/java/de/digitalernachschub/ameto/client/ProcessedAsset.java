package de.digitalernachschub.ameto.client;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.InputStream;

@Value
@RequiredArgsConstructor
public class ProcessedAsset {
    String id;
    InputStream essence;
}
