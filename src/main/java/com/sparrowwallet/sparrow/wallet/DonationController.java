package com.sparrowwallet.sparrow.wallet;

import java.util.List;
import java.util.regex.Pattern;

import com.sparrowwallet.drongo.BitcoinUnit;
import com.sparrowwallet.drongo.protocol.Transaction;
import com.sparrowwallet.drongo.protocol.TransactionOutput;
import com.sparrowwallet.drongo.silentpayments.SilentPayment;
import com.sparrowwallet.drongo.silentpayments.SilentPaymentAddress;
import com.sparrowwallet.sparrow.AppServices;
import com.sparrowwallet.sparrow.UnitFormat;
import com.sparrowwallet.sparrow.control.FiatLabel;
import com.sparrowwallet.sparrow.io.Config;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class DonationController {

    @FXML
    private ComboBox<DonationContact> contact;

    @FXML
    private TextField amount;

    @FXML
    private ComboBox<BitcoinUnit> amountUnit;

    @FXML
    private FiatLabel fiatAmount;

    @FXML
    private void initialize() {
        contact.getItems().setAll(getDonationContacts());
        amountUnit.getSelectionModel().select(BitcoinUnit.SATOSHIS);

        amount.textProperty().addListener((obs, oldValue, newValue) -> {
            updateFiatAmount();
        });

        amountUnit.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateFiatAmount();
        });
    }

    private List<DonationContact> getDonationContacts() {
        return List.of(
                new DonationContact(
                        "contact-1",
                        "DonationReceiver1",
                        "tsp1qq2v2t8h2u5gksq8c4enzzh2hnfkpvd7wy4jav545mvxpmjvcatuskq5tr4tp8ud4fp55yx74uwjn2z6c6fszy3rscxm5wza3vghh58y3zgqpwlyq"
                ),
                new DonationContact(
                        "contact-2",
                        "DonationReceiver2",
                        "tsp1qqg6s5zk9s0ukt664kw3mscj577avu0x88rvg4ynsakj8ew9mmm026quyrldc8j3c8j7637gu30uwyqxak22wvshmk0yke54ymtred6n2zch3kdh6"
                )
        );
    }

    public DonationContact getSelectedContact() {
        return contact.getValue();
    }

    public String getAmount() {
        return amount.getText();
    }

    public BitcoinUnit getAmountUnit() {
        return amountUnit.getValue();
    }

    private Long getDonationValueSats() {
        BitcoinUnit bitcoinUnit = getAmountUnit();

        if (amount.getText() != null && !amount.getText().isEmpty() && bitcoinUnit != null) {
            UnitFormat unitFormat = Config.get().getUnitFormat() == null
                    ? UnitFormat.DOT
                    : Config.get().getUnitFormat();

            double fieldValue = Double.parseDouble(
                    amount.getText()
                            .replaceAll(Pattern.quote(unitFormat.getGroupingSeparator()), "")
                            .replaceAll(",", ".")
            );

            return bitcoinUnit.getSatsValue(fieldValue);
        }

        return null;
    }

    public SilentPayment getDonationPayment() {
        DonationContact selectedContact = getSelectedContact();
        Long value = getDonationValueSats();

        if (selectedContact == null
                || value == null
                || value < getDonationDustThreshold()) {
            throw new IllegalStateException("Invalid donation specified");
        }

        try {
            SilentPaymentAddress silentPaymentAddress
                    = SilentPaymentAddress.from(selectedContact.getSilentPaymentAddress());

            return new SilentPayment(
                    silentPaymentAddress,
                    selectedContact.getName(),
                    value,
                    false
            );
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            throw new IllegalStateException("Invalid donation specified", e);
        }
    }

    public boolean isValidDonation() {
        try {
            getDonationPayment();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    // Per veure que realment s'estigui creant l'objecte SilentPayment i tot estigui anant bé
    private void debugDonation() {
        try {
            SilentPayment payment = getDonationPayment();

            System.out.println("=== DONATION DEBUG ===");
            System.out.println("Contact: " + getSelectedContact().getName());
            System.out.println("SP address: " + payment.getSilentPaymentAddress().getAddress());
            System.out.println("Amount sats: " + payment.getAmount());
            System.out.println("Label: " + payment.getLabel());
            System.out.println("Valid: true");
            System.out.println("======================");
        } catch (IllegalStateException e) {
            System.out.println("DONATION DEBUG -> encara no és una donació vàlida");
        }
    }

    private long getDonationDustThreshold() {
        var address = SilentPayment.getDummyAddress();

        TransactionOutput txOutput
                = new TransactionOutput(
                        new Transaction(),
                        1L,
                        address.getOutputScript()
                );

        return address.getScriptType().getDustThreshold(
                txOutput,
                Transaction.DUST_RELAY_TX_FEE
        );
    }

    // Perque el listener de sendController funcioni per actualitzar diagrama en funció de les modificacions dels parametres de donació 
    // a partir de coses que son privades a donació
    public javafx.beans.property.StringProperty amountProperty() {
        return amount.textProperty();
    }

    public javafx.beans.property.ObjectProperty<DonationContact> selectedContactProperty() {
        return contact.valueProperty();
    }

    public javafx.beans.property.ObjectProperty<BitcoinUnit> amountUnitProperty() {
        return amountUnit.valueProperty();
    }

    private void updateFiatAmount() {
        try {
            Long value = getDonationValueSats();

            if (value != null) {
                fiatAmount.set(
                        AppServices.getFiatCurrencyExchangeRate(),
                        value
                );
            } else {
                fiatAmount.setText("");
            }
        } catch (Exception e) {
            fiatAmount.setText("");
        }
    }
}
