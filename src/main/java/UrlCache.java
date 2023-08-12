import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UrlCache {

  private final UrlShortner urlShortner;

  private final Time time;
  private static final long LINK_EXPIRATION_IN_MINUTES = 60;
  private Map<String, UrlCacheEntry> shortToLongUrlMap = new ConcurrentHashMap<>();
  private Queue<UrlCacheEntry> expirationQueue = new LinkedList<>();
  private static UrlCache instance = new UrlCache();
  private final Object lock = new Object();

  private UrlCache(){
      this(new UrlShortner(), new Time());
  }

  UrlCache(UrlShortner urlShortner, Time time) {
      this.urlShortner = urlShortner;
      this.time = time;
  }

  public static synchronized UrlCache getInstance(){
    if (instance == null) {
      instance = new UrlCache();
    }
    return instance;
  }

  /**
   * Generates short URL based on given long URL
   * @param longUrl long url to shorten
   * @return short URL
   */
  public String getShortUrl(String longUrl){
      String shortUrl = urlShortner.getShortUrl(longUrl);
      UrlCacheEntry cacheEntry = new UrlCacheEntry(longUrl, shortUrl, time.now());
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
        if(time.now().isAfter(urlCacheEntry.timestamp().plusMinutes(LINK_EXPIRATION_IN_MINUTES))){
          throw new IllegalArgumentException("Short URL is expired" + shortUrl);
        }
        return urlCacheEntry.longUrl();
      }
      else {
        throw new IllegalArgumentException(UrlShortner.SHORT_URL_IS_INVALID + shortUrl);
      }
  }

  private void cleanExpiredCache(){
    while (true) {
      UrlCacheEntry entryFromHead = expirationQueue.peek();
      if (entryFromHead != null && time.now().isAfter(entryFromHead.timestamp().plusMinutes(LINK_EXPIRATION_IN_MINUTES))) {
          expirationQueue.poll();
          shortToLongUrlMap.remove(entryFromHead.shortUrl());
      } else {
        break;
      }
    }
  }
}
