package de.ameto.client;

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
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AmetoTest {
    private static final long GRACE_PERIOD = 5000L;
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

        ameto.addPipeline(pipelineName)
                .format(Jpeg)
                .build();
        Thread.sleep(GRACE_PERIOD);

        Collection<Pipeline> pipelinesAfterAdd = ameto.getPipelines();
        assertThat(pipelinesAfterAdd, hasItem(pipelineWithName(pipelineName)));
        assertThat(pipelinesAfterAdd.size(), is(pipelines.size() + 1));
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
        Pipeline pipeline = ameto.addPipeline("jpegTestPipeline")
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
        Pipeline pipeline = ameto.addPipeline("exactResize")
                .resize(targetWidth, targetHeight, Pipeline.ResizeMode.EXACT)
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
    public void testAutoOrient() throws IOException, InterruptedException {
        Pipeline pipeline = ameto.addPipeline("autoOrient")
                .autoOrient()
                .format(Jpeg)
                .build();
        Asset asset = ameto.add(Paths.get("src/integration/resources/flower.jpg"));
        Thread.sleep(GRACE_PERIOD);

        ProcessedAsset processedAsset = pipeline.push(asset);
    }

    @Test
    public void testAssetContainsProcessedAssetAsVariant() throws InterruptedException {
        Pipeline pipeline = ameto.addPipeline("jpegTestPipeline")
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