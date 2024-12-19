package pt.ms.myshare.ui

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import pt.ms.myshare.R
import pt.ms.myshare.databinding.ActivityMainBinding
import pt.ms.myshare.utils.insetsCallBack.InsetsWithKeyboardCallback
import pt.ms.myshare.utils.isDarkTheme
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.util.Locale

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val appBar: AppBarLayout by lazy { binding.appBarLayout }
    val collapsingToolbar: CollapsingToolbarLayout by lazy { binding.collapsingToolbar }
    val toolbar: Toolbar by lazy { binding.toolbar }
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag("SDK version").i("${Build.VERSION.SDK_INT}")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(toolbar)

        val insetsWithKeyboardCallback = InsetsWithKeyboardCallback(window, collapsingToolbar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, insetsWithKeyboardCallback)
        ViewCompat.setWindowInsetsAnimationCallback(binding.root, insetsWithKeyboardCallback)

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        logAppStart()
    }

    private fun logAppStart() {
        FirebaseUtils.logEvent(
            "app_start",
            Bundle().apply {
                putString("device_language", Locale.getDefault().language)
                putString("is_system_dark_theme", this@MainActivity.isDarkTheme().toString())
            },
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) ||
            super.onSupportNavigateUp()
    }
}
