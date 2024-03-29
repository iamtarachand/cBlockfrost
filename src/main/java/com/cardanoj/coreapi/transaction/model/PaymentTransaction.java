package com.cardanoj.coreapi.transaction.model;

import com.cardanoj.coreapi.account.Account;
import com.cardanoj.coreapi.model.Utxo;
import lombok.*;

import java.math.BigInteger;
import java.util.List;

/**
 * For payment transaction both in ADA (Lovelace) or Native tokens
 *  @deprecated Use Composable Functions API or QuickTx API to build transaction
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated(since = "0.5.0")
public class PaymentTransaction extends TransactionRequest {
    private String unit;
    private BigInteger amount;

    @Builder(toBuilder = true)
    public PaymentTransaction(Account sender, String receiver, BigInteger fee, List<Account> additionalWitnessAccounts,
                              List<Utxo> utxosToInclude, String unit, BigInteger amount, String datumHash) {
        super(sender, receiver, fee, additionalWitnessAccounts, utxosToInclude, datumHash);
        this.unit = unit;
        this.amount = amount;
    }

    public BigInteger getFee() {
        if(fee != null)
            return fee;
        else
            return BigInteger.ZERO;
    }

    public BigInteger getAmount() {
        if(amount != null)
            return amount;
        else
            return BigInteger.ZERO;
    }
}
