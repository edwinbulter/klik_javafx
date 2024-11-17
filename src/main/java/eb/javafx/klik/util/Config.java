package eb.javafx.klik.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {

    private static final Properties properties = new Properties();
    private static final Logger logger = LoggingUtil.getLogger();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.log(Level.SEVERE, "unable to find config.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "exception while loading config.properties", ex);
        }
    }

    public static String getCognitoUserPoolId() {
        return properties.getProperty("cognito.user_pool_id");
    }

    public static String getCognitoClientId() {
        return properties.getProperty("cognito.client_id");
    }

    public static String getJwksUrl() {
        return properties.getProperty("cognito.jwks_url");
    }

}
