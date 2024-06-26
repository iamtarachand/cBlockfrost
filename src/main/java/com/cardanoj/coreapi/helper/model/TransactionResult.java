package com.cardanoj.coreapi.helper.model;

import com.cardanoj.util.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResult {
    private byte[] signedTxn;
    private String transactionId;

    @Override
    public String toString() {
        if (signedTxn != null) {
            return "TransactionResult{" +
                    "signedTxn=" + HexUtil.encodeHexString(signedTxn) +
                    ", transactionId='" + transactionId + '\'' +
                    '}';
        } else {
            return super.toString();
        }
    }
}
