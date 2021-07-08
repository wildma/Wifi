package com.wildma.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    private val context: Context = this
    private val requestCode: Int = 1
    var mWifiScanResults = HashMap<String, WifiScanResult?>()
    lateinit var mWifiManager: WifiManager
    lateinit var mWifiScanReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 申请权限
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
        }

        // 获取 Wi-Fi 管理器
        mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        registerWifiScanReceiver()

        findViewById<View>(R.id.btn_start_scan).setOnClickListener { startScan() }
        findViewById<View>(R.id.btn_get_wifi_type).setOnClickListener {
            val ssid = connectedWifiSSID
            val wifiScanResult = mWifiScanResults[ssid] ?: return@setOnClickListener
            when {
                wifiScanResult.is24G() -> {
                    Toast.makeText(context, "2.4G", Toast.LENGTH_SHORT).show()
                }
                wifiScanResult.is5G() -> {
                    Toast.makeText(context, "2.5G", Toast.LENGTH_SHORT).show()
                }
                wifiScanResult.is245G() -> {
                    Toast.makeText(context, "2.4G+2.5G", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 开始扫描
     */
    private fun startScan() {
        val success = mWifiManager.startScan()
        if (!success) {
            scanFailure()
        }
    }

    /**
     * 注册 Wi-Fi 扫描广播监听器
     */
    private fun registerWifiScanReceiver() {
        mWifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                val success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(mWifiScanReceiver, intentFilter)
    }

    /**
     * 扫描成功
     */
    private fun scanSuccess() {
        val results = mWifiManager.scanResults
        handleScanResult(results)
    }

    /**
     * 扫描失败（扫描失败则获取的是上一次扫描的结果）
     */
    private fun scanFailure() {
        val results = mWifiManager.scanResults
        handleScanResult(results)
    }

    /**
     * 处理扫描结果
     *
     * @param scanResults 扫描结果集合
     */
    private fun handleScanResult(scanResults: List<ScanResult>) {
        for (i in scanResults.indices) {
            val scanResult = scanResults[i]
            val ssid = scanResult.SSID
            if (!mWifiScanResults.containsKey(ssid)) {
                mWifiScanResults[ssid] = WifiScanResult()
            }
            mWifiScanResults[ssid]?.setFrequency(scanResult.frequency)
        }
    }

    /**
     * 获取当前连接 Wi-Fi 的 SSID
     *
     * @return SSID
     */
    private val connectedWifiSSID: String
        get() {
            val wifiInfo = mWifiManager.connectionInfo
            // 去除双引号
            return wifiInfo?.ssid?.replace("\"", "") ?: ""
        }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mWifiScanReceiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            requestCode -> {
                if (!grantResults.isNotEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "You denied ACCESS_FINE_LOCATION permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}