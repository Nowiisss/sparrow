package com.sparrowwallet.sparrow.wallet.donation;

import java.io.IOException;

import com.sparrowwallet.sparrow.AppServices;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class AutoDonationDialog extends Dialog<Void> {

    public AutoDonationDialog() {
        DialogPane dialogPane = getDialogPane();

        setTitle("Auto Donations");

        try {
            FXMLLoader loader = new FXMLLoader(
                    AppServices.class.getResource("wallet/autoDonation.fxml")
            );

            dialogPane.setContent(loader.load());

            AutoDonationController controller = loader.getController();
            controller.initializeView();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load Auto Donations dialog", e);
        }

        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setPrefWidth(600);
        dialogPane.setPrefHeight(430);
    }
}
