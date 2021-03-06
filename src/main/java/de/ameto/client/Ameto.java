package de.ameto.client;

import de.ameto.client.operators.Operator;
import lombok.val;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * Represents the Ameto client.
 * Most interaction with the Ameto API will be perfomed using an instance of this client. It allows to manage assets,
 * processed assets and pipelines as well as querying the jobs that have been triggered.
 */
public class Ameto {
    private final Retrofit retrofit;
    private final AmetoApi ameto;

    /**
     * Initializes an Ameto client for the specified URL using the specified API key.
     * @param url URL to the Ameto API endpoint
     * @param apiKey API access key from your Ameto account
     */
    public Ameto(String url, String apiKey) {
        String version;
        try {
            version = getVersionFromManifest()
                    .orElse("dev");
        } catch (IOException e) {
            throw new AmetoException("Unable to determine library version.", e);
        }
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request alteredRequest = chain.request().newBuilder()
                            .addHeader("User-Agent", "Ameto/" + version + " (Java)")
                            .addHeader("Authorization", "Bearer " + apiKey)
                            .build();
                    return chain.proceed(alteredRequest);
                })
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(httpClient)
                .build();
        ameto = retrofit.create(AmetoApi.class);
    }

    private Optional<String> getVersionFromManifest() throws IOException {
        Enumeration<URL> manifests = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (manifests.hasMoreElements()) {
            URL manifestUrl = manifests.nextElement();
            Manifest manifest = new Manifest(manifestUrl.openStream());
            Attributes manifestAttributes = manifest.getMainAttributes();
            if (!manifestAttributes.containsKey("package") || !manifestAttributes.containsKey("version")) {
                continue;
            }
            String packageName = manifestAttributes.getValue("package");
            if ("de.ameto.client".equals(packageName)) {
                return Optional.of(manifestAttributes.getValue("version"));
            }
        }
        return Optional.empty();
    }

    /**
     * Starts a pipeline build process.
     * Existing Pipelines with the specified name are overwritten when the builder finishes.
     * @param name Pipeline name
     * @return Pipeline builder
     */
    public Pipeline.Builder addPipeline(String name) {
        return new Pipeline.Builder(ameto, name);
    }

    /**
     * Returns a collection of all pipelines.
     * @return Set of pipelines
     * @throws AmetoException if communication with the API was not possible or the response returned an error.
     */
    public Set<Pipeline> getPipelines() {
        List<PipelineDto> pipelines;
        try {
            Response<List<PipelineDto>> response = ameto.getPipelines().execute();
            if (!response.isSuccessful()) {
                Converter<ResponseBody, AddPipelineError> errorConverter =
                        retrofit.responseBodyConverter(AddPipelineError.class, new Annotation[0]);
                AddPipelineError error = errorConverter.convert(response.errorBody());
                throw new AmetoException(error.getError());
            }
            pipelines = response.body();
        } catch (IOException e) {
            throw new AmetoException("Unable to send pipeline reuqest to the Ameto API server", e);
        }
        return Collections.unmodifiableSet(pipelines.stream()
                .map(pipelineDto -> new Pipeline(ameto, pipelineDto.getId(), pipelineDto.getName(),
                        pipelineDto.getSteps().stream().map(Ameto::fromStep).collect(Collectors.toList())))
                .collect(Collectors.toSet()));
    }

    private static Operator fromStep(PipelineDto.Step step) {
        return new Operator() {
            @Override
            public String getName() {
                return step.getOperator();
            }

            @Override
            public String getVersion() {
                return step.getVersion();
            }

            @Override
            public List<String> getConsumes() {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<String> getArguments() {
                return Collections.unmodifiableList(step.getArguments());
            }
        };
    }

    /**
     * Returns Assets that have been uploaded to Ameto.
     * Note that the returned collection may not include very recently uploaded items.
     * @return Assets available to Ameto
     * @throws AmetoException if the asset list could not be retrieved
     */
    public Set<Asset> getAssets() {
        Response<List<String>> assetIds;
        try {
            assetIds = ameto.getAssets().execute();
        } catch (IOException e) {
            throw new AmetoException("Unable to retrieve assets from Ameto.", e);
        }
        if (!assetIds.isSuccessful() || assetIds.body() == null) {
            throw new AmetoException("Unsuccessful response from Ameto. This is either a bug in Ameto or you " +
                    "are using this client with an incompatible version of Ameto (e.g. wrong API version).");
        }
        return Collections.unmodifiableSet(assetIds.body().stream()
                .map(id -> new Asset(id, ameto))
                .collect(Collectors.toSet()));
    }

    /**
     * Uploads the file under the specified path to Ameto.
     * @param file File to be uploaded
     * @return Asset instance
     * @throws AmetoException if the specified path cannot be read
     */
    public Asset add(Path file) {
        InputStream data = null;
        try {
            data = Files.newInputStream(file);
        } catch (IOException e) {
            throw new AmetoException("Unable to add file " + file.toString(), e);
        }
        return add(data, file.getFileName().toString());
    }

    /**
     * Uploads the specified asset content.
     * The name parameter specifies the asset name, e.g. a file name.
     * @param assetContent Binary content of the asset
     * @param name Name of the asset.
     * @return An asset with the specified content
     * @throws AmetoException if an error occurs during asset upload
     */
    public Asset add(InputStream assetContent, String name) {
        try {
            RequestBody essenceBody = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse("application/octet-stream");
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    try (Source source = Okio.source(assetContent)) {
                        sink.writeAll(source);
                    }
                }
            };
            MultipartBody.Part asset = MultipartBody.Part.createFormData("essence", name, essenceBody);
            Response<AddAssetResponse> response = ameto.add(asset).execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new AmetoException("Received error response from Ameto API: " + response.errorBody().string());
            }
            return new Asset(response.body().getId(), ameto);
        } catch (IOException e) {
            throw new AmetoException("Unable to upload asset data to ameto.", e);
        }
    }

    /**
     * Removes the specified asset from Ameto.
     * @param asset Asset to be deleted
     * @throws AmetoException if the specified asset could not be deleted.
     */
    public void remove(Asset asset) {
        Response<Void> response = null;
        try {
            response = ameto.remove(asset.getId()).execute();
        } catch (IOException e) {
            throw new AmetoException("Unable to delete asset data from Ameto.", e);
        }
        if (!response.isSuccessful()) {
            throw new AmetoException("Received error response from Ameto API.");
        }
    }

    /**
     * Removes the specified processed asset from Ameto.
     * @param asset Processed asset to be deleted
     * @throws AmetoException if the specified processed asset could not be deleted
     */
    public void remove(ProcessedAsset asset) {
        Response<Void> response = null;
        try {
            response = ameto.remove(asset.getId()).execute();
        } catch (IOException e) {
            throw new AmetoException("Unable to delete asset data from Ameto.", e);
        }
        if (!response.isSuccessful()) {
            throw new AmetoException("Received error response from Ameto API.");
        }
    }

    /**
     * Returns a list of all jobs.
     * @return job list
     * @throws AmetoException if the request could not be sent
     */
    public List<Job> getJobs() {
        Response<List<GetJobResponse>> response;
        try {
            response = ameto.getJobs().execute();
        } catch (IOException e) {
            throw new AmetoException("Unable to send job request to Ameto API.", e);
        }
        val jobs = response.body();
        return Collections.unmodifiableList(jobs.stream()
                .map(job -> new Job(
                        job.getId(), job.getAsset().getId(), job.getPipeline(), job.getStatus()))
                .collect(Collectors.toList()));
    }
}
