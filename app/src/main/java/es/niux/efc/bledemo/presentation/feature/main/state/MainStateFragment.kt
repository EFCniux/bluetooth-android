package es.niux.efc.bledemo.presentation.feature.main.state

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import es.niux.efc.bledemo.R
import es.niux.efc.bledemo.presentation.feature.main.MainViewModel
import es.niux.efc.bluetooth.data.source.event.BleAvailabilityEvent
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelInjectorFactory
import es.niux.efc.core.common.injection.qualifier.For
import es.niux.efc.core.presentation.fragment.BaseFragment
import kotlinx.android.synthetic.main.main_state_fragment.*
import javax.inject.Inject

class MainStateFragment : BaseFragment() {
    @field:[Inject For(MainViewModel::class)]
    internal lateinit var activityViewModelFactory: ViewModelInjectorFactory
    private val activityViewModel: MainViewModel by activityViewModels { activityViewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityViewModel.bleAvailability
            .observe(this, Observer {
                onBleAvailability(it)
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater
        .inflate(R.layout.main_state_fragment, container, false)

    @SuppressLint("SetTextI18n")
    private fun onBleAvailability(event: BleAvailabilityEvent) {
        tv_main_state_unavailable.text = when (event) {
            BleAvailabilityEvent.Available ->
                "BleAvailabilityEvent\nAvailable"
            BleAvailabilityEvent.Unavailable.Permanent ->
                "BleAvailabilityEvent\nUnavailable\nPermanent"
            BleAvailabilityEvent.Unavailable.Disabled ->
                "BleAvailabilityEvent\nUnavailable\nDisabled"
            BleAvailabilityEvent.Unavailable.Location.Permission ->
                "BleAvailabilityEvent\nUnavailable\nLocation\nPermission"
            BleAvailabilityEvent.Unavailable.Location.Services ->
                "BleAvailabilityEvent\nUnavailable\nLocation\nServices"
        }
    }
}
