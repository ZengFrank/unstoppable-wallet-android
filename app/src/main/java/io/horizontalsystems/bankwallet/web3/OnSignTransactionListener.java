package io.horizontalsystems.bankwallet.web3;

import io.horizontalsystems.bankwallet.web3.entity.Web3Transaction;

public interface OnSignTransactionListener {
    void onSignTransaction(Web3Transaction transaction, String url);
}
