package com.cardanoj.coreapi.helper;

import com.cardanoj.coreapi.account.Account;
import com.cardanoj.coreapi.ProtocolParamsSupplier;
import com.cardanoj.coreapi.UtxoSupplier;
import com.cardanoj.coreapi.exception.ApiException;
import com.cardanoj.coreapi.helper.impl.UtxoTransactionBuilderImpl;
import com.cardanoj.coinselection.UtxoSelectionStrategy;
import com.cardanoj.crypto.SecretKey;
import com.cardanoj.exception.AddressExcepion;
import com.cardanoj.exception.CborSerializationException;
import com.cardanoj.metadata.Metadata;
import com.cardanoj.transaction.TransactionSigner;
import com.cardanoj.coreapi.transaction.model.MintTransaction;
import com.cardanoj.coreapi.transaction.model.PaymentTransaction;
import com.cardanoj.coreapi.transaction.model.TransactionDetailsParams;
import com.cardanoj.transaction.spec.Transaction;
import com.cardanoj.transaction.spec.TransactionWitnessSet;
import com.cardanoj.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class TransactionBuilder {

    private UtxoTransactionBuilder utxoTransactionBuilder;
    private ProtocolParamsSupplier protocolParamsSupplier;

    /**
     * Create a {@link TransactionHelperService} from {@link UtxoSupplier} and {@link ProtocolParamsSupplier}
     *
     * @param utxoSupplier
     * @param protocolParamsSupplier
     */
    public TransactionBuilder(UtxoSupplier utxoSupplier, ProtocolParamsSupplier protocolParamsSupplier) {
        this.utxoTransactionBuilder = new UtxoTransactionBuilderImpl(utxoSupplier);
        this.protocolParamsSupplier = protocolParamsSupplier;
    }

    /**
     * Create a {@link TransactionHelperService} from a {@link UtxoTransactionBuilder} and {@link ProtocolParamsSupplier}
     *
     * @param utxoTransactionBuilder
     * @param protocolParamsSupplier
     */
    public TransactionBuilder(UtxoTransactionBuilder utxoTransactionBuilder, ProtocolParamsSupplier protocolParamsSupplier) {
        this.utxoTransactionBuilder = utxoTransactionBuilder;
        this.protocolParamsSupplier = protocolParamsSupplier;
    }

    /**
     * Create a {@link TransactionHelperService} from {@link UtxoSelectionStrategy} and {@link ProtocolParamsSupplier}
     * This uses the default implementation of {@link UtxoTransactionBuilder} and set the custom {@link UtxoSelectionStrategy}
     *
     * @param utxoSelectionStrategy
     * @param protocolParamsSupplier
     */
    public TransactionBuilder(UtxoSelectionStrategy utxoSelectionStrategy, ProtocolParamsSupplier protocolParamsSupplier) {
        this.utxoTransactionBuilder = new UtxoTransactionBuilderImpl(utxoSelectionStrategy);
        this.protocolParamsSupplier = protocolParamsSupplier;
    }

    /**
     * Get UtxoTransactionBuilder set in this TransactionHelperService
     *
     * @return
     */
    public UtxoTransactionBuilder getUtxoTransactionBuilder() {
        return this.utxoTransactionBuilder;
    }

    /**
     * Set a custom UtxoTransactionBuilder
     *
     * @param utxoTransactionBuilder
     */
    public void setUtxoTransactionBuilder(UtxoTransactionBuilder utxoTransactionBuilder) {
        this.utxoTransactionBuilder = utxoTransactionBuilder;
    }

    public ProtocolParamsSupplier getProtocolParamsSupplier() {
        return protocolParamsSupplier;
    }

    public void setProtocolParamsSupplier(ProtocolParamsSupplier protocolParamsSupplier) {
        this.protocolParamsSupplier = protocolParamsSupplier;
    }

    /**
     * Get cbor serialized signed transaction in Hex
     *
     * @param paymentTransactions
     * @param detailsParams
     * @param metadata
     * @return
     * @throws ApiException
     * @throws AddressExcepion
     * @throws CborSerializationException
     */
    public String createSignedTransaction(List<PaymentTransaction> paymentTransactions, TransactionDetailsParams detailsParams, Metadata metadata)
            throws ApiException, AddressExcepion, CborSerializationException {
        if (log.isDebugEnabled())
            log.debug("Requests: \n" + JsonUtil.getPrettyJson(paymentTransactions));

        Transaction transaction = utxoTransactionBuilder.buildTransaction(paymentTransactions, detailsParams, metadata,
                protocolParamsSupplier.getProtocolParams());
        transaction.setValid(true);

        if (log.isDebugEnabled())
            log.debug(JsonUtil.getPrettyJson(transaction));

        Transaction finalTxn = transaction;

        Set<Account> signers = new HashSet<>();
        paymentTransactions
                .forEach(paymentTransaction -> {
                    signers.add(paymentTransaction.getSender());
                    if (paymentTransaction.getAdditionalWitnessAccounts() != null) {
                        signers.addAll(paymentTransaction.getAdditionalWitnessAccounts());
                    }
                });

        for (Account signer: signers) {
            finalTxn = signer.sign(finalTxn);
        }

        return finalTxn.serializeToHex();
    }

    /**
     * Create a mint transaction, sign and return cbor value as hex string
     *
     * @param mintTransaction
     * @param detailsParams
     * @param metadata
     * @return
     * @throws ApiException
     * @throws AddressExcepion
     * @throws CborSerializationException
     */
    public String createSignedMintTransaction(MintTransaction mintTransaction, TransactionDetailsParams detailsParams, Metadata metadata)
            throws ApiException, AddressExcepion, CborSerializationException {
        if (log.isDebugEnabled())
            log.debug("Requests: \n" + JsonUtil.getPrettyJson(mintTransaction));

        Transaction transaction = utxoTransactionBuilder.buildMintTokenTransaction(mintTransaction, detailsParams, metadata,
                protocolParamsSupplier.getProtocolParams());
        transaction.setValid(true);

        TransactionWitnessSet transactionWitnessSet = new TransactionWitnessSet();
        transactionWitnessSet.getNativeScripts().add(mintTransaction.getPolicy().getPolicyScript());
        transaction.setWitnessSet(transactionWitnessSet);

        //TODO - check probably not required here.
//        transaction.setAuxiliaryData(AuxiliaryData.builder()
//                .metadata(metadata)
//                .build());

        if (log.isDebugEnabled())
            log.debug(JsonUtil.getPrettyJson(transaction));

        Transaction signedTxn = mintTransaction.getSender().sign(transaction);

        if (mintTransaction.getPolicy().getPolicyKeys() != null) {
            for (SecretKey key : mintTransaction.getPolicy().getPolicyKeys()) {
                signedTxn = TransactionSigner.INSTANCE.sign(signedTxn, key);
            }
        }

        if (mintTransaction.getAdditionalWitnessAccounts() != null) {
            for (Account addWitnessAcc : mintTransaction.getAdditionalWitnessAccounts()) {
                signedTxn = addWitnessAcc.sign(signedTxn);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(signedTxn.toString());
            log.debug(signedTxn.serializeToHex());
        }
        return signedTxn.serializeToHex();
    }
}
