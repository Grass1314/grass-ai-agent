package com.grass.grassaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @description: 网页抓取工具，抓取指定URL的网页内容并提取文本
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10
 */
public class WebScrapingTool {

    private static final int TIMEOUT_MS = 15000;
    private static final int MAX_CONTENT_LENGTH = 5000;

    @Tool(description = "Scrape and extract the main text content from a web page URL. Returns the page title and body text.")
    public String scrapeWebPage(@ToolParam(description = "The full URL of the web page to scrape, e.g. https://example.com") String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            // 移除无关元素
            doc.select("script, style, nav, footer, header, iframe, noscript, .ad, .advertisement").remove();

            String title = doc.title();
            String metaDesc = doc.select("meta[name=description]").attr("content");

            // 优先提取 <article> 或 <main>，否则回退到 <body>
            Element mainContent = doc.selectFirst("article");
            if (mainContent == null) {
                mainContent = doc.selectFirst("main");
            }
            if (mainContent == null) {
                mainContent = doc.body();
            }

            String bodyText = mainContent != null ? mainContent.text() : "";
            if (bodyText.length() > MAX_CONTENT_LENGTH) {
                bodyText = bodyText.substring(0, MAX_CONTENT_LENGTH) + "...(content truncated)";
            }

            // 提取页面链接（最多10个）
            Elements links = doc.select("a[href]");
            StringBuilder linkSection = new StringBuilder();
            int linkCount = 0;
            for (Element link : links) {
                String href = link.absUrl("href");
                String text = link.text().trim();
                if (!href.isEmpty() && !text.isEmpty() && href.startsWith("http")) {
                    linkSection.append("  - ").append(text).append(": ").append(href).append("\n");
                    if (++linkCount >= 10) break;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Title: ").append(title).append("\n");
            if (!metaDesc.isEmpty()) {
                sb.append("Description: ").append(metaDesc).append("\n");
            }
            sb.append("URL: ").append(url).append("\n\n");
            sb.append("Content:\n").append(bodyText).append("\n");
            if (!linkSection.isEmpty()) {
                sb.append("\nKey Links:\n").append(linkSection);
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }

    @Tool(description = "Extract specific content from a web page using a CSS selector, e.g. 'h1', '.content', '#main'")
    public String scrapeWithSelector(
            @ToolParam(description = "The full URL of the web page") String url,
            @ToolParam(description = "CSS selector to extract content, e.g. 'h1', '.article-content', '#main'") String cssSelector) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            Elements elements = doc.select(cssSelector);
            if (elements.isEmpty()) {
                return "No elements found matching selector: " + cssSelector;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Found ").append(elements.size()).append(" element(s) matching '").append(cssSelector).append("':\n\n");
            int count = 0;
            for (Element el : elements) {
                String text = el.text().trim();
                if (!text.isEmpty()) {
                    sb.append(++count).append(". ").append(text).append("\n");
                }
                if (count >= 20) {
                    sb.append("...(more results truncated)");
                    break;
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error scraping with selector: " + e.getMessage();
        }
    }
}
