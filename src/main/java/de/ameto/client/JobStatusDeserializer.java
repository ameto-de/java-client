package de.ameto.client;

import com.fasterxml.jackson.databind.util.StdConverter;

class JobStatusDeserializer extends StdConverter<Integer, Job.Status> {
    @Override
    public Job.Status convert(Integer status) {
        switch (status) {
            case 0:
                return Job.Status.Pending;
            case 1:
                return Job.Status.InProgress;
            case 2:
                return Job.Status.Finished;
            case 3:
                return Job.Status.Failed;
            default:
                throw new IllegalArgumentException("Unknown job status: " + status);
        }
    }
}
