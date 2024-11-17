package eb.javafx.klik;

import eb.javafx.klik.api.UserCounterDeserializer;
import eb.javafx.klik.api.KlikClient;
import eb.javafx.klik.model.UserCounter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class KlikClientTest {

    /**
     * This test case verifies the getAllCounters method of the KlikClient class.
     * It checks if the method correctly parses the response with the CustomUserCounterDeserializer
     * and returns the list with UserCounter objects.
     */
    @Test
    public void getAllCountersTest() throws IOException, InterruptedException {
        HttpClient mockedClient = Mockito.mock(HttpClient.class);
        HttpResponse mockedResponse = Mockito.mock(HttpResponse.class);
        KlikClient klientUnderTest = new KlikClient(mockedClient);
        String json = "[{\""+
                UserCounterDeserializer.JSON_USER_ID+"\":\"user1\",\""+
                UserCounterDeserializer.JSON_CLICK_COUNT+"\":3,\""+
                UserCounterDeserializer.JSON_UPDATED_AT+"\":\"2024-09-28 10:36:43\"}]";

        when(mockedResponse.body()).thenReturn(json);
        when(mockedClient.send(Mockito.any(), Mockito.any())).thenReturn(mockedResponse);

        List<UserCounter> result = klientUnderTest.getAllCounters();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUserId());
        assertEquals(3, result.get(0).getClickCount());
        assertEquals("2024-09-28 10:36:43", result.get(0).getUpdatedAt());
    }

}