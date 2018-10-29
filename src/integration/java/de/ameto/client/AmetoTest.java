package de.ameto.client;

import de.ameto.client.operators.Operator;
import de.ameto.client.operators.Resize;
import de.ameto.client.operators.Shrink;
import org.assertj.core.api.Assertions;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static de.ameto.client.Pipeline.Format.Jpeg;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AmetoTest {
    private static final long GRACE_PERIOD = 3000L;
    private static Ameto ameto;

    @Before
    public void setUp() {
        String apiUrl = System.getenv().getOrDefault("AMETO_API_URL", "http://localhost:9200");
        String apiToken = System.getenv().getOrDefault("AMETO_API_TOKEN", "anyToken");
        ameto = new Ameto(apiUrl, apiToken);
    }

    @Test
    public void testAddPipelineAddsNewPipeline() throws Exception {
        Collection<Pipeline> pipelines = ameto.getPipelines();
        String pipelineName = "anyName";

        ameto.add(pipelineName)
                .format(Jpeg)
                .build();
        Thread.sleep(1000L);

        Collection<Pipeline> pipelinesAfterAdd = ameto.getPipelines();
        assertThat(pipelinesAfterAdd, hasItem(pipelineWithName(pipelineName)));
        assertThat(pipelinesAfterAdd.size(), is(pipelines.size() + 1));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testAddPipelineThrowsExceptionWhenOperatorIsUnknown() {
        String pipelineName = "anyName2";
        Operator shrinkOperator = new Shrink();
        Operator unknownOperator = new Operator() {
            @Override
            public String getName() {
                return "unknownOperator";
            }

            @Override
            public String getVersion() {
                return "1.0.0-test";
            }

            @Override
            public List<String> getConsumes() {
                return null;
            }
        };

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ameto.add(pipelineName, shrinkOperator, unknownOperator, shrinkOperator))
                .withMessageContaining(unknownOperator.getName());
        Collection<Pipeline> pipelines = ameto.getPipelines();
        assertThat(pipelines, not(hasItem(pipelineWithName(pipelineName))));
    }

    @Test
    public void testGetPipelineReturnsWithoutError() {
        ameto.getPipelines();
    }

    @Test
    public void addedAssetEventuallyAppearsInAssetList() throws Exception {
        Asset asset = ameto.add(Paths.get("src/integration/resources/flower.jpg"));

        Thread.sleep(GRACE_PERIOD);
        Set<Asset> assetsAfterUpload = ameto.getAssets();
        Assertions.assertThat(assetsAfterUpload).contains(asset);
    }

    @Test
    public void deletedAssetNoLongerAppearsInAssetList() throws Exception {
        Asset asset = ameto.add(Paths.get("src/integration/resources/flower.jpg"));
        Thread.sleep(GRACE_PERIOD);

        ameto.remove(asset);

        Thread.sleep(GRACE_PERIOD);
        Set<Asset> assetsAfterUpload = ameto.getAssets();
        Assertions.assertThat(assetsAfterUpload).doesNotContain(asset);
    }

    @Test
    public void getJobsReturnsWithoutError() {
        List<Job> jobs = ameto.getJobs();

        assertNotNull(jobs);
    }

    @Test
    public void testAmetoProcessesJpegImage() throws InterruptedException {
        Pipeline pipeline = ameto.add("jpegTestPipeline")
                .resize(64, 64)
                .format(Jpeg)
                .build();
        Asset asset = ameto.add(Paths.get("src/integration/resources/flower.jpg"));
        Thread.sleep(GRACE_PERIOD);

        ProcessedAsset processedAsset = pipeline.push(asset);
    }

    @Test
    public void testResizeExactGivesImageWithSpecifiedDimensions() throws IOException, InterruptedException {
        final int targetWidth = 42;
        final int targetHeight = 24;
        Pipeline pipeline = ameto.add("exactResize")
                .resize(targetWidth, targetHeight, Resize.Mode.EXACT)
                .format(Jpeg)
                .build();
        Asset asset = ameto.add(Paths.get("src/integration/resources/flower.jpg"));
        Thread.sleep(GRACE_PERIOD);

        ProcessedAsset processedAsset = pipeline.push(asset);

        Thread.sleep(GRACE_PERIOD);
        BufferedImage resizedImage = ImageIO.read(processedAsset.getEssence());
        Assertions.assertThat(resizedImage.getWidth()).isEqualTo(targetWidth);
        Assertions.assertThat(resizedImage.getHeight()).isEqualTo(targetHeight);
    }

    @Test
    public void testAssetContainsProcessedAssetAsVariant() throws InterruptedException {
        Pipeline pipeline = ameto.add("jpegTestPipeline")
                .format(Jpeg)
                .build();
        Asset asset = ameto.add(Paths.get("src/integration/resources/flower.jpg"));
        Thread.sleep(GRACE_PERIOD);

        ProcessedAsset processedAsset = pipeline.push(asset);

        Thread.sleep(GRACE_PERIOD);
        Asset originalAsset = ameto.getAssets().stream()
                .filter(a -> a.getId().equals(asset.getId()))
                .findAny()
                .get();
        Assertions.assertThat(originalAsset.getVariants()).contains(processedAsset);
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