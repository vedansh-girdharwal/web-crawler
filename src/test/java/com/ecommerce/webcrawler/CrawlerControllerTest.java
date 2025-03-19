package com.ecommerce.webcrawler;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.ecommerce.webcrawler.controllers.CrawlerController;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.ecommerce.webcrawler.service.SeleniumCrawlerService;
import com.ecommerce.webcrawler.dto.CrawlerRequest;
import com.ecommerce.webcrawler.dto.CrawlerResponse;

import java.util.*;

public class CrawlerControllerTest {

    @Mock
    private SeleniumCrawlerService seleniumCrawlerService;

    @InjectMocks
    private CrawlerController crawlerController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDiscoverProductUrls_Success() {
        // Arrange
        CrawlerRequest request = new CrawlerRequest();
        request.setUrls(List.of("example.com"));
        when(seleniumCrawlerService.crawlWebsites(request.getUrls())).thenReturn(Map.of("example.com", Set.of("http://example.com/product/1")));

        // Act
        ResponseEntity<CrawlerResponse> response = crawlerController.discoverProductUrls(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getProductUrls().size());
        assertEquals("http://example.com/product/1", response.getBody().getProductUrls().get("example.com").iterator().next());
    }

    @Test
    public void testDiscoverProductUrls_EmptyUrls() {
        // Arrange
        CrawlerRequest request = new CrawlerRequest();
        request.setUrls(new ArrayList<>());
        when(seleniumCrawlerService.crawlWebsites(request.getUrls())).thenReturn(new HashMap<>());

        // Act
        ResponseEntity<CrawlerResponse> response = crawlerController.discoverProductUrls(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getProductUrls().size());
    }

    @Test
    public void testDiscoverProductUrls_NullUrls() {
        // Arrange
        CrawlerRequest request = new CrawlerRequest();
        request.setUrls(null);
        when(seleniumCrawlerService.crawlWebsites(request.getUrls())).thenReturn(new HashMap<>());

        // Act
        ResponseEntity<CrawlerResponse> response = crawlerController.discoverProductUrls(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getProductUrls().size());
    }
}