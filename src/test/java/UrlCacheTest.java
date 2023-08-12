import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UrlCacheTest {
  private UrlShortner urlShortner = mock(UrlShortner.class);

  private Time time = mock(Time.class);

  private final UrlCache urlCache = new UrlCache(urlShortner, time);

  private final String longUrl = "https://www.example.com/suuuuuuuuuuuuperlongurl";

  private final String shortUrl = "https://mydomain.com/shorturl";

  @Before
  public void before() {
    when(urlShortner.getShortUrl(longUrl)).thenReturn(shortUrl);
    when(time.now()).thenReturn(LocalDateTime.now());
  }

  @Test
  public void shouldCreateShortUrl() {
    String shortUrl = urlCache.getShortUrl(longUrl);

    assertNotNull(shortUrl);
    assertEquals(shortUrl, this.shortUrl);
  }


  /* TODO: move to UrlShortnerTest */
  @Ignore
  @Test
  public void shouldThrowExceptionWhenLongUrlNullOrEmpty() {
    String emptyLongUrl = "";

    assertThrows(UrlShortner.PROVIDED_URL_IS_INVALID,IllegalArgumentException.class, ()-> urlCache.getShortUrl(emptyLongUrl));
    assertThrows(UrlShortner.PROVIDED_URL_IS_INVALID,IllegalArgumentException.class, ()-> urlCache.getShortUrl(null));
  }

  @Test
  public void shouldRetrieveCorrectLongUrl(){
    String shortUrl = urlCache.getShortUrl(longUrl);
    String retrievedLongUrl = urlCache.getLongUrl(shortUrl);

    assertEquals(longUrl, retrievedLongUrl);
  }

  @Test
  public void shouldThrowExceptionWhenShortUrlIsOutdated() {
    String shortUrl = urlCache.getShortUrl(longUrl);
    LocalDateTime timestamp = LocalDateTime.now().plusMinutes(61);
    when(time.now()).thenReturn(timestamp);
    assertThrows(IllegalArgumentException.class, () -> urlCache.getLongUrl(shortUrl));
    }
}