package functional.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import esz.dev.common.ResponseDto;
import esz.dev.App;
import esz.dev.user.control.UserDto;
import functional.common.Utils;
import io.jooby.JoobyTest;
import io.jooby.StatusCode;
import okhttp3.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static functional.common.Utils.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite of functional tests for the API which handles users.
 */
@JoobyTest(value = App.class, port = 8888)
class UserApiTest {
    private static OkHttpClient client = new OkHttpClient();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Test
    @DisplayName("Should create a new user")
    void createNewUser() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(new UserDto("John", "Doe", "New York 101"));
        RequestBody requestBody = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL_USER)
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();

        assertThat(response.code()).isEqualTo(StatusCode.CREATED_CODE);
        assertThat(getResponseObject(response, objectMapper, ResponseDto.class).getStatus()).isEqualByComparingTo(ResponseDto.Status.SUCCESS);
    }

    @Test
    @DisplayName("Should retrieve an existing user")
    void getExistingUser() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(new UserDto("John", "Doe", "New York 101"));
        RequestBody requestBody = RequestBody.create(json, JSON);
        Request postRequest = new Request.Builder()
                .url(BASE_URL_USER)
                .post(requestBody)
                .build();
        client.newCall(postRequest).execute();

        Request getRequest = new Request.Builder()
                .url(BASE_URL_USER + "/1")
                .get()
                .build();
        Response response = client.newCall(getRequest).execute();

        assertThat(response.code()).isEqualTo(StatusCode.OK_CODE);
        String responseJson = Objects.requireNonNull(response.body()).string();
        UserDto userDto = objectMapper.readValue(responseJson, UserDto.class);
        assertThat(userDto.getFirstName()).isEqualTo("John");
        assertThat(userDto.getLastName()).isEqualTo("Doe");
        assertThat(userDto.getAddress()).isEqualTo("New York 101");
    }

    @Test
    @DisplayName("Should get 404 in case of nonexistent user")
    void getNonExistingUser() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Request getRequest = new Request.Builder()
                .url(BASE_URL_USER + "/18979")
                .get()
                .build();
        Response response = client.newCall(getRequest).execute();

        assertThat(response.code()).isEqualTo(StatusCode.NOT_FOUND_CODE);
    }

    @Test
    @DisplayName("Should retrieve a list with IBAN numbers for a user")
    void getAccounts() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(new UserDto("John", "Doe", "New York 101"));
        RequestBody requestBody = RequestBody.create(json, JSON);
        Request postRequest = new Request.Builder()
                .url(BASE_URL_USER)
                .post(requestBody)
                .build();
        client.newCall(postRequest).execute();

        Request postRequestAccount = new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/1")
                .post(requestBody)
                .build();
        client.newCall(postRequestAccount).execute();

        Request getRequest = new Request.Builder()
                .url(BASE_URL_USER + "/1/accounts")
                .get()
                .build();
        Response response = client.newCall(getRequest).execute();

        assertThat(response.code()).isEqualTo(StatusCode.OK_CODE);
        String[] accounts = Utils.getResponseObject(response, objectMapper, String[].class);
        assertThat(accounts.length).isEqualTo(1);
        assertThat(accounts[0]).startsWith("RO");
        assertThat(accounts[0]).contains("XXXX");
    }
}
