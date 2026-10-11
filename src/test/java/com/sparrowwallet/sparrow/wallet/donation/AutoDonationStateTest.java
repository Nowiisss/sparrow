package com.sparrowwallet.sparrow.wallet.donation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class AutoDonationStateTest {

    private AutoDonationConfig config(int transactionsPerCycle) {
        return new AutoDonationConfig(
                10_000L,
                List.of("contact-1", "contact-2"),
                transactionsPerCycle
        );
    }

    @Test
    void startsWithFullBudgetAndNoTransactions() {
        AutoDonationConfig config = config(3);
        AutoDonationState state = new AutoDonationState();

        assertEquals(0, state.getDoneTransactionsInCycle());
        assertEquals(0L, state.getDonatedSatsInCycle());
        assertEquals(10_000L, state.getRemainingBudgetSats(config));
    }

    @Test
    void countsTransactionsWithoutDonationsAndResetsAfterTheHorizon() {
        AutoDonationConfig config = config(3);
        AutoDonationState state = new AutoDonationState();

        state.recordTransaction(2_000L, config);
        assertEquals(1, state.getDoneTransactionsInCycle());
        assertEquals(2_000L, state.getDonatedSatsInCycle());
        assertEquals(8_000L, state.getRemainingBudgetSats(config));

        state.recordTransaction(0L, config);
        assertEquals(2, state.getDoneTransactionsInCycle());
        assertEquals(2_000L, state.getDonatedSatsInCycle());
        assertEquals(8_000L, state.getRemainingBudgetSats(config));

        state.recordTransaction(1_500L, config);
        assertEquals(0, state.getDoneTransactionsInCycle());
        assertEquals(0L, state.getDonatedSatsInCycle());
        assertEquals(10_000L, state.getRemainingBudgetSats(config));

        state.recordTransaction(3_000L, config);
        assertEquals(1, state.getDoneTransactionsInCycle());
        assertEquals(3_000L, state.getDonatedSatsInCycle());
        assertEquals(7_000L, state.getRemainingBudgetSats(config));
    }

    @Test
    void rejectsNegativeDonationsAndBudgetOverrunsWithoutChangingState() {
        AutoDonationConfig config = config(3);
        AutoDonationState state = new AutoDonationState();

        state.recordTransaction(9_000L, config);

        assertThrows(IllegalArgumentException.class,
                () -> state.recordTransaction(-1L, config));
        assertThrows(IllegalArgumentException.class,
                () -> state.recordTransaction(2_000L, config));

        assertEquals(1, state.getDoneTransactionsInCycle());
        assertEquals(9_000L, state.getDonatedSatsInCycle());
        assertEquals(1_000L, state.getRemainingBudgetSats(config));
    }

    @Test
    void allowsSpendingExactlyTheRemainingBudget() {
        AutoDonationConfig config = config(3);
        AutoDonationState state = new AutoDonationState();

        state.recordTransaction(6_000L, config);
        state.recordTransaction(4_000L, config);

        assertEquals(2, state.getDoneTransactionsInCycle());
        assertEquals(10_000L, state.getDonatedSatsInCycle());
        assertEquals(0L, state.getRemainingBudgetSats(config));

        // No funds left for donations, but the final outgoing transaction still counts.
        state.recordTransaction(0L, config);
        assertEquals(0, state.getDoneTransactionsInCycle());
        assertEquals(0L, state.getDonatedSatsInCycle());
        assertEquals(10_000L, state.getRemainingBudgetSats(config));
    }

    @Test
    void resetsAfterEveryTransactionWhenHorizonIsOne() {
        AutoDonationConfig config = config(1);
        AutoDonationState state = new AutoDonationState();

        state.recordTransaction(500L, config);
        assertEquals(0, state.getDoneTransactionsInCycle());
        assertEquals(0L, state.getDonatedSatsInCycle());
        assertEquals(10_000L, state.getRemainingBudgetSats(config));

        state.recordTransaction(0L, config);
        assertEquals(0, state.getDoneTransactionsInCycle());
        assertEquals(0L, state.getDonatedSatsInCycle());
        assertEquals(10_000L, state.getRemainingBudgetSats(config));
    }

    @Test
    void rejectsInvalidConfigurations() {
        assertThrows(IllegalArgumentException.class,
                () -> new AutoDonationConfig(0L, List.of("contact-1"), 3));
        assertThrows(IllegalArgumentException.class,
                () -> new AutoDonationConfig(10_000L, List.of(), 3));
        assertThrows(IllegalArgumentException.class,
                () -> new AutoDonationConfig(10_000L, List.of("contact-1"), 0));
    }
}
