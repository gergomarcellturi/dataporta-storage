package com.dataportal.datastorage.controller.data;

import com.dataportal.datastorage.entity.DatasourceDetails;
import com.dataportal.datastorage.entity.Metadata;
import com.dataportal.datastorage.model.common.Response;
import com.dataportal.datastorage.model.request.DatasourceUpdate;
import com.dataportal.datastorage.model.response.UploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dataportal.datastorage.service.DataService;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(path = "data")
public class DataController {

    @Autowired
    private DataService dataService;

    @CrossOrigin
    @PostMapping("/create")
    public Response<Metadata> createMetadata(@RequestBody final Metadata metadata) throws ExecutionException, InterruptedException {
        return this.dataService.createMetadata(metadata);
    }

    @CrossOrigin
    @RequestMapping(value = "/initupload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<UploadResponse> uploadFile(
            @RequestParam final String metadataId,
            @RequestParam("file") final MultipartFile file
    ) throws ExecutionException, InterruptedException {
                return new Response<>(this.dataService.uploadFile(metadataId, file));
    }

    @CrossOrigin
    @PostMapping("/finalize")
    public Response<Metadata> finalizeData(@RequestBody DatasourceDetails datasourceDetails, @RequestParam String metadataId) {
        return new Response<>(this.dataService.finalizeDatasource(datasourceDetails, metadataId));
    }

    @CrossOrigin
    @GetMapping("/{metadataId}")
    public Response<Metadata> getMetadataById(@PathVariable final String metadataId) {
        return new Response<>(this.dataService.getMetadataByUid(metadataId));
    }

    @CrossOrigin
    @GetMapping("/details/{metadataId}")
    public Response<DatasourceDetails> getDatasourceDetailsByMetadataId(@PathVariable final String metadataId) {
        return new Response<>(this.dataService.getDatasourceDetailsByMetadataId(metadataId));
    }

    @CrossOrigin
    @PutMapping("/update")
    public Response<DatasourceUpdate> updateDatasource(@RequestBody DatasourceUpdate datasourceUpdate) {
        return new Response<>(this.dataService.datasourceUpdate(datasourceUpdate));
    }

    @CrossOrigin
    @DeleteMapping("/{metadataUid}")
    public Response<Metadata> deleteData(@PathVariable String metadataUid) {
        return new Response<>(this.dataService.deleteData(metadataUid));
    }

    @CrossOrigin
    @GetMapping("/download/{metadataUid}")
    public ResponseEntity<Resource> downloadFile(@PathVariable final String metadataUid) {
        return this.dataService.downloadFile(metadataUid);
    }
}
