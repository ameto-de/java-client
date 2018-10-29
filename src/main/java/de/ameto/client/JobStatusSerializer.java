package de.ameto.client;

import com.fasterxml.jackson.databind.util.StdConverter;

public class JobStatusSerializer extends StdConverter<Job.Status, Integer> {
    @Override
    public Integer convert(Job.Status status) {
        switch (status) {
            case Pending:
                return 0;
            case InProgress:
                return 1;
            case Finished:
                return 2;
            case Failed:
                return 3;
            default:
                throw new IllegalArgumentException("Unknown job status: " + status);
        }
    }
}
