package com.huandao.app

import android.content.Context
import android.content.Intent
import android.os.Build
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONObject

/**
 * JS Bridge — 让网页（JavaScript）可以调用原生 Android 功能。
 *
 * 网页端调用方式：
 *   window.HuanDao.share('标题', 'https://example.com');
 *   window.HuanDao.showToast('提示内容');
 *   window.HuanDao.getDeviceInfo(function(info) { console.log(info); });
 */
class HuandaoBridge(private val context: Context) {

    /**
     * 分享链接
     * @param title  分享标题
     * @param url    分享链接
     */
    @JavascriptInterface
    fun share(title: String, url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n$url")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_title)))
    }

    /**
     * 显示 Toast 提示
     * @param message 提示内容
     */
    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 获取设备信息，以 JSON 字符串返回
     */
    @JavascriptInterface
    fun getDeviceInfo(): String {
        val info = JSONObject().apply {
            put("os", "Android ${Build.VERSION.RELEASE}")
            put("sdk", Build.VERSION.SDK_INT)
            put("manufacturer", Build.MANUFACTURER)
            put("model", Build.MODEL)
            put("appVersion", BuildConfig.VERSION_NAME)
        }
        return info.toString()
    }
}
