# RSS Feed Processor

- This Java application is designed to process legacy RSS feeds and generate JSON files. It utilizes the Project Reactor library for handling reactive streams and asynchronous processing. 
- The solution consists of several components that work together to parse XML data, convert it into Outage objects, and output the results as JSON files.
- The original business_outages.json and customer_outages.json were included in the project resources for reference as well as outages.xml file.

### Components
- Main class: Entry point for the application, handling command-line arguments.
- RSSFeedProcessor class: Coordinates the processing of RSS feeds, XML conversion, and JSON file generation.
- XmlConverter class: Converts XML data into Outage objects using an XMLStreamReader.
- Outage class: Represents an outage entity.

### Potential Performance Upgrades

- Parallel Processing: Parallelize the processing of individual items using parallel Flux operators, leveraging multiple threads for improved performance.
- Reactive Backpressure: Implement reactive backpressure strategies to manage the rate of data consumption for unbounded streams, preventing overwhelming the system.
- Batch Processing Optimization: Experiment with different batch sizes when buffering the outages to find an optimal batch size that balances memory usage and processing speed.

### Pitfalls and Improvements

- XML Parsing Robustness: Enhance XML parsing logic to handle variations in XML structure gracefully, with proper error handling and validation.
- Error Handling: Implement appropriate error handling and recovery mechanisms for exceptions during XML parsing or JSON serialization.
- Threading Model: Optimize the threading model based on specific requirements and constraints, considering the number of threads and specialized thread pools.
- Test Coverage: Expand the test suite to cover various scenarios, including edge cases, error conditions, and performance testing.
- Scalability: The current implementation processes an entire batch of 1000 elements in memory before generating JSON files. This approach may not scale well for very large RSS feeds or in scenarios with limited memory.

## How to Run the Project

### Building the Fat JAR

To build a fat JAR you can use the Gradle build tool and the shadowJar plugin. Follow these steps:

1. Make sure you have Java 17 installed on your system, and you have properly set the JAVA_HOME env var
3. Run the following command to build the fat JAR:

   ```shell
   ./gradlew shadowJar

### Command-line Options

The RSS Feed Processor accepts the following command-line options:

- `-i`, `--input`: Specifies the input file path. This option is required.
- `-o`, `--output`: Specifies the output directory path. This option is required.

### Example Usage

To process an RSS feed and generate JSON files, use the following command:

```shell
java -jar datastreaming-1.0-SNAPSHOT-all.jar --input <input_file_path> --output <output_directory_path>