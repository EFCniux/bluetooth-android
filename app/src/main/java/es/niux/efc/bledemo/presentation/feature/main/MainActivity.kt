package es.niux.efc.bledemo.presentation.feature.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import es.niux.efc.bledemo.NgMainDirections
import es.niux.efc.bledemo.R
import es.niux.efc.bledemo.presentation.feature.main.devices.MainDevicesFragmentDirections
import es.niux.efc.bledemo.presentation.feature.main.state.MainStateFragmentDirections
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelInjectorFactory
import es.niux.efc.core.common.injection.qualifier.For
import es.niux.efc.core.presentation.activity.BaseActivity
import kotlinx.android.synthetic.main.main_activity.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : BaseActivity() {
    @field:[Inject For(MainViewModel::class)]
    internal lateinit var viewModelFactory: ViewModelInjectorFactory
    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    private val navController: NavController by lazy {
        findNavController(R.id.nhf_main)
    }

    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(setOf(
            R.id.nd_main_state_fragment,
            R.id.nd_main_devices_fragment
        ), dl_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(tb_main)
        setupActionBarWithNavController(navController, appBarConfiguration)
        nv_main.setupWithNavController(navController)

        viewModel.mainConsumables
            .observe(this, Observer { events ->
                events.forEach {
                    if (it.peek() is MainEvent.Nav)
                        onMainNavEvent(it.consume() as MainEvent.Nav)
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun onMainNavEvent(event: MainEvent.Nav) {
        val curDest: NavDestination? = navController.currentDestination
        when {
            event is MainEvent.Nav.Status && curDest?.id != R.id.nd_main_state_fragment -> navController
                .navigate(
                    NgMainDirections
                        .naMainGlobalState()
                )
            event is MainEvent.Nav.Devices && curDest?.id != R.id.nd_main_devices_fragment -> navController
                .navigate(
                    MainStateFragmentDirections
                        .naMainStateDevices()
                )
            event is MainEvent.Nav.Device && curDest?.id != R.id.nd_main_device_fragment -> navController
                .navigate(
                    MainDevicesFragmentDirections
                        .naMainDevicesDevice(event.deviceAddress)
                )
            else -> Timber
                .w(
                    "Current destination is %s (id: %s), ignored navigation event: %s",
                    curDest?.label, curDest?.id, event.toString()
                )
        }
    }
}
