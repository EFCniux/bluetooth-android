package es.niux.efc.bledemo.presentation.feature.main.devices

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import es.niux.efc.bledemo.R
import es.niux.efc.bledemo.presentation.feature.main.MainEvent
import es.niux.efc.bledemo.presentation.feature.main.MainViewModel
import es.niux.efc.bluetooth.data.source.event.BleScanEvent
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelInjectorFactory
import es.niux.efc.core.common.injection.qualifier.For
import es.niux.efc.core.presentation.fragment.BaseFragment
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_devices_fragment.*
import javax.inject.Inject

class MainDevicesFragment : BaseFragment() {
    @field:[Inject For(MainViewModel::class)]
    internal lateinit var activityViewModelFactory: ViewModelInjectorFactory
    private val activityViewModel: MainViewModel by activityViewModels { activityViewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityViewModel.bleScan
            .observe(this, Observer { onBleScanList(it) })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater
        .inflate(R.layout.main_devices_fragment, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        rv_main_devices.layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)
        rv_main_devices.addItemDecoration(DividerItemDecoration(view.context, RecyclerView.VERTICAL))
        rv_main_devices.adapter = BleScanAdapter()
            .apply { listenerSelection = { onBleScanEventSelected(it) } }
    }

    private fun onBleScanList(list: List<BleScanEvent.Found>) {
        activity!!.pb_main.isIndeterminate =
            list.isEmpty()

        (rv_main_devices.adapter as BleScanAdapter)
            .submitList(list)
    }

    private fun onBleScanEventSelected(event: BleScanEvent.Found): Unit = activityViewModel
        .navigate(MainEvent.Nav.Device(event.device.address))
}
