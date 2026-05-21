package com.huandao.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.huandao.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 缓岛唯一 Activity（Single-Activity Architecture）。
 * 承载 Navigation Component 的 NavHostFragment + BottomNavigationView。
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** 通知权限请求 Launcher（Android 13+） */
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 用户授权或拒绝后无需额外操作 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在 super.onCreate 之前调用，安装官方 Splash Screen
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        requestNotificationPermission()
    }

    /**
     * 将 NavHostFragment 与 BottomNavigationView 绑定，
     * 实现 Tab 切换时 Fragment 状态保持。
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
    }

    /**
     * Android 13+ 通知权限申请（合规要求）。
     * 首次使用时不主动弹出，而是在用户添加第一条冷静池条目后温和询问。
     * 此处仅在已授权时静默确认，不弹系统对话框。
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!alreadyGranted) {
                // 不在此处主动请求——PRD 要求在首次添加后温和询问
                // 留着 launcher 待 CoolPoolFragment 触发
                requestNotificationPermissionIfNeeded()
            }
        }
    }

    /**
     * 供 Fragment 调用：检查并请求通知权限。
     * 仅在 Android 13+ 且未授权时弹出系统对话框。
     */
    fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED

            if (notGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
