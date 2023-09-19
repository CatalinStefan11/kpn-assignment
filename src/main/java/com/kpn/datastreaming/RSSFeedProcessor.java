package com.kpn.datastreaming;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kpn.datastreaming.model.Outage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class RSSFeedProcessor {

  private static final Logger logger = LoggerFactory.getLogger(RSSFeedProcessor.class);

  public void processFeed(String inputFilePath, String outputDirectory) {
    processOutages(createOutagesFlux(inputFilePath), outputDirectory);
  }

  private Flux<Outage> createOutagesFlux(String inputFilePath) {
    return Flux.using(
        () -> createXMLStreamReader(new FileInputStream(inputFilePath), StandardCharsets.UTF_8),
        XmlConverter::convert,
        this::closeReader
    ).doOnError((ex) -> {
      logger.error("An exception occurred.", ex);
    });
  }

  private void processOutages(Flux<Outage> outages, String outputDirectory) {
    outages
        .buffer(1000)
        .flatMapIterable(batch -> batch)
        .groupBy(Outage::getType)
        .flatMap(groupedFlux -> outputToJson(groupedFlux.key(), groupedFlux, outputDirectory))
        .doOnError(ex -> logger.error("An exception occurred during processing.", ex))
        .doOnComplete(() -> {
          logger.info("JSON files generated successfully.");
        })
        .subscribe();
  }

  private Mono<Void> outputToJson(Outage.Type type, GroupedFlux<Outage.Type, Outage> groupedFlux, String outputDirectory) {
    String fileName = type.getOutputName();

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    Flux<String> jsonFlux = createJsonValues(groupedFlux, objectMapper);
    return createFiles(jsonFlux, fileName, outputDirectory);
  }

  private Flux<String> createJsonValues(GroupedFlux<Outage.Type, Outage> groupedFlux, ObjectMapper objectMapper) {
    return groupedFlux.map(outage -> {
      try {
        return objectMapper.writeValueAsString(outage);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error serializing outage to JSON: " + e.getMessage());
      }
    });
  }
  
  private Mono<Void> createFiles(Flux<String> jsonFlux, String fileName, String outputDirectory) {
    Path filePath = Path.of(outputDirectory, fileName);
    DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    Flux<DataBuffer> dataBufferFlux = jsonFlux
        .collectList()
        .map(jsonList -> {
          String joinedJson = String.join(",\n", jsonList);
          String formattedJson = "[\n" + joinedJson + "\n]";
          return bufferFactory.wrap(formattedJson.getBytes(StandardCharsets.UTF_8));
        })
        .flatMapMany(Flux::just);

    return deleteFileIfExists(filePath)
        .then(Mono.defer(() -> DataBufferUtils.write(dataBufferFlux, filePath, StandardOpenOption.CREATE)));
  }

  private Mono<Void> deleteFileIfExists(Path filePath) {
    return Mono.fromCallable(() -> {
      try {
        Files.deleteIfExists(filePath);
        return null;
      } catch (IOException e) {
        throw new RuntimeException("Error deleting file: " + e.getMessage());
      }
    });
  }

  private XMLStreamReader createXMLStreamReader(InputStream reader, Charset encoding) {
    try {
      return XMLInputFactory.newInstance().createXMLStreamReader(reader, encoding.name());
    } catch (XMLStreamException e) {
      throw new RuntimeException("Failed to create XMLStreamReader", e);
    }
  }

  private void closeReader(XMLStreamReader reader) {
    try {
      reader.close();
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
  }
}
