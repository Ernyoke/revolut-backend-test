package functional.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * Helper methods used for functional testing.
 */
public interface Utils {
    static final String BASE_URL_USER = "http://localhost:8888/api/user";
    static final String BASE_URL_ACCOUNT = "http://localhost:8888/api/account";

    static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Retrieves an instance of T from the HTTP response.
     */
    static <T> T getResponseObject(Response response, ObjectMapper objectMapper, Class<T> clazz) throws IOException {
        String responseJson = Objects.requireNonNull(response.body()).string();
        return objectMapper.readValue(responseJson, clazz);
    }

    /**
     * Retrieves the list of IBAN account numbers for a user
     */
    static String[] getAccountsForUser(long userId, OkHttpClient client, ObjectMapper objectMapper) throws IOException {
        Request getRequest = new Request.Builder()
                .url(BASE_URL_USER + "/" + userId + "/accounts")
                .get()
                .build();
        Response response = client.newCall(getRequest).execute();
        return Utils.getResponseObject(response, objectMapper, String[].class);
    }
}
