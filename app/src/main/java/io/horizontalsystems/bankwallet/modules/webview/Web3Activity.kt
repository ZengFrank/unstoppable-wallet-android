package io.horizontalsystems.bankwallet.modules.webview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.alphawallet.token.entity.EthereumMessage
import com.alphawallet.token.entity.EthereumTypedMessage
import com.alphawallet.token.entity.Signable
import com.alphawallet.token.tools.Numeric
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.C
import io.horizontalsystems.bankwallet.entities.DAppFunction
import io.horizontalsystems.bankwallet.entities.SignTransactionInterface
import io.horizontalsystems.bankwallet.entities.URLLoadInterface
import io.horizontalsystems.bankwallet.web3.OnSignMessageListener
import io.horizontalsystems.bankwallet.web3.OnSignPersonalMessageListener
import io.horizontalsystems.bankwallet.web3.OnSignTransactionListener
import io.horizontalsystems.bankwallet.web3.OnSignTypedMessageListener
import io.horizontalsystems.bankwallet.web3.entity.Address
import io.horizontalsystems.bankwallet.web3.entity.Web3Transaction
import kotlinx.android.synthetic.main.activity_web3.*
import org.jetbrains.anko.alert
import org.web3j.crypto.Wallet
import java.util.*

class Web3Activity : AppCompatActivity() , OnSignTransactionListener, OnSignPersonalMessageListener, OnSignTypedMessageListener, OnSignMessageListener,
        URLLoadInterface, SignTransactionInterface {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_web3)

        val guideUrl = intent.extras?.getString(Web3ViewActivity.guideWebUrlKey)
        val titleStr = intent.extras?.getString(Web3ViewActivity.guideTitleKey)

        titleStr?.let {
            textViewTitle.text = titleStr
        } ?: let {
            textViewTitle.visibility = View.GONE
            imageClose.visibility = View.GONE
        }
      
        imageClose.setOnClickListener {
            finish();
        }
        setupWeb3()
        web3view.loadUrl(guideUrl)
    }

    private val chainId = 1
    private val walletAddress: Address? = null
    private val wallet: Wallet?= null
    private val address = "0x242776e7ca6271e416e737adffcfeb22e8dc1b3c"

    private var messageTBS: Signable? = null
    private var dAppFunction: DAppFunction? = null
    private val canSign = true

    private lateinit var dialog: SignMessageDialog

    //Note: this default RPC is overriden before injection
    private val rpcUrl = "https://mainnet.infura.io/v3/bbfb2fa1009e40e4b72b10166d9a5069" //EthereumNetworkRepository.getDefaultNodeURL(EthereumNetworkBase.MAINNET_ID);


    private fun setupWeb3() {
        web3view.setActivity(this)
        web3view.setChainId(chainId)
        web3view.setRpcUrl(rpcUrl)

        web3view.setWebLoadCallback(this)

        web3view.setWalletAddress(Address(address))
        web3view.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(webview: WebView, newProgress: Int) {
               /* if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE)
                    swipeRefreshLayout.setRefreshing(false)
                    refresh.setEnabled(true)
                } else {
                    progressBar.setVisibility(View.VISIBLE)
                    progressBar.setProgress(newProgress)
                    swipeRefreshLayout.setRefreshing(true)
                }*/
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                //currentWebpageTitle = title
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String,
                                                            callback: GeolocationPermissions.Callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback)
                //requestGeoPermission(origin, callback)
            }

            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
                                           fCParams: FileChooserParams): Boolean {
               /* if (filePathCallback == null) return true
                uploadMessage = filePathCallback
                fileChooserParams = fCParams
                picker = fileChooserParams.createIntent()
                return if (checkReadPermission()) requestUpload() else true*/
                return true
            }
        })
        web3view.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val prefixCheck = url.split(":".toRegex()).toTypedArray()
                if (prefixCheck.size > 1) {
                    val intent: Intent
                    when (prefixCheck[0]) {
                        C.DAPP_PREFIX_TELEPHONE -> {
                            intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse(url)
                            startActivity(Intent.createChooser(intent, "Call " + prefixCheck[1]))
                            return true
                        }
                        C.DAPP_PREFIX_MAILTO -> {
                            intent = Intent(Intent.ACTION_SENDTO)
                            intent.data = Uri.parse(url)
                            startActivity(Intent.createChooser(intent, "Email: " + prefixCheck[1]))
                            return true
                        }
                        C.DAPP_PREFIX_ALPHAWALLET -> if (prefixCheck[1] == C.DAPP_SUFFIX_RECEIVE) {
                            //viewModel.showMyAddress(getContext())
                            return true
                        }
                        C.DAPP_PREFIX_WALLETCONNECT -> {
                            //start walletconnect
                            //viewModel.handleWalletConnect(this, url)
                            return true
                        }
                        else -> {
                        }
                    }
                }

                return false
            }
        })
        web3view.setOnSignMessageListener(this)
        web3view.setOnSignPersonalMessageListener(this)
        web3view.setOnSignTransactionListener(this)
        web3view.setOnSignTypedMessageListener(this)
        /*if (loadOnInit != null) {
            addToBackStack(com.alphawallet.app.ui.DappBrowserFragment.DAPP_BROWSER)
            web3view.loadUrl(Utils.formatUrl(loadOnInit), getWeb3Headers())
            urlTv.setText(Utils.formatUrl(loadOnInit))
        }*/
    }

    /* Required for CORS requests */
    private fun getWeb3Headers(): Map<String?, String?>? {
        //headers
        return object : HashMap<String?, String?>() {
            init {
                put("Connection", "close")
                put("Content-Type", "text/plain")
                put("Access-Control-Allow-Origin", "*")
                put("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
                put("Access-Control-Max-Age", "600")
                put("Access-Control-Allow-Credentials", "true")
                put("Access-Control-Allow-Headers", "accept, authorization, Content-Type")
            }
        }
    }

    override fun onSignTransaction(transaction: Web3Transaction?, url: String?) {
        TODO("Not yet implemented")
    }

    override fun onSignPersonalMessage(message: EthereumMessage?) {
        messageTBS = message
        dAppFunction = object : DAppFunction {
            override fun DAppError(error: Throwable?, message: Signable?) {
                web3view.onSignCancel(message)
                dialog.dismiss()
            }

            // TODO: Weiwu issue #1556 move this code to a class.
            override fun DAppReturn(data: ByteArray?, message: Signable) {
                val signHex = Numeric.toHexString(data)
                web3view.onSignPersonalMessageSuccessful(message, signHex)
                //Test Sig in debug build
                dialog.dismiss()
            }
        }

        try {
            // opens a dialogue to ask the user to sign
            dialog = SignMessageDialog(this, message)
            dialog.setAddress(address)
            dialog.setMessage(message!!.userMessage)
            dialog.setOnApproveListener { v ->
                dialog.dismiss()
                //messageBytes = getEthereumMessage(Numeric.hexStringToByteArray(message!!.getMessage()))
                //viewModel.getAuthorisation(wallet, getActivity(), this)
            }
            dialog.setOnRejectListener { v ->
                web3view.onSignCancel(message)
                dialog.dismiss()
            }
            dialog.show()
        } catch (e: java.lang.Exception) {
            // this will be mainly for developers, so no need to tidy the exception
            // if a user comes across this message they can report to the dapp writer
            onSignError(e.message!!)
        }
    }

    override fun onSignTypedMessage(message: EthereumTypedMessage?) {
        if (message!!.prehash == null) {
            web3view.onSignCancel(message)
        } else {
            messageTBS = message
            dAppFunction = object : DAppFunction {
                override fun DAppError(error: Throwable?, message: Signable?) {
                    web3view.onSignCancel(message)
                    dialog.dismiss()
                }

                override fun DAppReturn(data: ByteArray?, message: Signable) {
                    val signHex = Numeric.toHexString(data)
                    web3view.onSignMessageSuccessful(message, signHex)
                    dialog.dismiss()
                }
            }
            try {
                dialog = SignMessageDialog(this, message)
                dialog.setAddress(address)
                dialog.setOnApproveListener { v ->
                    // TODO: Weiwu: this segment should be encapsulated in EthereumMessage
                    // ensure we generate the signature correctly:
                    if (messageTBS!!.origin != null) {
                        //viewModel.getAuthorisation(wallet, getActivity(), this)
                    } else {
                        onSignError()
                    }
                }
                dialog.setOnRejectListener { v ->
                    if (web3view != null) web3view.onSignCancel(message)
                    dialog.dismiss()
                }
                dialog.show()
            } catch (e: java.lang.Exception) {
                onSignError(e.message!!)
            }
        }
    }

    override fun onSignMessage(message: EthereumMessage?) {
        messageTBS = message
        dAppFunction = object : DAppFunction {
            override fun DAppReturn(data: ByteArray?, message: Signable?) {
                val signHex = Numeric.toHexString(data)
                web3view.onSignMessageSuccessful(message, signHex)
                dialog.dismiss()
            }

            override fun DAppError(error: Throwable?, message: Signable?) {
                web3view.onSignCancel(message)
                dialog.dismiss()
            }

        }

        try {
            dialog = SignMessageDialog(this, message)
            dialog.setAddress(address)
            dialog.setOnApproveListener { v ->
                if (messageTBS?.getMessage() != null) {
                    //viewModel.getAuthorisation(wallet, this, this)
                    dialog.dismiss()
                } else {
                    onSignError()
                }
            }
            dialog.setOnRejectListener { v ->
                if (web3view != null) web3view.onSignCancel(message)
                dialog.dismiss()
            }
            dialog.show()
        } catch (e: Exception) {
        }
    }

    override fun onWebpageLoadComplete() {
        runOnUiThread { setBackForwardButtons() }
    }

    override fun onWebpageLoaded(url: String, title: String) {
        onWebpageLoadComplete()
    }

    override fun signTransaction(transaction: Web3Transaction?, txHex: String?, success: Boolean) {
        TODO("Not yet implemented")
    }

    private fun setBackForwardButtons() {
    }

    private fun onSignError(message: String = "") {

        alert ( message ).show()
    }

//    fun getEthereumMessage(message: ByteArray): ByteArray? {
//        val prefix: ByteArray = com.alphawallet.app.ui.DappBrowserFragment.getEthereumMessagePrefix(message.size)
//        val result = ByteArray(prefix.size + message.size)
//        System.arraycopy(prefix, 0, result, 0, prefix.size)
//        System.arraycopy(message, 0, result, prefix.size, message.size)
//        return result
//    }
}