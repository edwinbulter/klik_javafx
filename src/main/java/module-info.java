module eb.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires software.amazon.awssdk.services.lambda;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.apigateway;
    requires java.net.http;
    requires software.amazon.awssdk.services.cognitoidentityprovider;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.http;
    requires software.amazon.awssdk.awscore;
    requires com.auth0.jwt;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires java.logging;

    exports eb.javafx.klik;
    opens eb.javafx.klik to javafx.fxml;
    exports eb.javafx.klik.api;
    opens eb.javafx.klik.api to javafx.fxml;

    exports eb.javafx.klik.login;
    opens eb.javafx.klik.login to javafx.fxml;
    exports eb.javafx.klik.model;
    opens eb.javafx.klik.model to javafx.fxml;
    exports eb.javafx.klik.util;
    opens eb.javafx.klik.util to javafx.fxml;
}