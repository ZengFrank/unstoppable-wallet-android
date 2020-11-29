package io.horizontalsystems.bankwallet.web3.entity;


import com.alphawallet.token.entity.Signable;

import io.horizontalsystems.bankwallet.entities.DAppFunction;

/**
 * Created by James on 6/04/2019.
 * Stormbird in Singapore
 */
public interface FunctionCallback
{
    void signMessage(Signable sign, DAppFunction dAppFunction);
    void functionSuccess();
    void functionFailed();
}
