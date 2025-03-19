package com.ecommerce.webcrawler.dto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CrawlerRequest {
    private List<String> urls;
}
