package com.kpn.datastreaming;

import com.kpn.datastreaming.model.Outage;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class XmlConverterTest {

  @Test
  public void testConvert() throws Exception {

    XMLInputFactory inputFactory = XMLInputFactory.newFactory();
    XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(getTestXml()));
    
    Flux<Outage> resultFlux = XmlConverter.convert(reader);

    StepVerifier.create(resultFlux)
        .expectNextSequence(Flux.fromIterable(List.of(getExpectedOutage())).toIterable())
        .verifyComplete();
  }

  @Test
  public void testHandleStartTag_Locations() throws Exception {
    XMLStreamReader reader = Mockito.mock(XMLStreamReader.class);
    Outage outage = new Outage();

    when(reader.getElementText()).thenReturn("ZMOH");

    XmlConverter.handleStartTag(reader, "locations", outage);

    assertEquals(Outage.Type.BUSINESS, outage.getType());
  }

  @Test
  public void testHandleStartTag_Description() throws Exception {
    XMLStreamReader reader = Mockito.mock(XMLStreamReader.class);
    Outage outage = new Outage();

    when(reader.getElementText()).thenReturn("Starttijd: 2023-06-10 12:00&nbsp; Eindtijd: 2020-06-10 16:00&nbsp;");

    XmlConverter.handleStartTag(reader, "description", outage);

    assertEquals("Starttijd: 2023-06-10 12:00&nbsp; Eindtijd: 2020-06-10 16:00&nbsp;", outage.getDescription());
    assertEquals("2023-06-10 12:00", outage.getStartDate());
    assertEquals("2020-06-10 16:00", outage.getEndDate());
    assertEquals("Opgelost", outage.getStatus());
  }

  @Test
  public void testHandleStartTag_Title() throws Exception {
    XMLStreamReader reader = Mockito.mock(XMLStreamReader.class);
    Outage outage = new Outage();

    when(reader.getElementText()).thenReturn("Outage 1");

    XmlConverter.handleStartTag(reader, "title", outage);

    assertEquals("Outage 1", outage.getTitle());
  }

  @Test
  public void testHandleStartTag_PostalCodes() throws Exception {
    XMLStreamReader reader = Mockito.mock(XMLStreamReader.class);
    Outage outage = new Outage();

    when(reader.getElementText()).thenReturn("1111AA;4000AB;4000AS;");

    XmlConverter.handleStartTag(reader, "postalCodes", outage);

    assertEquals("1111AA;4000AB;4000AS;", outage.getPostalCodes());
  }

  @Test
  public void testGetDate_ValidDate() {
    String description = "Starttijd: 2023-06-10 12:00&nbsp;Eindtijd: 2023-06-10 16:00";

    String result = XmlConverter.getDate("Starttijd:", description).orElseThrow(RuntimeException::new);

    assertEquals("2023-06-10 12:00", result);
  }

  @Test
  public void testGetDate_UnknownDate() throws Exception {
    String description = "Starttijd: Onbekend";

    Optional<String> result = XmlConverter.getDate("Starttijd:", description);

    assertEquals("Onbekend", result.orElse(null));
  }

  @Test
  public void testGetStatus_Actueel() {
    String startDateString = "2023-06-10 12:00";
    String endDateString = "onbekend";

    String result = XmlConverter.getStatus(startDateString, endDateString);

    assertEquals("Actueel", result);
  }

  @Test
  public void testGetStatus_Opgelost() {
    String startDateString = "2023-06-10 12:00";
    String endDateString = "2020-06-09 16:00";

    String result = XmlConverter.getStatus(startDateString, endDateString);

    assertEquals("Opgelost", result);
  }

  @Test
  public void testGetStatus_Gepland() {
    String startDateString = "2050-06-11 12:00";
    String endDateString = "2020-06-12 16:00";

    String result = XmlConverter.getStatus(startDateString, endDateString);

    assertEquals("Gepland", result);
  }

  private static String getTestXml() {
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0" xmlns:james="http://www.sqills.com/james/">
            <channel>
                <item>
                    <title><![CDATA[Dummy storing message]]></title>
                    <category>Storing</category>
                    <james:ticketNumber></james:ticketNumber>
                    <james:postalCodes>1705SC;</james:postalCodes>
                    <james:expectedEndDate></james:expectedEndDate>
                    <james:category>ITVAK</james:category>
                    <james:locations>KPN;</james:locations>
                    <description><![CDATA[hello storing message<br/>Starttijd: 2014-06-13 01:31&nbsp;Eindtijd: onbekend&nbsp;]]></description>
                    <link></link>
                </item>
            </channel>
        </rss>
        """;
  }

  private static Outage getExpectedOutage() {
    Outage o = new Outage();
    o.setTitle("Dummy storing message");
    o.setType(Outage.Type.CUSTOMER);
    o.setDescription("hello storing message<br/>Starttijd: 2014-06-13 01:31&nbsp;Eindtijd: onbekend&nbsp;");
    o.setStartDate("2014-06-13 01:31");
    o.setEndDate("onbekend");
    o.setStatus("Actueel");
    o.setPostalCodes("1705SC;");
    return o;
  }
}
  

