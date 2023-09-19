package com.kpn.datastreaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(name = "rss_processor", version = "1.0", description = "Processes RSS feed and generates JSON files.")
public class Main implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  
  @CommandLine.Option(names = {"-i", "--input"}, description = "Input file path", required = true)
  private String inputFilePath;

  @CommandLine.Option(names = {"-o", "--output"}, description = "Output directory path", required = true)
  private String outputDirectory;

  public static void main(String[] args) {
    CommandLine commandLine = new CommandLine(new Main());
    commandLine.execute(args);
  }

  @Override
  public void run() {
    RSSFeedProcessor processor = new RSSFeedProcessor();
    try {
      processor.processFeed(inputFilePath, outputDirectory);
    } catch (Exception e) {
      logger.error("Exception occurred.", e);
    }
  }
}