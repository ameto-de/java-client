package de.ameto.client;

import com.fasterxml.jackson.databind.util.StdConverter;

class AssetReferenceDeserializer extends StdConverter<String, AssetReference> {
    @Override
    public AssetReference convert(String value) {
        if (value == null) {
            return null;
        }
        return new AssetReference(value);
    }
}
