package com.cardanoj.coreapi.transaction.model;

import com.cardanoj.spec.NetworkId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailsParams {
    private long ttl;
    private long validityStartInterval;
    private NetworkId networkId;
}
