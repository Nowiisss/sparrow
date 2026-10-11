package com.sparrowwallet.sparrow.wallet.donation;

public class DonationContact {

    private final String id;
    private final String name;
    private final String silentPaymentAddress;

    public DonationContact(String id, String name, String silentPaymentAddress) {
        this.id = id;
        this.name = name;
        this.silentPaymentAddress = silentPaymentAddress;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSilentPaymentAddress() {
        return silentPaymentAddress;
    }

    @Override
    public String toString() {
        return name;
    }
}