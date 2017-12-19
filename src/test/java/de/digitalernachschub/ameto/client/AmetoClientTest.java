package de.digitalernachschub.ameto.client;

import org.junit.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AmetoClientTest {

    @Test
    public void testAddPipelineAddsNewPipeline() {
        AmetoClient ameto = new AmetoClient("http://localhost:9100");
        Pipeline pipeline = new Pipeline("anyName", Collections.singletonList(new Operator("anyOperator")));
        List<Pipeline> pipelines = ameto.getPipelines();

        ameto.add(pipeline);

        List<Pipeline> pipelinesAfterAdd = ameto.getPipelines();
        assertThat(pipelinesAfterAdd.contains(pipeline), is(true));
        assertThat(pipelinesAfterAdd.size(), is(pipelines.size() + 1));
    }

    @Test
    public void testGetPipelineReturnsWithoutError() {
        AmetoClient ameto = new AmetoClient("http://localhost:9100");

        ameto.getPipelines();
    }

    @Test
    public void testAddAssetReturnsWithoutError() {
        AmetoClient ameto = new AmetoClient("http://localhost:9100");

        String assetUri = ameto.add(Paths.get("src/test/resources/flower.jpg"));

        assertNotNull(assetUri);
    }

    @Test
    public void testAddJobReturnsWithoutError() {
        AmetoClient ameto = new AmetoClient("http://localhost:9100");
        Job job = new Job("anyAssetId", "anyPipelineName");

        String jobId = ameto.add(job);

        assertNotNull(jobId);
    }
}