package de.digitalernachschub.ameto.client;

import lombok.val;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
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
        ameto = new Ameto(apiUrl, "anyToken");
    }

    @Test
    public void testAddPipelineAddsNewPipeline() {
        Collection<Pipeline> pipelines = ameto.getPipelines();
        String pipelineName = "anyName";

        ameto.add(pipelineName, Collections.singletonList("noop"));

        Collection<Pipeline> pipelinesAfterAdd = ameto.getPipelines();
        assertThat(pipelinesAfterAdd, hasItem(pipelineWithName(pipelineName)));
        assertThat(pipelinesAfterAdd.size(), is(pipelines.size() + 1));
    }

    @Test
    public void testAddPipelineThrowsExceptionWhenOperatorIsUnknown() {
        String pipelineName = "anyName2";
        String unknownOperatorName = "unknownOperator";

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ameto.add(pipelineName, Arrays.asList("noop", unknownOperatorName, "noop")))
                .withMessageContaining(unknownOperatorName);
        Collection<Pipeline> pipelines = ameto.getPipelines();
        assertThat(pipelines, not(hasItem(pipelineWithName(pipelineName))));
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
        List<Job> jobs = ameto.getJobs();

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

        Future<ProcessedAsset> processedAsset = pipeline.push(asset);

        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/flower.jpg"));
        assertArrayEquals(imageBytes, processedAsset.get().getEssence());
    }

    private static Matcher<Pipeline> pipelineWithName(String name) {
        return new TypeSafeDiagnosingMatcher<Pipeline>() {
            @Override
            protected boolean matchesSafely(Pipeline item, Description mismatchDescription) {
                mismatchDescription.appendText("Pipeline identified by ").appendText(name);
                return name.equals(item.getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Pipeline identified by ").appendText(name);
            }
        };
    }
}