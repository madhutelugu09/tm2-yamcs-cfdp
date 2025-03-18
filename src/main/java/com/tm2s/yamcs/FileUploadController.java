package com.tm2s.yamcs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFileAndCfdp(@RequestParam("file") MultipartFile file) throws IOException {
        logger.debug("Received file: " + file.getOriginalFilename());
        Path filePath = Paths.get("/home/madhu_telugu/tm2-moi-gs-yamcs/examples/cfdp/cfdpUp/", file.getOriginalFilename());
        logger.debug("Saving file to: " + filePath.toString());
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("File saved successfully.");
        } catch (IOException e) {
            logger.error("Error saving file", e);
            throw e;
        }

        String yamcsInstance = "cfdp0";
        String url = "http://localhost:8090/api/filetransfer/cfdp/" + yamcsInstance + "/transfers";
        logger.debug("Preparing payload for Yamcs CFDP API: " + url);
        Map<String, Object> payload = new HashMap<>();
        payload.put("direction", "UPLOAD");
        payload.put("bucket", "cfdpUp");
        payload.put("objectName", file.getOriginalFilename());
        payload.put("remotePath", file.getOriginalFilename());
        payload.put("source", "id11");
        payload.put("destination", "id5");
        Map<String, Object> options = new HashMap<>();
        options.put("reliable", true);
        payload.put("options", options);
        logger.debug("Payload: " + payload);
        RestTemplate restTemplate = new RestTemplate();
        logger.debug("Sending POST request to Yamcs CFDP API");
        ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
        logger.debug("Response: " + response.getStatusCode() + " " + response.getBody());
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
