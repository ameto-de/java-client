package de.ameto.client;

import com.fasterxml.jackson.databind.util.StdConverter;

public class AssetReferenceDeserializer extends StdConverter<String, AssetReference> {
    @Override
    public AssetReference convert(String value) {
        return new AssetReference(value);
    }
}
