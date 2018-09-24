package de.ameto.client;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor
class AssetMetadata {
    String id;
    List<String> variants;
}
