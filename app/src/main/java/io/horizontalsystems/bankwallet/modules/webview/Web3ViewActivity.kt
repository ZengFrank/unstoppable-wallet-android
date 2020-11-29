package io.horizontalsystems.bankwallet.modules.webview



import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import io.horizontalsystems.bankwallet.R
import kotlinx.android.synthetic.main.activity_web3_view.*
import org.jetbrains.anko.longToast

import trust.*
import trust.core.entity.Address
import trust.core.entity.Message
import trust.core.entity.Transaction
import trust.core.entity.TypedData
import trust.web3.OnSignMessageListener
import trust.web3.OnSignPersonalMessageListener
import trust.web3.OnSignTransactionListener
import trust.web3.OnSignTypedMessageListener


class Web3ViewActivity : AppCompatActivity(),
        OnSignTransactionListener, OnSignPersonalMessageListener, OnSignTypedMessageListener, OnSignMessageListener {

    companion object {
        const val guideWebUrlKey = "urlKey"
        const val guideTitleKey = "titleKey"
        const val guideAddressKey = "addressKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_web3_view)

        val guideUrl = intent.extras?.getString(guideWebUrlKey)
        val titleStr = intent.extras?.getString(guideTitleKey)

        titleStr?.let {
            textViewTitle.text = titleStr
        } ?: let {
            textViewTitle.visibility = View.GONE
            imageClose.visibility = View.GONE
        }
        val address = intent.extras?.getString(guideAddressKey) ?: ""

        imageClose.setOnClickListener {
            finish();
        }
        setupWeb3(address)
        web3view.loadUrl(guideUrl);
    }


    private var callSignMessage: Call<SignMessageRequest>? = null
    private var callSignPersonalMessage: Call<SignPersonalMessageRequest>? = null
    private var callSignTypedMessage: Call<SignTypedMessageRequest>? = null
    private var callSignTransaction: Call<SignTransactionRequest>? = null

    val TAG = "hyh_web3view"

    override fun onSignTransaction(transaction: Transaction?) {
        longToast("===onSignTransaction "+Gson().toJson(transaction))
        web3view.onSignCancel(transaction)
    }

    override fun onSignPersonalMessage(message: Message<String>) {
        longToast("====onSignPersonalMessage ${message.value}")
        web3view.onSignCancel(message)
    }

    override fun onSignTypedMessage(message: Message<Array<TypedData>>) {
        longToast("====onSignTypedMessage ${message.value}")
        web3view.onSignCancel(message)
    }

    override fun onSignMessage(message: Message<String>) {
        longToast("====onSignMessage ${message.value}")
        web3view.onSignCancel(message)
    }

    private fun setupWeb3(address: String) {
        WebView.setWebContentsDebuggingEnabled(true)
        web3view.setChainId(1)
        web3view.setRpcUrl("https://mainnet.infura.io/v3/bbfb2fa1009e40e4b72b10166d9a5069")
        //web3view.setRpcUrl("http://8.210.10.174:8545/v3/bbfb2fa1009e40e4b72b10166d9a5069")
        web3view.setWalletAddress(Address("0x242776e7ca6271e416e737adffcfeb22e8dc1b3c"))
        //web3view.setWalletAddress(Address(address))
        web3view.setOnSignMessageListener { message ->
            Log.d(TAG, "setOnSignMessageListener  " + message.value.toString())
            callSignMessage = Trust.signMessage().message(message).call(this)
        }
        web3view.setOnSignPersonalMessageListener { message ->
            Log.d(TAG, "setOnSignPersonalMessageListener  " + message.value.toString())
            callSignPersonalMessage = Trust.signPersonalMessage().message(message).call(this)
        }
        web3view.setOnSignTransactionListener { transaction ->
            Log.d(TAG, "setOnSignTransactionListener  " + transaction.value.toString())
            callSignTransaction = Trust.signTransaction().transaction(transaction).call(this)
        }
        web3view.setOnSignTypedMessageListener { message ->
            Log.d(TAG, "setOnSignTypedMessageListener  " + message.value.toString())
            callSignTypedMessage = Trust.signTypedMessage().message(message).call(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callSignTransaction?.let {
            it.onActivityResult(requestCode, resultCode, data) { response: Response<SignTransactionRequest> ->
                Log.d(TAG, "---000---")
                val transaction: Transaction = response.request!!.body<Transaction>()
                if (response.isSuccess) {
                    web3view.onSignTransactionSuccessful(transaction, response.result)
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        web3view.onSignCancel(transaction)
                    } else {
                        web3view.onSignError(transaction, "Some error")
                    }
                }
            }
        }
        callSignMessage?.let {
            it.onActivityResult(requestCode, resultCode, data) { response: Response<SignMessageRequest> ->
                Log.d(TAG, "---111---")
                val message: Message<String> = response.request!!.body<Message<String>>()
                if (response.isSuccess) {
                    web3view.onSignMessageSuccessful(message, response.result)
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        web3view.onSignCancel(message)
                    } else {
                        web3view.onSignError(message, "Some error")
                    }
                }
            }
        }
        callSignPersonalMessage?.let {
            it.onActivityResult(requestCode, resultCode, data) { response: Response<SignPersonalMessageRequest> ->
                Log.d(TAG, "---222---")
                val message: Message<String> = response.request!!.body<Message<String>>()
                if (response.isSuccess) {
                    web3view.onSignMessageSuccessful(message, response.result)
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        web3view.onSignCancel(message)
                    } else {
                        web3view.onSignError(message, "Some error")
                    }
                }
            }
        }
        callSignTypedMessage?.let {
            it.onActivityResult(requestCode, resultCode, data) { response: Response<SignTypedMessageRequest> ->
                Log.d(TAG, "---333---")
                val message: Message<Array<TypedData>> = response.request!!.body<Message<Array<TypedData>>>()
                if (response.isSuccess) {
                    web3view.onSignMessageSuccessful(message, response.result)
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        web3view.onSignCancel(message)
                    } else {
                        web3view.onSignError(message, "Some error")
                    }
                }
            }
        }
    }
}