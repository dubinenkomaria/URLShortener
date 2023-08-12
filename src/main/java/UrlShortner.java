import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
/* TODO: add javadoc*/
public class UrlShortner {
    private static final String DOMAIN = "https://mydomain.com/";

    public static final String PROVIDED_URL_IS_INVALID = "Provided URL is invalid";
    public static final String SHORT_URL_IS_INVALID = "Short URL is invalid: ";

    public String getShortUrl(String longUrl){
        validateUrl(longUrl);
        return DOMAIN.concat(UUID.randomUUID().toString()
                .replaceAll("[^0-9a-zA-Z]", "")
                .substring(0, 8));
    }

    /**
     * This method does not exist for this implementation. The caller is responsible for storing long URLs.
     * @param shortUrl
     * @return
     */
    public String getLongUrl(String shortUrl){
        return null;
    }


    private void validateUrl(String longUrl) {
        if (longUrl == null || longUrl.trim().isEmpty()) {
            throw new IllegalArgumentException(PROVIDED_URL_IS_INVALID);
        }
    }

}
