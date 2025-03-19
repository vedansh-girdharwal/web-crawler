package com.ecommerce.webcrawler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class CrawlerResponse {
    private Map<String, Set<String>> productUrls;
}
