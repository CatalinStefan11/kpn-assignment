package com.kpn.datastreaming;

import com.google.common.annotations.VisibleForTesting;
import com.kpn.datastreaming.model.Outage;
import reactor.core.publisher.Flux;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

public class XmlConverter {

  private static final Set<String> IGNORE_TAGS = Set.of(
      "rss",
      "channel",
      "language",
      "copyright",
      "link",
      "generator"
  );

  public static Flux<Outage> convert(XMLStreamReader reader) {
    return Flux.create(emitter -> {

      Outage o = null;
      State currentState = State.INIT;

      try {
        while (reader.hasNext()) {

          int event = reader.next();

          switch (currentState) {
            case INIT -> {
              if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("item")) {
                o = new Outage();
                currentState = State.IN_ITEM;
              }
            }
            case IN_ITEM -> {
              if (event == XMLStreamConstants.START_ELEMENT && !IGNORE_TAGS.contains(reader.getLocalName())) {
                handleStartTag(reader, reader.getLocalName(), o);
              } else if (event == XMLStreamConstants.END_ELEMENT
                  && !IGNORE_TAGS.contains(reader.getLocalName())
                  && reader.getLocalName().equals("item")) {

                emitter.next(o);
                currentState = State.INIT;
                o = null;
              }
            }
          }
        }
        emitter.complete();
      } catch (Exception e) {
        emitter.error(e);
      }
    });
  }

  @VisibleForTesting
  static void handleStartTag(XMLStreamReader reader, String tagName, Outage o) throws XMLStreamException {
    switch (tagName) {
      case "locations" -> {
        String locations = reader.getElementText();
        o.setType(locations.contains("ZMOH") || locations.contains("ZMST") ? Outage.Type.BUSINESS : Outage.Type.CUSTOMER);
      }
      case "description" -> {
        String description = reader.getElementText().replaceAll("\\n", " ");
        explodeDescription(o, description);
      }
      case "title" -> o.setTitle(reader.getElementText());
      case "postalCodes" -> o.setPostalCodes(reader.getElementText());
    }
  }

  private static void explodeDescription(Outage o, String description) {
    o.setDescription(description);

    String startDate = getDate("Starttijd:", description).orElse("");
    String endDate = getDate("Eindtijd:", description).orElse("");

    o.setStartDate(startDate);
    o.setEndDate(endDate);
    o.setStatus(getStatus(startDate, endDate));
  }

  @VisibleForTesting
  static Optional<String> getDate(String name, String description) {
    int startIndex = description.indexOf(name);

    if (startIndex == -1) {
      return Optional.empty();
    }

    startIndex += name.length();

    if (startIndex >= description.length()) {
      return Optional.empty();
    }

    String time = description.substring(startIndex).trim();
    String[] split = time.split("&nbsp;");
    String date = split[0];

    if (date.contains("Onbekend")) {
      return Optional.of("Onbekend");
    }

    return Optional.of(date);
  }

  @VisibleForTesting
  static String getStatus(String startDateString, String endDateString) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    LocalDateTime startDate = LocalDateTime.parse(startDateString, formatter);
    LocalDateTime endDate = endDateString.equalsIgnoreCase("onbekend") ? LocalDateTime.MAX : LocalDateTime.parse(endDateString, formatter);
    LocalDateTime currentDateTime = LocalDateTime.now();

    if (endDate.isAfter(currentDateTime) || endDateString.equalsIgnoreCase("onbekend")) {
      return "Actueel";
    } else if (startDate.isAfter(currentDateTime)) {
      return "Gepland";
    } else {
      return "Opgelost";
    }
  }

  private enum State {
    INIT,
    IN_ITEM
  }
}
