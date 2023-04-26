package com.dataportal.datastorage.service;

import com.dataportal.datastorage.entity.Datasource;
import com.dataportal.datastorage.entity.Metadata;
import com.dataportal.datastorage.exception.ApplicationException;
import com.dataportal.datastorage.model.common.Response;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class DataApiService {

    private final String portalUrl = "http://localhost:8080";
    private static final Integer pageSize = 1024 * 1024; // 1MB

    @Autowired
    private DataService dataService;

    public ResponseEntity<byte[]> getFileContents(String metadataUid, String apiKey) throws ExecutionException, InterruptedException {
        Metadata metadata = dataService.getMetadataByUid(metadataUid);
        if (!canAccess(metadata, apiKey)) {
            throw new ApplicationException("Cannot Access Datasource with API Key");
        }
        Datasource datasource = dataService.getDatasourceByMetadata(metadata);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition", "inline; filename=\"" + metadata.getFilename() + "\"");
        switch (metadata.getType()) {
            case CSV -> headers.setContentType(MediaType.parseMediaType("text/csv"));
            case XML -> headers.setContentType(MediaType.APPLICATION_XML);
            case JSON -> headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return new ResponseEntity<>(datasource.getData(), headers, HttpStatus.OK);
    }

    public ResponseEntity<byte[]> getSegmentedFileContents(String metadataUid, Integer segment, String apiKey) throws ExecutionException, InterruptedException {
        Metadata metadata = dataService.getMetadataByUid(metadataUid);
        if (!canAccess(metadata, apiKey)) {
            throw new ApplicationException("Cannot Access Datasource with API Key");
        }
        Datasource datasource = dataService.getDatasourceByMetadata(metadata);
        byte[] fileChunk = getFileChunkByPage(datasource, segment, pageSize);
        HttpHeaders headers = new HttpHeaders();
        switch (metadata.getType()) {
            case CSV -> headers.setContentType(MediaType.parseMediaType("text/csv"));
            case XML -> headers.setContentType(MediaType.APPLICATION_XML);
            case JSON -> headers.setContentType(MediaType.APPLICATION_JSON);
        }
        headers.set("Content-Disposition", "inline; filename=\"" + metadata.getFilename() + "\"");
        headers.setContentLength(fileChunk.length);
        headers.set("X-Total-Pages", String.valueOf(getTotalPages(datasource, pageSize)));
        return new ResponseEntity<>(fileChunk, headers, HttpStatus.OK);
    }

    public byte[] getFileChunkByPage(Datasource datasource, int page, int pageSize) {
        byte[] data = datasource.getData();
        int offset = page * pageSize;
        int start = Math.min(offset, data.length);
        int end = Math.min(start + pageSize, data.length);
        return Arrays.copyOfRange(data, start, end);
    }

    public int getTotalPages(Datasource datasource, int pageSize) {
        byte[] data = datasource.getData();
        return (int) Math.ceil((double) data.length / pageSize);
    }

    public boolean canAccess(Metadata metadata, String apiKey) throws ExecutionException, InterruptedException {
        final String requestingUserUid = getUserUidFromApiKey(apiKey);
        if (apiKey != null && Objects.equals(requestingUserUid, metadata.getUserUid())) {
            return true;
        }
        switch (metadata.getDataAccess()) {
            case OPEN: return true;
            case PUBLIC:
                if (apiKey == null) return false;
                return getUserUidFromApiKey(apiKey) != null;
            case PRIVATE: return (Objects.equals(getUserUidFromApiKey(apiKey), metadata.getUserUid()));
            case REQUEST:
                this.updateAccessed(metadata, requestingUserUid);
                Firestore firestore = FirestoreClient.getFirestore();
                DocumentReference docRef =
                        firestore.collection("metadata_preview").document(metadata.getUid()).collection("access").document(requestingUserUid);
                DocumentSnapshot snap = docRef.get().get();
                return (snap.exists() && Boolean.TRUE.equals(snap.get("canAccess", Boolean.class)));
        }
        return false;
    }

    public void updateAccessed(Metadata metadata, String userUid) {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef =
                firestore.collection("metadata_preview").document(metadata.getUid()).collection("access").document(userUid);
        firestore.runTransaction(transaction -> {
            Timestamp timestamp = Timestamp.now();
            transaction.update(docRef, "accesses", FieldValue.arrayUnion(timestamp));
            return null;
        });
    }


    public String getUserUidFromApiKey(String apiKey) {
        RestTemplate restTemplate = new RestTemplate();
        String url = portalUrl + "/auth/apikey?apikey=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Response<String>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
        Response<String> responseBody = response.getBody();
        assert responseBody != null;
        return responseBody.getData();
    }

}
