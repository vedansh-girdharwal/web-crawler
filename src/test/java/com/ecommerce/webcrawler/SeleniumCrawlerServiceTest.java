package com.ecommerce.webcrawler;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.ecommerce.webcrawler.service.SeleniumCrawlerService;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

public class SeleniumCrawlerServiceTest {
    @Mock
    private Elements elementsMock;

    @Mock
    private Element elementMock;

    @InjectMocks
    private SeleniumCrawlerService seleniumCrawlerService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCrawlWebsites_Success() throws Exception {
        // Arrange
        List<String> domains = Arrays.asList("example.com", "test.com");
        // Act
        Map<String, Set<String>> result = seleniumCrawlerService.crawlWebsites(domains);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("example.com"));
        assertTrue(result.containsKey("test.com"));
    }

    @Test
    public void testCrawlWebsites_EmptyList() throws Exception {
        // Arrange
        List<String> domains = Collections.emptyList();

        // Act
        Map<String, Set<String>> result = seleniumCrawlerService.crawlWebsites(domains);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @BeforeEach
    public void setup() {
        // Create a mocked Element representing a product link
        when(elementMock.absUrl("href")).thenReturn("https://example.com/product/123");

        // Mock Elements collection to return a list containing elementMock
        when(elementsMock.iterator()).thenReturn(Set.of(elementMock).iterator());
    }

    @Test
    public void testIsProductUrl_ValidProductUrl() {
        assertTrue(seleniumCrawlerService.isProductUrl("https://example.com/product/123"));
        assertTrue(seleniumCrawlerService.isProductUrl("https://shop.com/item/456"));
        assertTrue(seleniumCrawlerService.isProductUrl("https://store.com/p/789"));
    }

    @Test
    public void testIsProductUrl_InvalidProductUrl() {
        assertFalse(seleniumCrawlerService.isProductUrl("https://example.com/category/electronics"));
        assertFalse(seleniumCrawlerService.isProductUrl("https://shop.com/about-us"));
        assertFalse(seleniumCrawlerService.isProductUrl("https://store.com/contact"));
    }

    @Test
    public void testIsProductUrl_NullUrl() {
        assertFalse(seleniumCrawlerService.isProductUrl(null));
    }

    @Test
    public void testIsProductUrl_EmptyUrl() {
        assertFalse(seleniumCrawlerService.isProductUrl(""));
    }
}