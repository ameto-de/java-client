package de.digitalernachschub.ameto.client;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AmetoClientTest {
    private static AmetoClient ameto;

    @Before
    public void setUp() {
        String apiUrl = System.getenv().getOrDefault("AMETO_API_URL", "http://localhost:9100");
        ameto = new AmetoClient(apiUrl);
    }

    @Test
    public void testAddPipelineAddsNewPipeline() {
        Pipeline pipeline = new Pipeline("anyName", Collections.singletonList("anyOperator"));
        List<Pipeline> pipelines = ameto.getPipelines();

        ameto.add(pipeline);

        List<Pipeline> pipelinesAfterAdd = ameto.getPipelines();
        assertThat(pipelinesAfterAdd.contains(pipeline), is(true));
        assertThat(pipelinesAfterAdd.size(), is(pipelines.size() + 1));
    }

    @Test
    public void testGetPipelineReturnsWithoutError() {
        ameto.getPipelines();
    }

    @Test
    public void testAddAssetReturnsWithoutError() {
        String assetUri = ameto.add(Paths.get("src/test/resources/flower.jpg"));

        assertNotNull(assetUri);
    }

    @Test
    public void testAddJobReturnsWithoutError() {
        Job job = new Job("anyAssetId", "anyPipelineName");

        String jobId = ameto.add(job);

        assertNotNull(jobId);
    }

    @Test
    public void testGetOperatorsReturnsWithoutError() {
        List<Operator> operators = ameto.getOperators();

        assertNotNull(operators);
    }
}