package eb.javafx.klik.api;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eb.javafx.klik.login.CognitoAuthenticationUtil;
import eb.javafx.klik.model.UserCount;
import eb.javafx.klik.model.UserCounter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class KlikClient {
    public static final String BASE_URL = "https://ub0h6c0f8k.execute-api.eu-central-1.amazonaws.com/dev";
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public KlikClient() {
        this(HttpClient.newHttpClient());
    }

    public KlikClient(HttpClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("CustomUserCounterDeserializer", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(UserCounter.class, new UserCounterDeserializer());
        this.objectMapper.registerModule(module);
    }

    public List<UserCounter> getAllCounters() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/getAllCounters"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    public UserCount getCounter(String userName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/getCounter"))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(new CounterRequest(userName))))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + CognitoAuthenticationUtil.getAccessToken())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    public UserCount incrementCounter(String userName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(new CounterRequest(userName))))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + CognitoAuthenticationUtil.getAccessToken())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }


    public static void main(String[] args) throws IOException, InterruptedException {
//        System.out.println(CognitoLogin.getAccessToken());

        System.out.println("------- getCounter for edwin --------");
        UserCount userCount = new KlikClient().getCounter("edwin");
        System.out.println(userCount);

        System.out.println("------- incrementCounter for edwin --------");
        userCount = new KlikClient().incrementCounter("edwin");
        System.out.println(userCount);

        System.out.println("------- getAllCounters --------");
        List<UserCounter> result = new KlikClient().getAllCounters();
            for (UserCounter counter : result) {
                System.out.println(counter);
            }
    }

}
