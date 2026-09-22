// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import org.studiodroid.app.databinding.ActivityMainBinding
import org.studiodroid.app.screen.*

class MainActivity : AppCompatActivity() {
    private val graph get() = (application as StudioDroidApp).graph
    private val model: LauncherViewModel by viewModels {
        viewModelFactory { initializer { LauncherViewModel(graph, createSavedStateHandle()) } }
    }
    private val client by lazy { graph.client() }
    private var lastDestination: Destination? = null
    private lateinit var binding: ActivityMainBinding
    private val exportDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) {
            val resolver = applicationContext.contentResolver
            model.export { resolver.openOutputStream(uri, "w") }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainPanel) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        binding.toolbar.setNavigationOnClickListener { binding.drawer.openDrawer(GravityCompat.START) }
        binding.navigation.setNavigationItemSelectedListener { item ->
            destination(item.itemId)?.let(model::select)
            binding.drawer.closeDrawer(GravityCompat.START)
            true
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    binding.drawer.isDrawerOpen(GravityCompat.START) -> binding.drawer.closeDrawer(GravityCompat.START)
                    model.state.value.destination != Destination.HOME -> model.select(Destination.HOME)
                    else -> { isEnabled = false; onBackPressedDispatcher.onBackPressed(); isEnabled = true }
                }
            }
        })
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { model.state.collect(::render) }
        }
    }
    override fun onStart() { super.onStart(); client.connect() }
    override fun onStop() { client.disconnect(); model.onHidden(); binding.content.removeAllViews(); super.onStop() }
    private fun render(state: LauncherUiState) {
        binding.toolbar.title = getString(title(state.destination))
        binding.navigation.setCheckedItem(menuId(state.destination))
        val scrollY = if (lastDestination == state.destination) (binding.content.getChildAt(0) as? NestedScrollView)?.scrollY ?: 0 else 0
        lastDestination = state.destination
        val page = PageBuilder(this, binding.content)
        if (state.message != null) page.notice(state.message)
        state.runtime.probeError?.let(page::notice)
        val actions = ScreenActions(model::refresh, { exportDocument.launch("studiodroid-diagnostics.zip") }, model::setProfile)
        when (state.destination) {
            Destination.HOME -> Home.render(page, state, actions)
            Destination.RUNTIME -> Runtime.render(page, state, actions)
            Destination.DIAGNOSTICS -> Diagnostics.render(page, state, actions)
            Destination.STORAGE -> Storage.render(page, state, actions)
            Destination.LOGS -> Logs.render(page, state, actions)
            Destination.SETTINGS -> Settings.render(page, state, actions)
            Destination.ABOUT -> About.render(page, state, actions)
        }
        binding.content.getChildAt(0)?.let { view -> view.post { view.scrollTo(0, scrollY) } }
    }
    private fun menuId(destination: Destination): Int = when (destination) {
        Destination.HOME -> R.id.nav_home; Destination.RUNTIME -> R.id.nav_runtime; Destination.DIAGNOSTICS -> R.id.nav_diagnostics
        Destination.STORAGE -> R.id.nav_storage; Destination.LOGS -> R.id.nav_logs; Destination.SETTINGS -> R.id.nav_settings; Destination.ABOUT -> R.id.nav_about
    }
    private fun destination(id: Int) = Destination.entries.firstOrNull { menuId(it) == id }
    private fun title(destination: Destination): Int = when (destination) {
        Destination.HOME -> R.string.home; Destination.RUNTIME -> R.string.runtime; Destination.DIAGNOSTICS -> R.string.diagnostics
        Destination.STORAGE -> R.string.storage; Destination.LOGS -> R.string.logs; Destination.SETTINGS -> R.string.settings; Destination.ABOUT -> R.string.about
    }
}
