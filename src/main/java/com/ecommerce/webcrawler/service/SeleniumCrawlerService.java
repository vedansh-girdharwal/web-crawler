package com.ecommerce.webcrawler.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

@Service
public class SeleniumCrawlerService {

    // Regex patterns to identify product URLs
    private static final List<String> PRODUCT_PATTERNS = Arrays.asList(
            ".*?/product/.*", ".*?/item/.*", ".*?/p/.*", ".*?/ip/.*", ".*?/t/.*", ".*?/pd/.*", ".*?/catalog/.*", 
            ".*?/goods/.*", ".*?/good/.*", ".*?/detail/.*", ".*?/dp/.*", ".*?/buy/.*", ".*?/prod-.*", 
            ".*?/item-.*", ".*?/itm/.*", ".*?/productdetails/.*", ".*?/p/.*", ".*?/proddetail/.*", ".?/listing/.*"
    );
    
    // Thread pool for concurrent crawling
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Crawls multiple websites concurrently to discover product URLs
     * 
     * @param domains List of domains to crawl
     * @return Map of domain to its discovered product URLs
     */
    public Map<String, Set<String>> crawlWebsites(List<String> domains) {
        Map<String, Set<String>> result = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();

        // Submit crawling tasks for each domain
        for (String domain : domains) {
            futures.add(executorService.submit(() -> 
                result.put(domain, discoverProductUrls("https://www." + domain))
            ));
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Save results to local file
        try (FileWriter writer = new FileWriter("crawl_results.txt")) {
            for (Map.Entry<String, Set<String>> entry : result.entrySet()) {
                writer.write("Domain: " + entry.getKey() + "\n");
                for (String url : entry.getValue()) {
                    writer.write("\t" + url + "\n");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Failed to write crawl results to file: " + e.getMessage());
        }

        return result;
    }

    /**
     * Discovers product URLs from a given domain using Jsoup, falls back to Selenium if Jsoup fails
     * 
     * @param domain The website domain to crawl for product URLs
     * @return Set of discovered product URLs
     */
    public Set<String> discoverProductUrls(String domain) {
        Set<String> productUrls = new HashSet<>();
        try {
            // Connect to the website using Jsoup with custom user agent and timeout
            Document doc = Jsoup.connect(domain)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10_000)
                    .get();
            
            // Select all anchor tags with href attributes
            Elements links = doc.select("a[href]");

            // Process each link to find product URLs
            for (Element link : links) {
                String url = link.absUrl("href");
                if (isProductUrl(url)) {
                    productUrls.add(url);
                }
            }
            
            // If no product URLs found, throw exception to trigger Selenium fallback
            if(productUrls.size() == 0){
                throw new IOException("No product URLs found via jsoup for " + domain);
            }
        } catch (IOException e) {
            // If Jsoup fails, log error and try with Selenium
            System.err.println("Jsoup failed for " + domain + ", trying Selenium...");
            productUrls.addAll(discoverWithSelenium(domain));
        }
        return productUrls;
    }

    /**
     * Discovers product URLs using Selenium WebDriver
     * 
     * @param domain The website domain to crawl
     * @return List of discovered product URLs
     */
    public List<String> discoverWithSelenium(String domain) {
        List<String> productUrls = new ArrayList<>();
        Set<String> uniqueUrls = new HashSet<>();

        // Configure ChromeDriver with headless options
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);

        try {
            // Navigate to the domain
            driver.get(domain);

            // Wait for page to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href], a, href")));

            // Handle infinite scrolling
            int scrollAttempts = 0;
            int maxScrollAttempts = 10;
            int previousSize = 0;
            int currentSize = 0;

            do {
                previousSize = uniqueUrls.size();
                
                // Scroll to bottom of page to load more content
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(2000); // Wait for new content to load

                // Find all links on the page
                List<WebElement> links = driver.findElements(By.cssSelector("a.s-item__link, a, a[href], href"));

                // Process each link to find product URLs
                for (WebElement link : links) {
                    String url = link.getAttribute("href");
                    if (url != null && isProductUrl(url) && uniqueUrls.add(url)) {
                        productUrls.add(url);
                    }
                }

                currentSize = uniqueUrls.size();
                scrollAttempts++;
                
                // Stop if no new URLs are found after scrolling
                if (currentSize == previousSize) {
                    break;
                }

            } while (scrollAttempts < maxScrollAttempts);

        } catch (Exception e) {
            System.err.println("Selenium failed for " + domain + ": " + e.getMessage());
        } finally {
            driver.quit();
        }
        return productUrls;
    }

    /**
     * Checks if a URL matches known product URL patterns
     * 
     * @param url The URL to check
     * @return true if the URL matches product patterns, false otherwise
     */
    public boolean isProductUrl(String url) {
        if(url == null || url.isEmpty()){
            return false;
        }
        return PRODUCT_PATTERNS.stream().anyMatch(pattern -> Pattern.matches(pattern, url));
    }
}