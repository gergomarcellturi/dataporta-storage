package com.dataportal.datastorage.controller.data;

import com.dataportal.datastorage.service.DataApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(path = "api")
public class DataApiController {

    @Autowired
    private DataApiService dataApiService;

    @CrossOrigin
    @GetMapping("/file/{metadataUid}")
    public ResponseEntity<byte[]> getFileContent(@PathVariable String metadataUid, @RequestParam(required = false) String apiKey) throws ExecutionException, InterruptedException {
        return dataApiService.getFileContents(metadataUid, apiKey);
    }

    @CrossOrigin
    @GetMapping("/segmented/{metadataUid}")
    public ResponseEntity<byte[]> getFileContent(@PathVariable String metadataUid, @RequestParam(required = false) String apiKey, @RequestParam(required = false, defaultValue = "0") Integer segment) throws ExecutionException, InterruptedException {
        return dataApiService.getSegmentedFileContents(metadataUid, segment, apiKey);
    }

}
