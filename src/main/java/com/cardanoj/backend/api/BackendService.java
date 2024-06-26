package com.cardanoj.backend.api;

import com.cardanoj.coreapi.helper.FeeCalculationService;
import com.cardanoj.coreapi.helper.TransactionBuilder;
import com.cardanoj.coreapi.helper.TransactionHelperService;
import com.cardanoj.coreapi.helper.UtxoTransactionBuilder;
import com.cardanoj.coreapi.helper.impl.FeeCalculationServiceImpl;
import com.cardanoj.coreapi.helper.impl.UtxoTransactionBuilderImpl;

public interface BackendService {

    /**
     * Get AssetService
     *
     * @return {@link AssetService}
     */
    AssetService getAssetService();

    /**
     * Get BlockService
     *
     * @return {@link BlockService}
     */
    BlockService getBlockService();

    /**
     * Get NetworkInfoService
     *
     * @return {@link NetworkInfoService}
     */
    NetworkInfoService getNetworkInfoService();

    /**
     * Get Transaction service
     *
     * @return {@link TransactionService}
     */
    TransactionService getTransactionService();

    /**
     * Get UtxoService
     *
     * @return {@link UtxoService}
     */
    UtxoService getUtxoService();

    /**
     * Get AddressService
     *
     * @return {@link AddressService}
     */
    AddressService getAddressService();

    /**
     * Get AccountService
     *
     * @return {@link AccountService}
     */
    AccountService getAccountService();

    /**
     * Get EpochService
     *
     * @return {@link EpochService}
     */
    EpochService getEpochService();

    /**
     * Get MetadataService
     *
     * @return {@link MetadataService}
     */
    MetadataService getMetadataService();

    /**
     * Get ScriptService
     *
     * @return {@link ScriptService}
     */
    ScriptService getScriptService();

    /**
     * Get TransactionHelperService
     *
     * @return {@link TransactionHelperService}
     */
    default TransactionHelperService getTransactionHelperService() {
        TransactionHelperService transactionHelperService = new TransactionHelperService(
                new TransactionBuilder(new DefaultUtxoSupplier(getUtxoService()), new DefaultProtocolParamsSupplier(getEpochService())),
                new DefaultTransactionProcessor(getTransactionService()));
        return transactionHelperService;
    }

    /**
     * Get UtxoTransactionBuilder
     *
     * @return {@link UtxoTransactionBuilder}
     */
    default UtxoTransactionBuilder getUtxoTransactionBuilder() {
        UtxoTransactionBuilder utxoTransactionBuilder = new UtxoTransactionBuilderImpl(new DefaultUtxoSupplier(getUtxoService()));
        return utxoTransactionBuilder;
    }

    /**
     * Get FeeCalculationService
     *
     * @return {@link FeeCalculationService}
     */
    default FeeCalculationService getFeeCalculationService() {
        return new FeeCalculationServiceImpl(getTransactionHelperService().getTransactionBuilder());
    }

    /**
     * Get FeeCalculationService
     *
     * @param transactionHelperService TransactionHelperService instance
     * @return {@link FeeCalculationService}
     */
    default FeeCalculationService getFeeCalculationService(TransactionHelperService transactionHelperService) {
        return new FeeCalculationServiceImpl(transactionHelperService.getTransactionBuilder());
    }
}
