package eb.javafx.klik.login;

import eb.javafx.klik.util.Config;
import eb.javafx.klik.util.LoggingUtil;
import javafx.scene.control.Alert;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CognitoAuthenticationUtil {

    private static final Logger logger = LoggingUtil.getLogger();

    public static String getAccessToken() {
        String accessToken = TokenManager.getAccessTokenNullIfExpired();
        if (accessToken == null) {
            refreshAccessToken();
            return TokenManager.getAccessTokenNullIfExpired();
        } else {
            return accessToken;
        }
    }

    public static void refreshAccessToken() {
        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().build()) {
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("REFRESH_TOKEN", TokenManager.getRefreshToken());

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(Config.getCognitoUserPoolId())
                    .clientId(Config.getCognitoClientId())
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParameters)
                    .build();

            AdminInitiateAuthResponse authResponse;
            authResponse = cognitoClient.adminInitiateAuth(authRequest);
            if (authResponse != null && authResponse.authenticationResult() != null) {
                String accessToken = authResponse.authenticationResult().accessToken();
                int expiresInSeconds = authResponse.authenticationResult().expiresIn();
                Instant expires = Instant.now().plusSeconds(expiresInSeconds - 1);
                TokenValidator.validateToken(accessToken);
                TokenManager.updateAccessToken(accessToken, expires);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Refresh Access Token");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public static boolean login(String userName, String password) {
        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().build()) {
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", userName);
            authParameters.put("PASSWORD", password);

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(Config.getCognitoUserPoolId())
                    .clientId(Config.getCognitoClientId())
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParameters)
                    .build();

            AdminInitiateAuthResponse authResponse;
            authResponse = cognitoClient.adminInitiateAuth(authRequest);
            if (authResponse != null && authResponse.authenticationResult() != null) {
                String accessToken = authResponse.authenticationResult().accessToken();
                int expiresInSeconds = authResponse.authenticationResult().expiresIn();
                Instant expires = Instant.now().plusSeconds(expiresInSeconds - 1);
                String refreshToken = authResponse.authenticationResult().refreshToken();
                TokenValidator.validateToken(accessToken);
                TokenManager.saveTokens(userName, accessToken, expires, refreshToken);
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean createAccount(String userName, String password, String email) {
        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().build()) {
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(Config.getCognitoClientId())
                    .username(userName)
                    .password(password)
                    .userAttributes(
                            AttributeType.builder()
                                    .name("email")
                                    .value(email)
                                    .build())
                    .build();
            SignUpResponse signUpResponse = cognitoClient.signUp(signUpRequest);
            return signUpResponse.sdkHttpResponse().isSuccessful();
        }
    }

    public static boolean confirmUser(String userName, String confirmationCode) {
        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().build()) {
            ConfirmSignUpRequest confirmSignUpRequest = ConfirmSignUpRequest.builder()
                    .clientId(Config.getCognitoClientId())
                    .username(userName)
                    .confirmationCode(confirmationCode)
                    .build();
            ConfirmSignUpResponse confirmSignUpResponse = cognitoClient.confirmSignUp(confirmSignUpRequest);
            return confirmSignUpResponse.sdkHttpResponse().isSuccessful();
        }
    }

    public static boolean deleteUnconfirmedUser(String username) {
        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().build()) {
            AdminDeleteUserRequest deleteUserRequest = AdminDeleteUserRequest.builder()
                    .userPoolId(Config.getCognitoUserPoolId())
                    .username(username)
                    .build();
            AdminDeleteUserResponse adminDeleteUserResponse = cognitoClient.adminDeleteUser(deleteUserRequest);
            return adminDeleteUserResponse.sdkHttpResponse().isSuccessful();
        }
    }

    public static boolean initiatePasswordReset(String username) {
        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().build()) {
            ForgotPasswordRequest forgotPasswordRequest = ForgotPasswordRequest.builder()
                    .clientId(Config.getCognitoClientId())
                    .username(username)
                    .build();

            ForgotPasswordResponse forgotPasswordResponse = cognitoClient.forgotPassword(forgotPasswordRequest);
            return forgotPasswordResponse.sdkHttpResponse().isSuccessful();
        }
    }

    public static boolean confirmNewPassword(String username, String confirmationCode, String newPassword) {
        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().build()) {
            ConfirmForgotPasswordRequest confirmForgotPasswordRequest = ConfirmForgotPasswordRequest.builder()
                    .clientId(Config.getCognitoClientId())
                    .username(username)
                    .confirmationCode(confirmationCode)
                    .password(newPassword)
                    .build();

            ConfirmForgotPasswordResponse confirmForgotPasswordResponse = cognitoClient.confirmForgotPassword(confirmForgotPasswordRequest);
            return confirmForgotPasswordResponse.sdkHttpResponse().isSuccessful();
        }
    }

}
