package stress;

import esz.dev.account.control.*;
import esz.dev.account.entity.Account;
import esz.dev.user.control.UserStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;

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
     * This integration test is trying to create deadlocks by invoking 10 million of simultaneous transactions. In case
     * of deadlock, the test will fail with time-out.
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

        // Check that we have all the money
        BigDecimal sum = accounts.stream().map(Account::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        assertThat(sum).isEqualByComparingTo(new BigDecimal("400000000"));

        // Check that we had done all the transactions
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
