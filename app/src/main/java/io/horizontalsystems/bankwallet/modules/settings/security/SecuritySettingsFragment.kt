package io.horizontalsystems.bankwallet.modules.settings.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.BaseFragment
import io.horizontalsystems.bankwallet.core.setOnSingleClickListener
import io.horizontalsystems.bankwallet.modules.settings.security.privacy.PrivacySettingsModule
import io.horizontalsystems.pin.PinInteractionType
import io.horizontalsystems.pin.PinModule
import io.horizontalsystems.views.TopMenuItem
import kotlinx.android.synthetic.main.fragment_settings_security.*

class SecuritySettingsFragment : BaseFragment() {

    private val viewModel by viewModels<SecuritySettingsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings_security, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.init()

        shadowlessToolbar.bind(getString(R.string.Settings_SecurityCenter), TopMenuItem(R.drawable.ic_back, onClick = {
            activity?.supportFragmentManager?.popBackStack()
        }))

        changePin.setOnSingleClickListener { viewModel.delegate.didTapEditPin() }

        privacy.setOnSingleClickListener {
            viewModel.delegate.didTapPrivacy()
        }

        biometricAuth.setOnClickListener {
            biometricAuth.switchToggle()
        }

        biometricAuth.setOnCheckedChangeListener {
            viewModel.delegate.didSwitchBiometricEnabled(it)
        }

        enablePin.setOnSingleClickListener {
            enablePin.switchToggle()
        }

        enablePin.setOnCheckedChangeListenerSingle {
            viewModel.delegate.didSwitchPinSet(it)
        }

        //  Handling view model live events

        viewModel.pinSetLiveData.observe(viewLifecycleOwner, Observer { pinEnabled ->
            enablePin.setChecked(pinEnabled)
            enablePin.showAttention(!pinEnabled)
        })

        viewModel.editPinVisibleLiveData.observe(viewLifecycleOwner, Observer { pinEnabled ->
            changePin.isVisible = pinEnabled
            enablePin.showBottomBorder(!pinEnabled)
        })

        viewModel.biometricSettingsVisibleLiveData.observe(viewLifecycleOwner, Observer { enabled ->
            biometricAuth.isVisible = enabled
            txtBiometricAuthInfo.isVisible = enabled
        })

        viewModel.biometricEnabledLiveData.observe(viewLifecycleOwner, Observer {
            biometricAuth.setChecked(it)
        })

        //  Router

        viewModel.openEditPinLiveEvent.observe(viewLifecycleOwner, Observer {
            activity?.supportFragmentManager?.commit {
                add(R.id.fragmentContainerView, PinModule.startForEditPin())
                addToBackStack(null)
            }
        })

        viewModel.openSetPinLiveEvent.observe(viewLifecycleOwner, Observer {
            activity?.supportFragmentManager?.commit {
                add(R.id.fragmentContainerView, PinModule.startForSetPin())
                addToBackStack(null)
            }
        })

        viewModel.openUnlockPinLiveEvent.observe(viewLifecycleOwner, Observer {
            activity?.supportFragmentManager?.commit {
                add(R.id.fragmentContainerView, PinModule.startForUnlock())
                addToBackStack(null)
            }
        })

        viewModel.openPrivacySettingsLiveEvent.observe(viewLifecycleOwner, Observer {
            activity?.let {
                PrivacySettingsModule.start(it)
            }
        })

        subscribeFragmentResult()
    }

    private fun subscribeFragmentResult() {
        val fragmentActivity = activity ?: return

        fragmentActivity.supportFragmentManager.setFragmentResultListener(PinModule.requestKey, viewLifecycleOwner) { requestKey, bundle ->
            val resultType = bundle.getParcelable<PinInteractionType>(PinModule.requestType)
            val resultCode = bundle.getInt(PinModule.requestResult)

            if (resultType == PinInteractionType.SET_PIN) {
                when (resultCode) {
                    PinModule.RESULT_OK -> viewModel.delegate.didSetPin()
                    PinModule.RESULT_CANCELLED -> viewModel.delegate.didCancelSetPin()
                }
            }

            if (resultType == PinInteractionType.UNLOCK) {
                when (resultCode) {
                    PinModule.RESULT_OK -> viewModel.delegate.didUnlockPinToDisablePin()
                    PinModule.RESULT_CANCELLED -> viewModel.delegate.didCancelUnlockPinToDisablePin()
                }
            }
        }
    }
}