package com.sparrowwallet.sparrow.wallet;

import java.util.List;

import com.sparrowwallet.drongo.BitcoinUnit;

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
    private void initialize() {
        contact.getItems().setAll(getDonationContacts());
        amountUnit.getSelectionModel().select(BitcoinUnit.SATOSHIS);
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
}
