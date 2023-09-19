package com.kpn.datastreaming;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.datastreaming.model.Outage;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RSSFeedProcessorTest {

    @Test
    void processFeed_ShouldGenerateJsonFiles() throws Exception {
        RSSFeedProcessor rssFeedProcessor = new RSSFeedProcessor();
        ObjectMapper objectMapper = new ObjectMapper();

        Path outputDir = Files.createTempDirectory("output");

        try {
            rssFeedProcessor.processFeed(getInputFilePathFromResources("test.xml"), outputDir.toString());

            Path businessFile = outputDir.resolve("business_outages.json");
            Path customerFile = outputDir.resolve("customer_outages.json");

            List<Outage> businessList = objectMapper.readValue(Files.readString(businessFile), new TypeReference<>() {});
            List<Outage> customerList = objectMapper.readValue(Files.readString(customerFile), new TypeReference<>() {});

            assertNotNull(businessList);
            assertNotNull(customerList);
            assertFalse(businessList.isEmpty());
            assertFalse(customerList.isEmpty());
        } finally {
            cleanupFiles(outputDir);
        }
    }

    private void cleanupFiles(Path outputDir) throws IOException {
        Files.walk(outputDir)
            .map(Path::toFile)
            .forEach(File::delete);
    }
    
    private String getInputFilePathFromResources(String fileName) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        try {
            File tempFile = File.createTempFile(fileName, "-tmp");
            tempFile.deleteOnExit();
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary file.", e);
        }
    }
}