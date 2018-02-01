package de.digitalernachschub.ameto.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Pipeline {
    private final AmetoApi api;
    @Getter
    private final String name;

    public void push(String assetId) {
        Job job = new Job(assetId, getName());
        try {
            api.add(job).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
