package functional.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import esz.common.ResponseDto;
import esz.dev.App;
import esz.dev.account.control.AmountDto;
import esz.dev.account.control.TransferAmountDto;
import esz.dev.user.control.UserDto;
import functional.common.Utils;
import io.jooby.JoobyTest;
import io.jooby.StatusCode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static functional.common.Utils.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite of functional tests for the API which deals with bank accounts and banking transactions.
 */
class AccountApiTest {
    private static OkHttpClient client = new OkHttpClient();

    @BeforeEach
    void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(new UserDto("John", "Doe", "New York 101"));
        RequestBody requestBody = RequestBody.create(json, JSON);
        Response response = client.newCall(new Request.Builder()
                .url(BASE_URL_USER)
                .post(requestBody)
                .build()).execute();
    }

    @JoobyTest(value = App.class, port = 8888)
    @Test
    @DisplayName("Should create a new bank account for an user")
    void createNewAccount() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RequestBody requestBody = RequestBody.create("{}", JSON);
        Response response = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/1")
                .post(requestBody)
                .build()).execute();

        assertThat(response.code()).isEqualTo(StatusCode.CREATED_CODE);
        assertThat(getResponseObject(response, objectMapper, ResponseDto.class).getStatus()).isEqualByComparingTo(ResponseDto.Status.SUCCESS);
    }

    @JoobyTest(value = App.class, port = 8888)
    @Test
    @DisplayName("Should return 404 in case of a request for a new account for a nonexistent user")
    void createNewAccountForNonexistentUser() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RequestBody requestBody = RequestBody.create("{}", JSON);
        Response response = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/1213123")
                .post(requestBody)
                .build()).execute();

        assertThat(response.code()).isEqualTo(StatusCode.NOT_FOUND_CODE);
        assertThat(getResponseObject(response, objectMapper, ResponseDto.class).getStatus()).isEqualByComparingTo(ResponseDto.Status.FAILURE);
    }

    @JoobyTest(value = App.class, port = 8888)
    @Test
    @DisplayName("Should be able to deposit/withdraw funds.")
    void depositAndWithDrawFunds() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // create the account
        RequestBody requestBody = RequestBody.create("{}", JSON);
        client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/1")
                .post(requestBody)
                .build()).execute();

        // get the IBAN for the account
        String[] accounts = getAccountsForUser(1, client, objectMapper);

        // deposit a some amount
        AmountDto amountDto = new AmountDto(accounts[0], new BigDecimal("100"));
        String json = objectMapper.writeValueAsString(amountDto);
        Response depositResponse = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/deposit")
                .patch(RequestBody.create(json, JSON))
                .build()).execute();

        assertThat(depositResponse.code()).isEqualTo(StatusCode.ACCEPTED_CODE);
        assertThat(getResponseObject(depositResponse, objectMapper, ResponseDto.class)
                .getStatus()).isEqualByComparingTo(ResponseDto.Status.SUCCESS);

        //withdraw some amount
        AmountDto withDrawAmountDto = new AmountDto(accounts[0], new BigDecimal("22.34"));
        String jsonWithDraw = objectMapper.writeValueAsString(withDrawAmountDto);
        Response withDrawResponse = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/withdraw")
                .patch(RequestBody.create(jsonWithDraw, JSON))
                .build()).execute();

        assertThat(withDrawResponse.code()).isEqualTo(StatusCode.OK_CODE);
        assertThat(getResponseObject(withDrawResponse, objectMapper, ResponseDto.class)
                .getStatus()).isEqualByComparingTo(ResponseDto.Status.SUCCESS);

        // check the existing amount
        Response checkAmountResponse = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/" + accounts[0])
                .get()
                .build()).execute();
        assertThat(checkAmountResponse.code()).isEqualTo(StatusCode.OK_CODE);
        AmountDto actualAmountDto = Utils.getResponseObject(checkAmountResponse, objectMapper, AmountDto.class);
        assertThat(actualAmountDto.getAmount()).isEqualByComparingTo(new BigDecimal("77.66"));
    }

    @JoobyTest(value = App.class, port = 8888)
    @Test
    @DisplayName("Should not be able to withdraw more funds than available.")
    void withdrawMoreThanAvailable() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // create the account
        RequestBody requestBody = RequestBody.create("{}", JSON);
        client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/1")
                .post(requestBody)
                .build()).execute();

        // get the IBAN for the account
        String[] accounts = getAccountsForUser(1, client, objectMapper);

        // deposit a some amount
        AmountDto amountDto = new AmountDto(accounts[0], new BigDecimal("100"));
        String json = objectMapper.writeValueAsString(amountDto);
        Response depositResponse = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/deposit")
                .patch(RequestBody.create(json, JSON))
                .build()).execute();

        assertThat(depositResponse.code()).isEqualTo(StatusCode.ACCEPTED_CODE);
        assertThat(getResponseObject(depositResponse, objectMapper, ResponseDto.class)
                .getStatus()).isEqualByComparingTo(ResponseDto.Status.SUCCESS);

        //withdraw some amount
        AmountDto withDrawAmountDto = new AmountDto(accounts[0], new BigDecimal("100.01"));
        String jsonWithDraw = objectMapper.writeValueAsString(withDrawAmountDto);
        Response withDrawResponse = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/withdraw")
                .patch(RequestBody.create(jsonWithDraw, JSON))
                .build()).execute();

        assertThat(withDrawResponse.code()).isEqualTo(StatusCode.BAD_REQUEST_CODE);
        assertThat(getResponseObject(withDrawResponse, objectMapper, ResponseDto.class)
                .getStatus()).isEqualByComparingTo(ResponseDto.Status.FAILURE);

        // check the existing amount
        Response checkAmountResponse = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/" + accounts[0])
                .get()
                .build()).execute();
        assertThat(checkAmountResponse.code()).isEqualTo(StatusCode.OK_CODE);
        AmountDto actualAmountDto = Utils.getResponseObject(checkAmountResponse, objectMapper, AmountDto.class);
        assertThat(actualAmountDto.getAmount()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @JoobyTest(value = App.class, port = 8888)
    @Test
    @DisplayName("Should not be able to withdraw negative amount of money.")
    void withDrawNegativeAmount() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // create the account
        RequestBody requestBody = RequestBody.create("{}", JSON);
        client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/1")
                .post(requestBody)
                .build()).execute();

        // get the IBAN for the account
        String[] accounts = getAccountsForUser(1, client, objectMapper);

        //withdraw some amount
        AmountDto withDrawAmountDto = new AmountDto(accounts[0], new BigDecimal("-10"));
        String jsonWithDraw = objectMapper.writeValueAsString(withDrawAmountDto);
        Response withDrawResponse = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/withdraw")
                .patch(RequestBody.create(jsonWithDraw, JSON))
                .build()).execute();

        assertThat(withDrawResponse.code()).isEqualTo(StatusCode.BAD_REQUEST_CODE);
        assertThat(getResponseObject(withDrawResponse, objectMapper, ResponseDto.class)
                .getStatus()).isEqualByComparingTo(ResponseDto.Status.FAILURE);
    }

    @JoobyTest(value = App.class, port = 8888)
    @Test
    @DisplayName("Should be able to transfer amount.")
    void transferAmount() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // create the accounts
        RequestBody requestBody = RequestBody.create("{}", JSON);
        client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/1")
                .post(requestBody)
                .build()).execute();
        client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/1")
                .post(requestBody)
                .build()).execute();

        // get the IBAN for the account
        String[] accounts = getAccountsForUser(1, client, objectMapper);

        // deposit a some amount
        AmountDto depositAmount1 = new AmountDto(accounts[0], new BigDecimal("100"));
        Response depositResponse1 = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/deposit")
                .patch(RequestBody.create(objectMapper.writeValueAsString(depositAmount1), JSON))
                .build()).execute();
        AmountDto depositAmount2 = new AmountDto(accounts[1], new BigDecimal("100"));
        String json = objectMapper.writeValueAsString(depositAmount1);
        Response depositResponse2 = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/deposit")
                .patch(RequestBody.create(objectMapper.writeValueAsString(depositAmount2), JSON))
                .build()).execute();

        //transfer some amount between accounts
        TransferAmountDto transferAmountDto = new TransferAmountDto(accounts[0], accounts[1], new BigDecimal("22.34"));
        String jsonWithDraw = objectMapper.writeValueAsString(transferAmountDto);
        Response withDrawResponse = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/transfer")
                .patch(RequestBody.create(jsonWithDraw, JSON))
                .build()).execute();

        assertThat(withDrawResponse.code()).isEqualTo(StatusCode.OK_CODE);
        assertThat(getResponseObject(withDrawResponse, objectMapper, ResponseDto.class)
                .getStatus()).isEqualByComparingTo(ResponseDto.Status.SUCCESS);

        // check the existing amounts
        Response checkAmountResponse1 = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/" + accounts[0])
                .get()
                .build()).execute();
        Response checkAmountResponse2 = client.newCall(new Request.Builder()
                .url(BASE_URL_ACCOUNT + "/" + accounts[1])
                .get()
                .build()).execute();
        assertThat(checkAmountResponse1.code()).isEqualTo(StatusCode.OK_CODE);
        assertThat(checkAmountResponse2.code()).isEqualTo(StatusCode.OK_CODE);
        AmountDto actualAmountDto1 = Utils.getResponseObject(checkAmountResponse1, objectMapper, AmountDto.class);
        AmountDto actualAmountDto2 = Utils.getResponseObject(checkAmountResponse2, objectMapper, AmountDto.class);
        assertThat(actualAmountDto1.getAmount()).isEqualByComparingTo(new BigDecimal("77.66"));
        assertThat(actualAmountDto2.getAmount()).isEqualByComparingTo(new BigDecimal("122.34"));
    }
}
