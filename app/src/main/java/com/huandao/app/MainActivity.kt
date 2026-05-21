package com.huandao.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import com.huandao.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    // 通知权限请求 Launcher（Android 13+）
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 用户授权或拒绝后的回调，可用于埋点或逻辑处理
        // isGranted == true 表示用户允许通知
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在 super.onCreate 之前调用，安装官方 Splash Screen
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupPullToRefresh()
        setupErrorView()
        setupBackNavigation()
        setupNetworkListener()
        requestNotificationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let {
            (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                .unregisterNetworkCallback(it)
        }
    }

    /**
     * Android 13+ 通知权限申请（合规要求）
     * 推送功能依赖此权限才能正常展示通知
     */
    @Suppress("DEPRECATION")
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                // 已授权
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 已授权，无需操作
                }
                // 用户首次申请，弹出系统权限对话框
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // 可以引导用户去设置页面开启
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // 首次请求权限
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        // Android 12 及以下不需要运行时权限（已在 Manifest 声明即可）
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(false)

                // ---- 缓存策略：优先加载缓存，离线也能看 ----
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

                // ---- JS Bridge 接口名称 ----
                // 注册后网页可通过 window.HuanDao 调用以下方法
                //   window.HuanDao.share(title, url)
                //   window.HuanDao.showToast(message)
                //   window.HuanDao.getDeviceInfo()  → 返回 JSON
            }

            // 注册 JS Bridge
            addJavascriptInterface(HuandaoBridge(this@MainActivity), "HuanDao")

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean = false

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    hideErrorView()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.swipeRefresh.isRefreshing = false
                    binding.progressBar.isVisible = false
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    if (failingUrl == view?.url) {
                        showErrorView()
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.apply {
                        isVisible = newProgress < 100
                        progress = newProgress
                    }
                }
            }

            val url = getString(R.string.web_url)
            loadUrl(url)
        }
    }

    private fun setupPullToRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(
                android.R.color.holo_orange_light
            )
            setOnRefreshListener {
                binding.webView.reload()
            }
        }
    }

    private fun setupErrorView() {
        binding.retryButton.setOnClickListener {
            hideErrorView()
            binding.webView.reload()
        }
    }

    private fun showErrorView() {
        binding.errorView.isVisible = true
        binding.progressBar.isVisible = false
    }

    private fun hideErrorView() {
        binding.errorView.isVisible = false
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupNetworkListener() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    if (binding.errorView.isVisible) {
                        hideErrorView()
                        binding.webView.reload()
                    }
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }
}
