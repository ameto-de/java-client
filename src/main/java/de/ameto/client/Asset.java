package de.ameto.client;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
@EqualsAndHashCode(of={"id"})
public class Asset {
    String id;
}
