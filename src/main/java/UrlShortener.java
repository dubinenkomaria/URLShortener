import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UrlShortener {
  private static final String DOMAIN = "https://mydomain.com/";
  private static final long LINK_EXPIRATION_IN_MINUTES = 60;
  public static final String PROVIDED_URL_IS_INVALID = "Provided URL is invalid";
  public static final String SHORT_URL_IS_INVALID = "Short URL is invalid: ";
  private Map<String, UrlCacheEntry> shortToLongUrlMap = new ConcurrentHashMap<>();
  private Queue<UrlCacheEntry> expirationQueue = new LinkedList<>();
  private static UrlShortener instance = new UrlShortener();
  private final Object lock = new Object();

  private UrlShortener(){}

  public static synchronized UrlShortener getInstance(){
    if (instance == null) {
      instance = new UrlShortener();
    }
    return instance;
  }

  /**
   * Generates short URL based on given long URL
   * @param longUrl long url to shorten
   * @return short URL
   */
  public String shortenUrl(String longUrl){
      validateUrl(longUrl);
      String shortUrl = DOMAIN.concat(UUID.randomUUID().toString()
          .replaceAll("[^0-9a-zA-Z]", "")
          .substring(0, 8));
      UrlCacheEntry cacheEntry = new UrlCacheEntry(longUrl, shortUrl, LocalDateTime.now());
      synchronized (lock) {
        shortToLongUrlMap.put(shortUrl, cacheEntry);
        expirationQueue.add(cacheEntry);
      }
    cleanExpiredCache(); //TODO: add executor service
      return shortUrl;
  }
  /**
   * Retrieves long URL from URL cache used to redirect based on short URL
   * @param shortUrl given short URL
   * @return long URL from cache
   */
  public String getLongUrl(String shortUrl){
      Optional<UrlCacheEntry> entryCorrespondedToShortUrl = Optional.ofNullable(shortToLongUrlMap.get(shortUrl));
      if(entryCorrespondedToShortUrl.isPresent()){
        UrlCacheEntry urlCacheEntry = entryCorrespondedToShortUrl.get();
        if(LocalDateTime.now().isAfter(urlCacheEntry.timestamp().plusMinutes(LINK_EXPIRATION_IN_MINUTES))){
          throw new IllegalArgumentException("Short URL is expired" + shortUrl);
        }
        return urlCacheEntry.longUrl();
      }
      else {
        throw new IllegalArgumentException(SHORT_URL_IS_INVALID + shortUrl);
      }
  }

  private void validateUrl(String longUrl) {
    if (longUrl == null || longUrl.trim().isEmpty()) {
      throw new IllegalArgumentException(PROVIDED_URL_IS_INVALID);
    }
  }

  private void cleanExpiredCache(){
    while (true) {
      UrlCacheEntry entryFromHead = expirationQueue.peek();
      if (entryFromHead != null && LocalDateTime.now().isAfter(entryFromHead.timestamp().plusMinutes(LINK_EXPIRATION_IN_MINUTES))) {
          expirationQueue.poll();
          shortToLongUrlMap.remove(entryFromHead.shortUrl());
      } else {
        break;
      }
    }
  }
}
