package com.ecommerce.webcrawler.controllers;

import com.ecommerce.webcrawler.dto.CrawlerRequest;
import com.ecommerce.webcrawler.dto.CrawlerResponse;
import com.ecommerce.webcrawler.service.SeleniumCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling web crawling operations
 * Exposes REST endpoints under /api base path
 */
@RestController
@RequestMapping("/api")
public class CrawlerController {

    // Service for performing web crawling operations
    @Autowired
    private SeleniumCrawlerService seleniumCrawlerService;

    /**
     * Endpoint for discovering product URLs from provided website URLs
     * 
     * @param request Contains list of URLs to crawl
     * @return Response containing discovered product URLs
     */
    @PostMapping("/discover")
    public ResponseEntity<CrawlerResponse> discoverProductUrls(@RequestBody CrawlerRequest request) {
        CrawlerResponse response = new CrawlerResponse(seleniumCrawlerService.crawlWebsites(request.getUrls()));
        return ResponseEntity.ok(response);
    }
}