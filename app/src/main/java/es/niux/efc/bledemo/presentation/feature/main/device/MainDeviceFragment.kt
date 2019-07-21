package es.niux.efc.bledemo.presentation.feature.main.device

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import es.niux.efc.bledemo.R
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelInjectorFactory
import es.niux.efc.core.common.injection.qualifier.For
import es.niux.efc.core.presentation.fragment.BaseFragment
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_device_fragment.*
import timber.log.Timber
import javax.inject.Inject

class MainDeviceFragment : BaseFragment() {
    val args: MainDeviceFragmentArgs by navArgs()

    @field:[Inject For(MainDeviceViewModel::class)]
    internal lateinit var viewModelFactory: ViewModelInjectorFactory
    private val viewModel: MainDeviceViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.deviceUiData.observe(this, Observer { onDevice(it) })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater
        .inflate(R.layout.main_device_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d(viewModel.toString())
    }

    @SuppressLint("SetTextI18n")
    private fun onDevice(data: DeviceUiData) {
        activity!!.pb_main.isIndeterminate =
            data.isLoading

        tv_main_device.text =
            (if (data.isConnected) "Connected!" else "Not Connected!") +
                    "\n" + (data.device?.toString() ?: "null")

        tv_main_device_services.text = data.services
            ?.map { it.toString() }
            ?.reduce { acc, s -> acc + "\n\n" + s }
            ?: "null"
    }
}
