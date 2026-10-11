package com.sparrowwallet.sparrow.wallet.donation;

public enum DonationMode {
    AUTO,
    MANUAL;

    @Override
    public String toString() {
        return switch (this) {
            case AUTO ->
                "Auto";
            case MANUAL ->
                "Manual";
        };
    }
}
