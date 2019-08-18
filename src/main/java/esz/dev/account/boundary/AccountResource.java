package esz.dev.account.boundary;

import esz.dev.account.control.*;
import esz.common.ResponseDto;
import io.jooby.Context;
import io.jooby.StatusCode;
import io.jooby.annotations.*;
import esz.dev.user.control.UserNotFoundException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * REST end-point implementation for banking transactions.
 */
@Singleton
@Path("/api/account")
public class AccountResource {
    private final AccountService accountService;
    private final ValidationService validationService;

    @Inject
    public AccountResource(AccountService accountService, ValidationService validationService) {
        this.accountService = accountService;
        this.validationService = validationService;
    }

    @POST("/{userId}")
    public ResponseDto createAccount(@PathParam Long userId, Context context) throws UserNotFoundException {
        accountService.createAccount(userId);
        context.setResponseCode(StatusCode.CREATED);
        return ResponseDto.builder().status(ResponseDto.Status.SUCCESS).message("Successfully created esz.dev.account!").build();
    }

    @GET("/{iban}")
    public AmountDto checkFunds(@PathParam String iban) throws InvalidInputException, AccountNotFoundException {
        validationService.validateIban(iban);
        return accountService.checkFunds(iban);
    }

    @PATCH("/withdraw")
    public ResponseDto withdraw(AmountDto amountDto, Context context)
            throws AccountNotFoundException, NotEnoughAmountException, InvalidInputException {
        validationService.validate(amountDto);
        accountService.withdraw(amountDto);
        context.setResponseCode(StatusCode.OK);
        return ResponseDto.builder().status(ResponseDto.Status.SUCCESS).message("Successfully withdrawn amount!").build();
    }

    @PATCH("/deposit")
    public ResponseDto deposit(AmountDto amountDto, Context context)
            throws AccountNotFoundException, NotEnoughAmountException, InvalidInputException {
        validationService.validate(amountDto);
        accountService.deposit(amountDto);
        context.setResponseCode(StatusCode.ACCEPTED);
        return ResponseDto.builder().status(ResponseDto.Status.SUCCESS).message("Successfully deposited amount!").build();
    }

    @PATCH("/transfer")
    public ResponseDto transfer(TransferAmountDto transferAmountDto, Context context)
            throws AccountNotFoundException, NotEnoughAmountException, InvalidInputException {
        validationService.validate(transferAmountDto);
        accountService.transfer(transferAmountDto);
        context.setResponseCode(StatusCode.OK);
        return ResponseDto.builder().status(ResponseDto.Status.SUCCESS).message("Successfully transferred amount!").build();
    }
}
