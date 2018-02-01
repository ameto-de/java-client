package de.digitalernachschub.ameto.client;

import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AmetoTest {
    private static Ameto ameto;

    @Before
    public void setUp() {
        String apiUrl = System.getenv().getOrDefault("AMETO_API_URL", "http://localhost:9100");
        ameto = new Ameto(apiUrl);
    }

    @Test
    public void testAddPipelineAddsNewPipeline() {
        List<String> pipelines = ameto.getPipelines();
        String pipelineName = "anyName";

        ameto.add(pipelineName, Collections.singletonList("noop"));

        List<String> pipelinesAfterAdd = ameto.getPipelines();
        assertThat(pipelinesAfterAdd, hasItem(pipelineName));
        assertThat(pipelinesAfterAdd.size(), is(pipelines.size() + 1));
    }

    @Test
    public void testAddPipelineThrowsExceptionWhenOperatorIsUnknown() {
        String pipelineName = "anyName2";
        String unknownOperatorName = "unknownOperator";

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ameto.add(pipelineName, Arrays.asList("noop", unknownOperatorName, "noop")))
                .withMessageContaining(unknownOperatorName);
        List<String> pipelines = ameto.getPipelines();
        assertThat(pipelines, not(hasItem(pipelineName)));
    }

    @Test
    public void testGetPipelineReturnsWithoutError() {
        ameto.getPipelines();
    }

    @Test
    public void testAddAssetReturnsWithoutError() throws ExecutionException, InterruptedException {
        val assetFuture = ameto.add(Paths.get("src/test/resources/flower.jpg"));

        val asset = assetFuture.get();
        assertNotNull(asset);
    }

    @Test
    public void getJobsReturnsWithoutError() {
        List<String> jobs = ameto.getJobs();

        assertNotNull(jobs);
    }

    @Test
    public void testGetOperatorsReturnsWithoutError() {
        List<Operator> operators = ameto.getOperators();

        assertNotNull(operators);
    }

    @Test
    public void testAmetoProcessesJpegImage() throws InterruptedException, IOException, ExecutionException {
        Pipeline pipeline = ameto.add("jpegTestPipeline", Collections.singletonList("noop"));
        Future<Asset> uploadResult = ameto.add(Paths.get("src/test/resources/flower.jpg"));
        Asset asset = uploadResult.get();

        Future<Asset> processedAsset = pipeline.push(asset);

        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/flower.jpg"));
        assertArrayEquals(imageBytes, processedAsset.get().getEssence());
    }
}