package ru.reeg.moexmonitor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ru.reeg.moexmonitor.databinding.ActivityMainBinding

/**
 * Single-Activity: единственная активность держит NavHostFragment,
 * а экраны приходят из feature-модулей.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHost) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // Нижнюю навигацию показываем только на корневых экранах вкладок.
        val topLevelGraphIds = binding.bottomNav.menu.children.map { it.itemId }.toSet()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val parent = destination.parent
            val isTabRoot = parent != null &&
                parent.id in topLevelGraphIds &&
                parent.startDestinationId == destination.id
            binding.bottomNav.isVisible = isTabRoot
        }
    }
}
