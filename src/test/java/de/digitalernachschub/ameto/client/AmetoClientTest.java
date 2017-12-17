package de.digitalernachschub.ameto.client;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class AmetoClientTest {

    @Test
    public void testAddPipelineAddsNewPipeline() {
        AmetoClient ameto = new AmetoClient();
        Pipeline pipeline = new Pipeline("anyName", Collections.singletonList(new Operator("anyOperator")));
        List<Pipeline> pipelines = ameto.getPipelines();

        ameto.add(pipeline);

        List<Pipeline> pipelinesAfterAdd = ameto.getPipelines();
        assertThat(pipelinesAfterAdd.contains(pipeline), is(true));
        assertThat(pipelinesAfterAdd.size(), is(pipelines.size() + 1));
    }

    @Test
    public void testGetPipelineReturnsWithoutError() {
        AmetoClient ameto = new AmetoClient();

        ameto.getPipelines();
    }
}