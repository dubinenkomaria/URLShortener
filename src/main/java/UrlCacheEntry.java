import java.time.LocalDateTime;
public record UrlCacheEntry(String longUrl, String shortUrl, LocalDateTime timestamp) {
}
