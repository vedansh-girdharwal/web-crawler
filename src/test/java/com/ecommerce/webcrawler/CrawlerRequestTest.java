package com.ecommerce.webcrawler;

import com.ecommerce.webcrawler.dto.CrawlerRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CrawlerRequestTest {

    @Test
    void testCrawlerRequest() {
        CrawlerRequest request = new CrawlerRequest();
        request.setUrls(Arrays.asList("http://example.com", "http://anotherexample.com"));

        assertNotNull(request.getUrls());
        assertEquals(2, request.getUrls().size());
        assertEquals("http://example.com", request.getUrls().get(0));
    }
}