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
    public void testAddAssetReturnsWithoutError() {
        Asset asset = ameto.add(Paths.get("src/test/resources/flower.jpg"));

        assertNotNull(asset);
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
        Pipeline pipeline = ameto.add("jpegTestPipeline", Collections.singletonList("noop"));
        Asset asset = ameto.add(Paths.get("src/test/resources/flower.jpg"));
        pipeline.push(asset);
        Thread.sleep(5000L);
        OkHttpClient http = new OkHttpClient();
        String assetUrl = deliveryBaseUrl + asset.getId();
        Request getProcessedAsset = new Request.Builder()
                .url(assetUrl)
                .build();

        Response response = http.newCall(getProcessedAsset).execute();

        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/flower.jpg"));
        byte[] processedImageBytes = response.body().bytes();
        assertArrayEquals(imageBytes, processedImageBytes);
    }
}