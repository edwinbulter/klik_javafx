package eb.javafx.klik.login;

import java.time.Instant;

public class TokenManager {
    private static String userName;
    private static String accessToken;
    private static String refreshToken;
    private static Instant accessTokenExpiry;

    public static void saveTokens(String userName, String accessToken, Instant accessTokenExpiry, String refreshToken) {
        TokenManager.userName = userName;
        TokenManager.accessToken = accessToken;
        TokenManager.accessTokenExpiry = accessTokenExpiry;
        TokenManager.refreshToken = refreshToken;
    }

    protected static String getAccessTokenNullIfExpired() {
        if (accessToken != null && Instant.now().isBefore(accessTokenExpiry)) {
            return accessToken;
        } else {
            return null;
        }
    }

    public static void updateAccessToken(String accessToken, Instant accessTokenExpiry) {
        TokenManager.accessToken = accessToken;
        TokenManager.accessTokenExpiry = accessTokenExpiry;
    }

    public static String getRefreshToken() {
        return refreshToken;
    }

    public static String getUserName() {
        return userName;
    }

    public static void clearTokens() {
        userName = null;
        accessToken = null;
        refreshToken = null;
        accessTokenExpiry = null;
    }

    public static boolean isSignedIn() {
        return accessToken != null;
    }

}