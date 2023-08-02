import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UrlShortenerTest {
  private UrlShortener urlShortener = UrlShortener.getInstance();

  @InjectMocks
  private UrlShortener urlShortenerForExpired;

  @Mock
  private ConcurrentHashMap<String, UrlCacheEntry> shortToLongUrlMapMock;


  @Test
  public void shouldCreateShortUrl() {
    String longUrl = "https://www.example.com/suuuuuuuuuuuuperlongurl";

    String shortUrl = urlShortener.shortenUrl(longUrl);
    String shortUrlChangedPart = shortUrl.substring("https://mydomain.com/".length());

    assertNotNull(shortUrl);
    assertTrue(shortUrl.startsWith("https://mydomain.com/"));
    assertTrue(shortUrlChangedPart.matches("[a-zA-Z0-9]+"));
    assertEquals(8,shortUrlChangedPart.length());
  }

  @Test
  public void shouldThrowExceptionWhenLongUrlNullOrEmpty() {
    String emptyLongUrl = "";

    assertThrows(UrlShortener.PROVIDED_URL_IS_INVALID,IllegalArgumentException.class, ()-> urlShortener.shortenUrl(emptyLongUrl));
    assertThrows(UrlShortener.PROVIDED_URL_IS_INVALID,IllegalArgumentException.class, ()-> urlShortener.shortenUrl(null));
  }

  @Test
  public void shouldRetrieveCorrectLongUrl(){
    String longUrl = "https://www.example.com/suuuuuuuuuuuuperlongurl";

    String shortUrl = urlShortener.shortenUrl(longUrl);
    String retrievedLongUrl = urlShortener.getLongUrl(shortUrl);

    assertEquals(longUrl, retrievedLongUrl);
  }
  @Ignore
  @Test
//TODO: fix test
  public void shouldThrowExceptionWhenShortUrlIsOutdated() {
    String longUrl = "https://www.example.com/suuuuuuuuuuuuperlongurl";
    LocalDateTime timestamp = LocalDateTime.now().minusMinutes(61);
    UrlCacheEntry urlCacheEntry = new UrlCacheEntry(longUrl,"",timestamp);
    String shortUrl = urlShortenerForExpired.shortenUrl(longUrl);
    when(shortToLongUrlMapMock.get(shortUrl)).thenReturn(urlCacheEntry);

    assertThrows(IllegalArgumentException.class, () -> urlShortenerForExpired.getLongUrl(shortUrl));
    }
}