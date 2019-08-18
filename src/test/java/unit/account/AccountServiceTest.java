package unit.account;

import esz.dev.account.control.*;
import esz.dev.account.entity.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import esz.dev.user.control.UserNotFoundException;
import esz.dev.user.control.UserStore;
import esz.dev.user.entity.User;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for business logic which handles bank esz.dev.account transactions.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountStore accountStore;

    @Mock
    private UserStore userStore;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("Should create a new esz.dev.account for an esz.dev.user")
    void successfullyCreateAccount() throws UserNotFoundException {
        long userId = 1L;
        when(userStore.getUser(userId))
                .thenReturn(Optional.of(User.builder()
                                .id(userId)
                                .firstName("fname")
                                .lastName("lname")
                                .address("addr")
                                .accounts(new HashSet<>())
                                .build()));
        accountService.createAccount(userId);
        verify(accountStore, times(1)).addAccount(any(Account.class));
        verify(userStore, times(1)).getUser(userId);
    }

    @Test
    @DisplayName("Should throw exception in case nonexistent esz.dev.user")
    void throwExceptionWhenUserNotFoundForNewAccount() {
        long userId = 1L;
        when(userStore.getUser(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> accountService.createAccount(1L)).isInstanceOf(UserNotFoundException.class);
        verify(userStore, times(1)).getUser(userId);
    }

    @Test
    @DisplayName("Should successfully withdraw a given amount")
    void successfullyWithdrawAmount() throws AccountNotFoundException, NotEnoughAmountException {
        String iban = "IBAN";
        Account account = Account
                .builder().iban(iban)
                .amount(new BigDecimal("100.55"))
                .build();
        when(accountStore.getAccount(iban)).thenReturn(Optional.of(account));
        AmountDto amountDto = new AmountDto(iban, BigDecimal.TEN);
        accountService.withdraw(amountDto);

        assertThat(account.getAmount()).isEqualByComparingTo("90.55");
        verify(accountStore, times(1)).getAccount(iban);
        verifyNoMoreInteractions(accountStore);
    }

    @Test
    @DisplayName("Should throw an exception in case of insufficient funds for withdrawal")
    void throwExceptionWhenNotEnoughAmount() {
        String iban = "IBAN";
        Account account = Account
                .builder().iban(iban)
                .amount(new BigDecimal("9.99"))
                .build();
        when(accountStore.getAccount(iban)).thenReturn(Optional.of(account));
        AmountDto amountDto = new AmountDto(iban, BigDecimal.TEN);
        assertThatThrownBy(() -> accountService.withdraw(amountDto)).isInstanceOf(NotEnoughAmountException.class);

        verify(accountStore, times(1)).getAccount(iban);
        verifyNoMoreInteractions(accountStore);
    }

    @Test
    @DisplayName("Should throw an exception in case of nonexistent esz.dev.account used for withdrawal")
    void throwExceptionWhenAccountDoesNotExistWithdrawal() {
        String iban = "IBAN";
        when(accountStore.getAccount(iban)).thenReturn(Optional.empty());
        AmountDto amountDto = new AmountDto(iban, BigDecimal.TEN);
        assertThatThrownBy(() -> accountService.withdraw(amountDto)).isInstanceOf(AccountNotFoundException.class);

        verify(accountStore, times(1)).getAccount(iban);
        verifyNoMoreInteractions(accountStore);
    }

    @Test
    @DisplayName("Should successfully deposit a given amount")
    void successfullyDepositAmount() throws AccountNotFoundException, NotEnoughAmountException {
        String iban = "IBAN";
        Account account = Account
                .builder().iban(iban)
                .amount(new BigDecimal("100.55"))
                .build();
        when(accountStore.getAccount(iban)).thenReturn(Optional.of(account));
        AmountDto amountDto = new AmountDto(iban, new BigDecimal("203.999"));
        accountService.deposit(amountDto);

        assertThat(account.getAmount()).isEqualByComparingTo("304.549");
        verify(accountStore, times(1)).getAccount(iban);
        verifyNoMoreInteractions(accountStore);
    }

    @Test
    @DisplayName("Should throw an exception in case of nonexistent esz.dev.account for deposit")
    void throwExceptionWhenAccountDoesNotExistDeposit() {
        String iban = "IBAN";
        when(accountStore.getAccount(iban)).thenReturn(Optional.empty());
        AmountDto amountDto = new AmountDto(iban, BigDecimal.TEN);
        assertThatThrownBy(() -> accountService.deposit(amountDto)).isInstanceOf(AccountNotFoundException.class);

        verify(accountStore, times(1)).getAccount(iban);
        verifyNoMoreInteractions(accountStore);
    }

    @Test
    @DisplayName("Should successfully transfer funds between accounts")
    void successfullyTransferAmount() throws AccountNotFoundException, NotEnoughAmountException {
        String senderIban = "IBAN1";
        Account sender = Account
                .builder().iban(senderIban)
                .amount(new BigDecimal("100.55"))
                .build();
        String receiverIban = "IBAN2";
        Account receiver = Account
                .builder().iban(receiverIban)
                .amount(new BigDecimal("10"))
                .build();
        when(accountStore.getAccount(senderIban)).thenReturn(Optional.of(sender));
        when(accountStore.getAccount(receiverIban)).thenReturn(Optional.of(receiver));
        TransferAmountDto transferAmountDto = new TransferAmountDto(senderIban, receiverIban, BigDecimal.TEN);
        accountService.transfer(transferAmountDto);

        assertThat(sender.getAmount()).isEqualByComparingTo("90.55");
        assertThat(receiver.getAmount()).isEqualByComparingTo("20");
        verify(accountStore, times(1)).getAccount(senderIban);
        verify(accountStore, times(1)).getAccount(receiverIban);
        verifyNoMoreInteractions(accountStore);
    }

    @Test
    @DisplayName("Should throw an exception in case of insufficient funds for transfer")
    void throwExceptionWhenNotEnoughAmountForTransfer() {
        String senderIban = "IBAN1";
        Account sender = Account
                .builder().iban(senderIban)
                .amount(new BigDecimal("9.99"))
                .build();
        String receiverIban = "IBAN2";
        Account receiver = Account
                .builder().iban(receiverIban)
                .amount(new BigDecimal("10"))
                .build();
        when(accountStore.getAccount(senderIban)).thenReturn(Optional.of(sender));
        TransferAmountDto transferAmountDto = new TransferAmountDto(senderIban, receiverIban, BigDecimal.TEN);
        assertThatThrownBy(() -> accountService.transfer(transferAmountDto)).isInstanceOf(NotEnoughAmountException.class);

        verify(accountStore, times(1)).getAccount(senderIban);
        verifyNoMoreInteractions(accountStore);
    }

    @Test
    @DisplayName("Should throw an exception in case of nonexistent sender esz.dev.account used for transfer")
    void throwExceptionWhenSenderNotExist() {
        String senderIban = "IBAN1";
        String receiverIban = "IBAN2";
        when(accountStore.getAccount(senderIban)).thenReturn(Optional.empty());
        TransferAmountDto transferAmountDto = new TransferAmountDto(senderIban, receiverIban, BigDecimal.TEN);
        assertThatThrownBy(() -> accountService.transfer(transferAmountDto)).isInstanceOf(AccountNotFoundException.class);

        verify(accountStore, times(1)).getAccount(senderIban);
        verifyNoMoreInteractions(accountStore);
    }

    @Test
    @DisplayName("Should throw an exception in case of nonexistent sender esz.dev.account used for transfer")
    void throwExceptionWhenReceiverNotExist() {
        String senderIban = "IBAN1";
        Account sender = Account
                .builder().iban(senderIban)
                .amount(new BigDecimal("19.99"))
                .build();
        String receiverIban = "IBAN2";
        when(accountStore.getAccount(senderIban)).thenReturn(Optional.of(sender));
        when(accountStore.getAccount(receiverIban)).thenReturn(Optional.empty());
        TransferAmountDto transferAmountDto = new TransferAmountDto(senderIban, receiverIban, BigDecimal.TEN);
        assertThatThrownBy(() -> accountService.transfer(transferAmountDto)).isInstanceOf(AccountNotFoundException.class);

        verify(accountStore, times(1)).getAccount(senderIban);
        verifyNoMoreInteractions(accountStore);
    }
}
