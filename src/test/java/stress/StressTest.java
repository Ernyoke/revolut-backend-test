package stress;

import esz.dev.account.control.*;
import esz.dev.account.entity.Account;
import esz.dev.user.control.UserNotFoundException;
import esz.dev.user.control.UserStore;
import esz.dev.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This suit contains test cases of which goal is to test the concurrent behaviour of the system.
 */
@ExtendWith(MockitoExtension.class)
class StressTest {
    private AccountStore accountStore;
    private UserStore userStore;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountStore = new AccountStore();
        userStore = new UserStore();
        accountService = new AccountService(accountStore, userStore);
    }

    /**
     * This test will attempt to create 1 million bank accounts concurrently for a single user. The system should be
     * able to not lose any request for a new account.
     */
    @Test
    @Timeout(1000)
    @DisplayName("Should create one million accounts concurrently for the same user")
    void concurrentAccountCreationTest() throws InterruptedException {
        long userId = 1L;
        User user = User.builder().id(userId).accounts(new HashSet<>()).build();
        userStore.addUser(user);

        final int NR_OF_ACCOUNTS = 1_000_000;

        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < NR_OF_ACCOUNTS; i++) {
            tasks.add(() -> {
                return accountService.createAccount(userId);
            });
        }
        executorService.invokeAll(tasks);
        executorService.shutdown();

        // check for the number of created accounts
        assertThat(accountStore.countAccounts()).isEqualTo(NR_OF_ACCOUNTS);

        // check that the user has all the accounts
        assertThat(user.getAccounts().size()).isEqualTo(NR_OF_ACCOUNTS);
    }

    /**
     * This tests concurrently invokes 1 million of random deposit/withdrawal transactions on a single account. The test
     * should fail in case of money loss or timeout caused by a deadlock.
     */
    @Test
    @Timeout(1000)
    @DisplayName("Should attempt to deposit/withdraw 1 million times")
    void concurrentDepositWithdrawTest() throws InterruptedException {
        int numberOfDeposits = 0;
        int numberOfWithdrawals = 0;

        String iban = "IBAN1";
        final int NR_OF_TRANSACTIONS = 1_000_000;
        Account account = Account.builder().amount(new BigDecimal(NR_OF_TRANSACTIONS)).iban(iban).build();
        accountStore.addAccount(account);

        // generate 1_000_000 random deposit/withdrawal tasks
        List<Callable<Void>> tasks = new ArrayList<>();
        Random randomGenerator = new Random();
        AmountDto amountDto = new AmountDto(iban, BigDecimal.ONE);
        for (int i = 0; i < NR_OF_TRANSACTIONS; i++) {
            if (randomGenerator.nextBoolean()) {
                numberOfDeposits++;
                tasks.add(() -> {
                    accountService.deposit(amountDto);
                    return null;
                });
            } else {
                numberOfWithdrawals++;
                tasks.add(() -> {
                    accountService.withdraw(amountDto);
                    return null;
                });
            }
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.invokeAll(tasks);
        executorService.shutdown();

        // check if we have all the money
        assertThat(account.getAmount())
                .isEqualByComparingTo(new BigDecimal(NR_OF_TRANSACTIONS + numberOfDeposits - numberOfWithdrawals));

    }

    /**
     * This integration test is trying to create deadlocks by invoking 10 million of simultaneous money transfer transactions.
     * In case of deadlock, the test will fail with time-out.
     */
    @Test
    @Timeout(1000)
    @DisplayName("Should try to create deadlocks by invoking a huge number of money transfers")
    void transferDeadlockTest() throws InterruptedException {
        List<Account> accounts = Arrays.asList(Account.builder().iban("IBAN1").amount(new BigDecimal("100000000")).build(),
                Account.builder().iban("IBAN2").amount(new BigDecimal("100000000")).build(),
                Account.builder().iban("IBAN3").amount(new BigDecimal("100000000")).build(),
                Account.builder().iban("IBAN4").amount(new BigDecimal("100000000")).build());

        for (Account account : accounts) {
            accountStore.addAccount(account);
        }

        final int NR_OF_TRANSACTIONS = 10_000_000;

        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Callable<Void>> tasks = new ArrayList<>();
        LongAdder counter = new LongAdder();
        for (int i = 0; i < NR_OF_TRANSACTIONS; i++) {
            int[] indexes = generateTwoDistinctRandomNumbers(accounts.size());
            tasks.add(() -> {
                Account account1 = accounts.get(indexes[0]);
                Account account2 = accounts.get(indexes[1]);
                TransferAmountDto transferAmountDto = new TransferAmountDto(account1.getIban(), account2.getIban(), BigDecimal.ONE);
                try {
                    accountService.transfer(transferAmountDto);
                } catch (AccountNotFoundException | NotEnoughAmountException e) {
                    e.printStackTrace();
                }
                counter.increment();
                return null;
            });
        }
        executorService.invokeAll(tasks);
        executorService.shutdown();

        // check that we have all the money
        BigDecimal sum = accounts.stream().map(Account::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        assertThat(sum).isEqualByComparingTo(new BigDecimal("400000000"));

        // check that we had done all the transactions
        assertThat(counter.longValue()).isEqualTo(NR_OF_TRANSACTIONS);
    }

    /**
     * Generate 2 random numbers between 0 and bound.
     */
    private int[] generateTwoDistinctRandomNumbers(int bound) {
        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(4);
        int index2;
        do {
            index2 = randomGenerator.nextInt(4);
        } while (index2 == index);
        return new int[]{index, index2};
    }
}
