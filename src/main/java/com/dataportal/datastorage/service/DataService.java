package com.dataportal.datastorage.service;

import com.dataportal.datastorage.entity.DatasourceDetails;
import com.dataportal.datastorage.entity.Tag;
import com.dataportal.datastorage.exception.ApplicationException;
import com.dataportal.datastorage.model.DataAccess;
import com.dataportal.datastorage.model.DataDownloadAccess;
import com.dataportal.datastorage.model.common.Response;
import com.dataportal.datastorage.model.request.DatasourceUpdate;
import com.dataportal.datastorage.model.response.UploadResponse;
import com.dataportal.datastorage.repository.DatasourceDetailsRepository;
import com.dataportal.datastorage.repository.TagsRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.dataportal.datastorage.entity.Datasource;
import com.dataportal.datastorage.entity.Metadata;
import com.dataportal.datastorage.model.DatasourceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.dataportal.datastorage.repository.DatasourceRepository;
import com.dataportal.datastorage.repository.MetadataRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class DataService {
    private final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private DatasourceDetailsRepository datasourceDetailsRepository;

    @Autowired
    private DatasourceRepository datasourceRepository;

    public Response<Metadata> createMetadata(final Metadata metadata) throws ExecutionException, InterruptedException {
        final Instant now = Instant.now();
        metadata.setCreatedAt(now);
        metadata.setLastModified(now);
        metadata.setDateDeleted(null);
        metadata.setDatePublished(null);
        metadata.setStatus(DatasourceStatus.NEW);
        metadata.setDataAccess(DataAccess.PUBLIC);
        metadata.setDataDownloadAccess(DataDownloadAccess.PUBLIC);

        final Metadata savedMetadata = metadataRepository.save(metadata);
        LOGGER.info(String.format("Metadata created with ID: %s", savedMetadata.getUid()));
        final Datasource datasource = new Datasource();
        datasource.setData(null);
        datasource.setCreatedAt(now);
        datasource.setLastModified(now);
        datasource.setMetadata(savedMetadata);
        datasourceRepository.save(datasource);
        return new Response<>(savedMetadata);
    }

    public Map<String, Object> getPreviewData(Metadata metadata, DatasourceDetails datasourceDetails) {
        Map<String, Object> data = new HashMap<>();
        List<String> titleNgrams = generateNgrams(datasourceDetails.getTitle().toLowerCase(), 3);
        List<String> summaryNgrams = generateNgrams(datasourceDetails.getSummary().toLowerCase(), 3);
        List<Map<String, Object>> tagsWithNgrams = createTagsWithNgrams(datasourceDetails.getTags(), 3);


        List<String> allTagsNgrams = new ArrayList<>();
        for (Tag tag : datasourceDetails.getTags()) {
            String tagTitleLower = tag.getTitle().toLowerCase();
            List<String> tagTitleNgrams = generateNgrams(tagTitleLower, 3);
            allTagsNgrams.addAll(tagTitleNgrams);
        }

        data.put("type", metadata.getType().toString());
        data.put("userUid", metadata.getUserUid());
        data.put("title", datasourceDetails.getTitle());
        data.put("titleLower", datasourceDetails.getTitle().toLowerCase());
        data.put("titleNgrams", titleNgrams);
        data.put("summary", datasourceDetails.getSummary());
        data.put("summaryLower", datasourceDetails.getSummary().toLowerCase());
        data.put("summaryNgrams", summaryNgrams);
        data.put("uid", metadata.getUid());
        data.put("status", metadata.getStatus().toString());
        data.put("filename", metadata.getFilename());
        data.put("dataAccess", metadata.getDataAccess());
        data.put("downloadAccess", metadata.getDataDownloadAccess());
        data.put("size", metadata.getSize());
        data.put("tags", tagsWithNgrams);
        data.put("allTagsNgrams", allTagsNgrams);
        data.put("lastModified", Timestamp.ofTimeMicroseconds(datasourceDetails.getLastModified().toEpochMilli()));
        data.put("createdAt", Timestamp.ofTimeMicroseconds(datasourceDetails.getCreatedAt().toEpochMilli()));
        return data;
    }

    public static List<String> generateNgrams(String text, int n) {
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i < text.length() - n + 1; i++) {
            ngrams.add(text.substring(i, i + n));
        }
        return ngrams;
    }

    public List<Map<String, Object>> createTagsWithNgrams(List<Tag> tags, int n) {
        List<Map<String, Object>> tagsWithNgrams = new ArrayList<>();
        for (Tag tag : tags) {
            Map<String, Object> tagWithNgrams = new HashMap<>();
            String tagTitle = tag.getTitle();
            String tagTitleLower = tagTitle.toLowerCase();
            List<String> tagTitleNgrams = generateNgrams(tagTitleLower, n);
            tagWithNgrams.put("title", tagTitle);
            tagWithNgrams.put("titleNgrams", tagTitleNgrams);
            tagsWithNgrams.add(tagWithNgrams);
        }
        return tagsWithNgrams;
    }

    public Metadata finalizeDatasource(final DatasourceDetails datasourceDetails, final String metadataId) {
        Metadata metadata = this.getMetadataByUid(metadataId);
        datasourceDetails.setMetadata(metadata);
        datasourceDetails.setTags(saveTags(datasourceDetails.getTags()));
        datasourceDetails.setLastModified(Instant.now());
        datasourceDetails.setCreatedAt(Instant.now());
        this.datasourceDetailsRepository.save(datasourceDetails);
        metadata.setLastModified(Instant.now());
        metadata.setStatus(DatasourceStatus.READY);
        CompletableFuture.runAsync(() -> {
            Firestore firestore = FirestoreClient.getFirestore();
            CollectionReference collection = firestore.collection("metadata_preview");
            DocumentReference docRef =  collection.document(metadata.getUid());
            Map<String, Object> data = getPreviewData(metadata, datasourceDetails);
            docRef.set(data);
        });
        return metadataRepository.save(metadata);
    }

    public List<Tag> saveTags(List<Tag> tags) {
        List<Tag> savedTags = new ArrayList<>();
        for (Tag tag: tags) {
            if (tag.getUid() != null) {
                savedTags.add(tag);
            } else {
                Optional<Tag> checkTag = tagsRepository.findByTitle(tag.getTitle());
                if (checkTag.isPresent()) {
                    savedTags.add(checkTag.get());
                } else {
                    tag.setCreatedAt(Instant.now());
                    tag.setLastModified(Instant.now());
                    savedTags.add(tagsRepository.save(tag));
                }
            }
        }
    return savedTags;
    }

    public UploadResponse uploadFile(final String metadataId, final MultipartFile file) throws ExecutionException, InterruptedException {
        Metadata metadata = getMetadataByUid(metadataId);
        UploadResponse response = new UploadResponse();
        Firestore firestore = FirestoreClient.getFirestore();
        CollectionReference collection = firestore.collection("progress");
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("filename", file.getOriginalFilename());
        progressData.put("userUid", metadata.getUserUid());
        progressData.put("progress", 0);
        progressData.put("lastModified", Timestamp.now());
        progressData.put("status", "UPLOADING");
        DocumentReference docRef = collection.add(progressData).get();

        CompletableFuture.runAsync(() -> {
            try {
                this.uploadData(metadata, file, progressData, docRef);
            } catch (IOException e) {
                progressData.put("lastModified", Timestamp.now());
                progressData.put("progress", 0);
                progressData.put("status", "INTERRUPTED");
                docRef.update(progressData);
            }
        });

        LOGGER.info("Sending back monitor id");
        response.setMonitorId(docRef.getId());
        return response;
    }


    @Async
    public void uploadData(
            Metadata metadata,
            MultipartFile file,
            Map<String, Object> progressData,
            DocumentReference docRef
            ) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        long totalLines = reader.lines().count();
        reader.close();

        reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        AtomicReference<AtomicInteger> lineNumber = new AtomicReference<>(new AtomicInteger(0));


        ArrayList<Byte> storeBytes = new ArrayList<>();
        ScheduledExecutorService progressUpdateExecutor
                = Executors.newSingleThreadScheduledExecutor();
        AtomicReference<Future<?>> progressUpdateFuture = new AtomicReference<>();
        Runnable progressUpdateTask = () -> {
            int progress = (int) (((double) lineNumber.get().get() / totalLines) * 100);
            progressData.put("lastModified", Timestamp.now());
            progressData.put("progress", progress);
            progressData.put("status", "UPLOADING");
            docRef.update(progressData);

        };
        progressUpdateFuture.set(progressUpdateExecutor.scheduleAtFixedRate(progressUpdateTask, 0, 500, TimeUnit.MILLISECONDS));

        reader.lines().forEach(line -> {
            byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
            lineNumber.updateAndGet(l -> new AtomicInteger(l.get() + 1));
            for (byte lineByte : lineBytes) {
                storeBytes.add(lineByte);
            }
        });

        reader.close();
        progressUpdateFuture.get().cancel(true);
        progressUpdateExecutor.shutdown();

        progressData.put("lastModified", Timestamp.now());
        progressData.put("progress", 100);
        progressData.put("status", "SAVING");
        docRef.update(progressData);
        byte[] saveBytes = new byte[storeBytes.size()];
        for (int i = 0; i < storeBytes.size(); i++) {
            saveBytes[i] = storeBytes.get(i);
        }



        Datasource datatasource = this.getDatasourceByMetadata(metadata);
        datatasource.setData(saveBytes);
        datatasource.setLastModified(Instant.now());
        datasourceRepository.save(datatasource);
        progressData.put("lastModified", Timestamp.now());
        progressData.put("progress", 100);
        progressData.put("status", "FINISHED");
        docRef.update(progressData);
    }

    @Async
    public CompletableFuture<Void> saveDatasourceFile(Datasource datasource, MultipartFile file) throws IOException {
        datasource.setData(file.getBytes());
        datasource.setLastModified(Instant.now());
        datasourceRepository.save(datasource);
        LOGGER.info(String.format("Upload init for datasource: %s", datasource.getUid()));
        return CompletableFuture.completedFuture(null);
    }

    public Metadata getMetadataByUid(final String metadataId) {
        return metadataRepository.findById(metadataId).orElseThrow(() -> new ApplicationException(
                String.format("Metadata not found with ID: %s", metadataId)
        ));
    }

    public DatasourceDetails getDatasourceDetailsByMetadataId(final String metadataId) {
        Metadata metadata = this.getMetadataByUid(metadataId);

        return this.datasourceDetailsRepository.findByMetadata(metadata).orElseThrow(() ->
                new ApplicationException(String.format("DatasourceDetails not found with metadataId: %s", metadataId)));
    }

    public ResponseEntity<Resource> downloadFile(final String metadataUid) {
        Metadata metadata = this.getMetadataByUid(metadataUid);
        Datasource datasource = this.getDatasourceByMetadata(metadata);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFilename() + "\"");
        ByteArrayResource resource = new ByteArrayResource(datasource.getData());
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    public Metadata deleteData(final String metadataUid) {
        Metadata metadata = this.getMetadataByUid(metadataUid);
        metadata.setStatus(DatasourceStatus.DELETED);
        Metadata savedMetadata = this.metadataRepository.save(metadata);
        this.updatePreview(savedMetadata);
        return savedMetadata;
    }

    @Async
    public void updatePreview(Metadata metadata) {
        DatasourceDetails datasourceDetails = getDatasourceDetailsByMetadataId(metadata.getUid());
        Firestore firestore = FirestoreClient.getFirestore();
        CollectionReference collection = firestore.collection("metadata_preview");
        DocumentReference docRef = collection.document(metadata.getUid());
        Map<String, Object> data = getPreviewData(metadata, datasourceDetails);
        if (metadata.getStatus() == DatasourceStatus.DELETED) {
            docRef.delete();
        } else {
            docRef.set(data);
        }
    }

    public DatasourceUpdate datasourceUpdate(final DatasourceUpdate datasourceUpdate) {
        Metadata savedMetadata = this.metadataRepository.save(datasourceUpdate.getMetadata());
        DatasourceDetails savedDetails = this.datasourceDetailsRepository.save(datasourceUpdate.getDatasourceDetails());
        updatePreview(savedMetadata);
        DatasourceUpdate updateResponse = new DatasourceUpdate();
        updateResponse.setMetadata(savedMetadata);
        updateResponse.setDatasourceDetails(savedDetails);
        return updateResponse;
    }

    public Datasource getDatasourceByMetadata(final Metadata metadata) {
        return this.datasourceRepository.findByMetadata(metadata).orElseThrow(() ->
                new ApplicationException(String.format("Datasource not found with metadataId: %s", metadata.getUid())));
    }

}
