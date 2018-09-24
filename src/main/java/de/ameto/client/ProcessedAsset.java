package de.ameto.client;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.InputStream;

@Value
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class ProcessedAsset {
    String id;
    InputStream essence;
}
