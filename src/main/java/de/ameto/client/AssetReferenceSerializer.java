package de.ameto.client;

import com.fasterxml.jackson.databind.util.StdConverter;

public class AssetReferenceSerializer extends StdConverter<AssetReference, String> {
    @Override
    public String convert(AssetReference value) {
        return value.getId();
    }
}
