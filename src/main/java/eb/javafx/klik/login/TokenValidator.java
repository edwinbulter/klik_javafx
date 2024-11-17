package eb.javafx.klik.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import eb.javafx.klik.util.Config;
import eb.javafx.klik.util.LoggingUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenValidator {
    private static final Logger logger = LoggingUtil.getLogger();

    public static void validateToken(String accessToken) {
        try {
            DecodedJWT decodedJWT = JWT.decode(accessToken);
            RSAKeyProvider keyProvider = getKeyProvider(decodedJWT);
            Algorithm algorithm = Algorithm.RSA256(keyProvider);

            JWTVerifier verifier = JWT.require(algorithm)
                    //added ignoreIssuedAt() to avoid the occasional exception: com.auth0.jwt.exceptions.InvalidClaimException: The Token can't be used before
                    .ignoreIssuedAt()
                    .withArrayClaim("cognito:groups", "clickers")
                    .withClaim("client_id", Config.getCognitoClientId())
                    .withClaim("username", TokenManager.getUserName())
                    .build();
            verifier.verify(accessToken);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            showErrorAlert("Token validation failed: " + e.getMessage());
        }
    }

    private static void showErrorAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private static RSAKeyProvider getKeyProvider(DecodedJWT jwt) throws Exception {
        String kid = jwt.getKeyId();
        Map<String, Object> jwks = getJWKS();
        List<Map<String,String>> maps = (List<Map<String,String>>)jwks.get("keys");
        Map<String, String> keyInfo = null;
        for (Map<String,String> map : maps) {
            String mapKid = map.get("kid");
            if (kid.equals(mapKid)) {
                keyInfo = map;
                break;
            }
        }
        if (keyInfo != null) {
            String modulus = keyInfo.get("n");
            String exponent = keyInfo.get("e");

            RSAPublicKey publicKey = RSAKeyFactory.createPublicKeyFrom(modulus, exponent);

            // Create the key provider
            return new RSAKeyProvider() {
                @Override
                public RSAPublicKey getPublicKeyById(String keyId) {
                    return publicKey;
                }

                @Override
                public RSAPrivateKey getPrivateKey() {
                    return null;
                }

                @Override
                public String getPrivateKeyId() {
                    return null;
                }
            };
        } else {
            return null;
        }
    }

    private static Map<String, Object> getJWKS() throws Exception {
        URL url = new URL(Config.getJwksUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        InputStream inputStream = conn.getInputStream();
        String result = new Scanner(inputStream).useDelimiter("\\A").next();
        conn.disconnect();
        return new ObjectMapper().readValue(result, Map.class);
    }

    static class RSAKeyFactory {
        public static RSAPublicKey createPublicKeyFrom(String modulusBase64, String exponentBase64) throws Exception {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(modulusBase64);
            byte[] exponentBytes = Base64.getUrlDecoder().decode(exponentBase64);
            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger publicExponent = new BigInteger(1, exponentBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) factory.generatePublic(spec);
        }
    }
}
