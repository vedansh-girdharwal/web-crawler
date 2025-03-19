# E-commerce Web Crawler

This project is a Spring Boot application that crawls e-commerce websites to discover product URLs.

## Prerequisites

- Java 17
- Maven 3.6.3 or higher

## Setup

1. **Clone the repository:**

    ```sh
    git clone <repository-url>
    cd <repository-directory>
    ```

2. **Build the project:**

    ```sh
    ./mvnw clean install
    ```

3. **Run the Spring Boot application:**

    ```sh
    ./mvnw spring-boot:run
    ```

## Usage

To use the `discoverProductUrls` endpoint, you can use the following `curl` command:

```sh
curl --location 'http://localhost:8080/api/discover' \
--header 'Content-Type: application/json' \
--data '{
    "urls": [
        "example1.com",
        "example2.in",
        "example3.com"
    ]
}'
```

This will send a POST request to the `/api/discover` endpoint with a JSON body containing a list of domain URLs to crawl.

# Web Crawler Service

This service is designed to discover product URLs from e-commerce websites using a combination of Jsoup and Selenium WebDriver.

## How It Works

### 1. Initial Setup
- The service uses a thread pool to handle multiple domains concurrently
- It maintains a list of regex patterns to identify product URLs

### 2. Crawling Process
1. **Domain Processing**
   - Each domain is prefixed with "https://www." for proper URL formation
   - Multiple domains are processed in parallel using ExecutorService

2. **Jsoup Crawling (Primary Method)**
   - Attempts to fetch and parse the website using Jsoup
   - Uses a custom user agent to mimic a real browser
   - Extracts all anchor tags with href attributes
   - Filters URLs using predefined product patterns
   - If no product URLs are found, falls back to Selenium

3. **Selenium Crawling (Fallback Method)**
   - Uses ChromeDriver in headless mode for JavaScript-heavy websites
   - Handles infinite scrolling and dynamically loaded content
   - Scrolls the page multiple times to load additional content
   - Extracts and filters URLs using the same product patterns
   - Maintains a set of unique URLs to avoid duplicates

4. **Result Processing**
   - Collected URLs are stored in a map with their respective domains
   - Results are saved to a local file (`crawl_results.txt`)
   - Returns a map of domains to their discovered product URLs

### 3. URL Matching
- The service uses a list of regex patterns to identify product URLs
- Patterns match common e-commerce URL structures like:
  - `/product/`
  - `/item/`
  - `/p/`
  - `/dp/`
  - `/detail/`
  - And many more

### 4. Error Handling
- Jsoup failures are gracefully handled by falling back to Selenium
- Selenium failures are logged and the process continues with other domains
- File writing errors are logged but don't interrupt the main process

## Configuration
- Thread pool size: 10
- Jsoup timeout: 10 seconds
- Selenium scroll attempts: 10
- Scroll wait time: 2 seconds
- Headless browser mode: enabled

## Usage
```java
@Autowired
private SeleniumCrawlerService crawlerService;

List<String> domains = Arrays.asList("example.com", "anotherexample.com");
Map<String, Set<String>> results = crawlerService.crawlWebsites(domains);
```

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.