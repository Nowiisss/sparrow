
package com.sparrowwallet.sparrow.wallet.donation;

import java.util.List;

public final class AutoDonationConfig {

    private final long budgetPerCycleSats;
    private final List<String> recipientIds;
    private final int outgoingTransactionsPerCycle;

    public AutoDonationConfig(
            long budgetPerCycleSats,
            List<String> recipientIds,
            int outgoingTransactionsPerCycle) {

        if (budgetPerCycleSats <= 0) {
            throw new IllegalArgumentException(
                    "Donation budget must be positive");
        }

        if (recipientIds == null || recipientIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one donation recipient is required");
        }

        if (outgoingTransactionsPerCycle <= 0) {
            throw new IllegalArgumentException(
                    "Transaction horizon must be positive");
        }

        this.budgetPerCycleSats = budgetPerCycleSats;
        this.recipientIds = List.copyOf(recipientIds);
        this.outgoingTransactionsPerCycle = outgoingTransactionsPerCycle;
    }

    public long getBudgetPerCycleSats() {
        return budgetPerCycleSats;
    }

    public List<String> getRecipientIds() {
        return recipientIds;
    }

    public int getOutgoingTransactionsPerCycle() {
        return outgoingTransactionsPerCycle;
    }
}
