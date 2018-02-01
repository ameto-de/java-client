package de.digitalernachschub.ameto.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AmetoTest {
    private static Ameto ameto;
    private static String deliveryBaseUrl;

    @Before
    public void setUp() {
        String apiUrl = System.getenv().getOrDefault("AMETO_API_URL", "http://localhost:9100");
        deliveryBaseUrl = System.getenv().getOrDefault("AMETO_DELIVERY_URL", "http://localhost:9200/");
        ameto = new Ameto(apiUrl);
    }

    @Test
    public void testAddPipelineAddsNewPipeline() {
        List<String> pipelines = ameto.getPipelines();
        String pipelineName = "anyName";

        ameto.add(pipelineName, Collections.singletonList(new Pipeline.Step("noop")));

        List<String> pipelinesAfterAdd = ameto.getPipelines();
        assertThat(pipelinesAfterAdd, hasItem(pipelineName));
        assertThat(pipelinesAfterAdd.size(), is(pipelines.size() + 1));
    }

    @Test
    public void testAddPipelineThrowsExceptionWhenOperatorIsUnknown() {
        Pipeline.Step noop = new Pipeline.Step("noop");
        Pipeline.Step unknownStep = new Pipeline.Step("unknownOperator");
        String pipelineName = "anyName2";

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ameto.add(pipelineName, Arrays.asList(noop, unknownStep, noop)))
                .withMessageContaining("unknownOperator");
        List<String> pipelines = ameto.getPipelines();
        assertThat(pipelines, not(hasItem(pipelineName)));
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
    public void getJobsReturnsWithoutError() {
        List<Job> jobs = ameto.getJobs();

        assertNotNull(jobs);
    }

    @Test
    public void testGetOperatorsReturnsWithoutError() {
        List<Operator> operators = ameto.getOperators();

        assertNotNull(operators);
    }

    @Test
    public void testAmetoProcessesJpegImage() throws InterruptedException, IOException {
        String pipelineName = "jpegTestPipeline";
        ameto.add(pipelineName, Collections.singletonList(new Pipeline.Step("noop")));
        String assetId = ameto.add(Paths.get("src/test/resources/flower.jpg"));
        Job job = new Job(assetId, pipelineName);
        ameto.add(job);
        Thread.sleep(5000L);
        OkHttpClient http = new OkHttpClient();
        String assetUrl = deliveryBaseUrl + assetId;
        Request getProcessedAsset = new Request.Builder()
                .url(assetUrl)
                .build();

        Response response = http.newCall(getProcessedAsset).execute();

        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/flower.jpg"));
        byte[] processedImageBytes = response.body().bytes();
        assertArrayEquals(imageBytes, processedImageBytes);
    }
}