
package com.sparrowwallet.sparrow.wallet.donation;

import java.math.BigDecimal;

import com.sparrowwallet.drongo.BitcoinUnit;
import com.sparrowwallet.sparrow.AppServices;
import com.sparrowwallet.sparrow.UnitFormat;
import com.sparrowwallet.sparrow.control.CoinTextFormatter;
import com.sparrowwallet.sparrow.control.FiatLabel;
import com.sparrowwallet.sparrow.control.IntegerSpinner;
import com.sparrowwallet.sparrow.io.Config;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class AutoDonationController {

    @FXML
    private Label information;

    @FXML
    private TextField budgetAmount;

    @FXML
    private ComboBox<BitcoinUnit> budgetUnit;

    @FXML
    private FiatLabel fiatBudget;

    @FXML
    private IntegerSpinner transactionHorizon;

    public void initializeView() {
        information.setText(
                "Configure the donation budget, recipients and cycle duration."
        );

        initializeBudget();
        initializeHorizon();
    }

    private void initializeBudget() {
        budgetUnit.getSelectionModel().select(BitcoinUnit.SATOSHIS);
        budgetAmount.setPromptText("Enter budget");

        budgetAmount.setTextFormatter(
                new CoinTextFormatter(Config.get().getUnitFormat(), budgetUnit.getValue())
        );

        fiatBudget.visibleProperty().bind(fiatBudget.textProperty().isNotEmpty());
        fiatBudget.managedProperty().bind(fiatBudget.visibleProperty());

        budgetAmount.textProperty().addListener((obs, oldValue, newValue) -> {
            updateFiatBudget();
        });

        budgetUnit.valueProperty().addListener((obs, oldUnit, newUnit) -> {
            Long valueSats = getBudgetSats(oldUnit);

            budgetAmount.setTextFormatter(
                    new CoinTextFormatter(Config.get().getUnitFormat(), newUnit)
            );

            if (valueSats != null) {
                UnitFormat format = getUnitFormat();

                String formattedValue = newUnit == BitcoinUnit.BTC
                        ? format.getBtcFormat().format(
                                BigDecimal.valueOf(valueSats, 8))
                        : format.getSatsFormat().format(valueSats);

                budgetAmount.setText(formattedValue);
            } else {
                budgetAmount.clear();
            }

            updateFiatBudget();
        });
    }

    private UnitFormat getUnitFormat() {
        UnitFormat format = Config.get().getUnitFormat();
        return format == null ? UnitFormat.DOT : format;
    }

    private Long getBudgetSats(BitcoinUnit unit) {
        String text = budgetAmount.getText();

        if (text == null || text.isBlank() || unit == null) {
            return null;
        }

        UnitFormat format = getUnitFormat();

        String normalized = text
                .replace(format.getGroupingSeparator(), "")
                .replace(format.getDecimalSeparator(), ".");

        try {
            BigDecimal amount = new BigDecimal(normalized);

            BigDecimal sats = unit == BitcoinUnit.BTC
                    ? amount.movePointRight(8)
                    : amount;

            long value = sats.longValueExact();

            return value > 0 ? value : null;
        } catch (NumberFormatException | ArithmeticException e) {
            return null;
        }
    }

    private void updateFiatBudget() {
        Long valueSats = getBudgetSats(budgetUnit.getValue());

        if (valueSats == null) {
            fiatBudget.setText("");
            return;
        }

        var exchangeRate = AppServices.getFiatCurrencyExchangeRate();

        if (exchangeRate != null) {
            fiatBudget.set(exchangeRate, valueSats);
        } else {
            fiatBudget.setText("");
        }
    }

    private void initializeHorizon() {
        transactionHorizon.setValueFactory(
                new IntegerSpinner.ValueFactory(1, 1_000_000, 10)
        );

        transactionHorizon.getEditor().setTextFormatter(
                new TextFormatter<String>(change -> {
                    String text = change.getControlNewText();

                    if (text.isEmpty()) {
                        return change;
                    }

                    if (!text.matches("\\d{1,7}")) {
                        return null;
                    }

                    try {
                        int value = Integer.parseInt(text);

                        return value >= 1 && value <= 1_000_000
                                ? change
                                : null;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
        );
    }
}
