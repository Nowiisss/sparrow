
package com.sparrowwallet.sparrow.wallet.donation;

import java.util.Objects;

public final class AutoDonationState {

    private int doneTransactionsInCycle;
    private long donatedSatsInCycle;

    public AutoDonationState() {
        this.doneTransactionsInCycle = 0;
        this.donatedSatsInCycle = 0;
    }

    public int getDoneTransactionsInCycle() {
        return doneTransactionsInCycle;
    }

    public long getDonatedSatsInCycle() {
        return donatedSatsInCycle;
    }

    public long getRemainingBudgetSats(AutoDonationConfig config) {
        Objects.requireNonNull(config, "Donation config is required");

        return config.getBudgetPerCycleSats() - donatedSatsInCycle;
    }

    public void recordTransaction(long donationSats, AutoDonationConfig config) {
        Objects.requireNonNull(config, "Donation config is required");

        if (donationSats < 0 || donationSats > getRemainingBudgetSats(config)) {
            throw new IllegalArgumentException(
                    "Invalid donation amount for current cycle");
        }

        donatedSatsInCycle += donationSats;
        doneTransactionsInCycle++;

        if (doneTransactionsInCycle >= config.getOutgoingTransactionsPerCycle()) {
            doneTransactionsInCycle = 0;
            donatedSatsInCycle = 0;
        }
    }
}
