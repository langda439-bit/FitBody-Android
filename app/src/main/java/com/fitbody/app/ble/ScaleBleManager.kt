package com.fitbody.app.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 体脂秤 BLE 中央管理器。
 *
 * 职责：扫描 FG25028WB/A1 → 建立 GATT → 订阅通知 → 解析实时体重与 BIA →
 * 完成后回到扫描等待下一次上秤；连接异常按退避策略自动重连。
 *
 * 蓝牙运行时权限（BLUETOOTH_SCAN/BLUETOOTH_CONNECT 等）由调用方
 * （UI / [ScaleForegroundService]）在启动前确保已授予。
 */
@SuppressLint("MissingPermission")
class ScaleBleManager(private val context: Context) {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter

    private val parser = ScaleProtocolParser()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var gatt: BluetoothGatt? = null
    private var boundMac: String? = null
    private var scanning = false
    private var retryCount = 0
    private var manualStopped = false

    private val _state = MutableStateFlow(ScaleConnectionState.IDLE)
    val state: StateFlow<ScaleConnectionState> = _state.asStateFlow()

    private val _discoveredDevice = MutableStateFlow<ScaleDevice?>(null)
    val discoveredDevice: StateFlow<ScaleDevice?> = _discoveredDevice.asStateFlow()

    private val _liveMeasurement = MutableStateFlow<ScaleMeasurement?>(null)
    val liveMeasurement: StateFlow<ScaleMeasurement?> = _liveMeasurement.asStateFlow()

    /** 测量完成回调（稳定值 + 阻抗），供上层入库 */
    var onMeasurementComplete: ((ScaleMeasurement) -> Unit)? = null

    // ---------------- 扫描回调 ----------------
    private val scanCallback = object : android.bluetooth.le.ScanCallback() {
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            val device = result.device
            val name = result.scanRecord?.deviceName ?: device.name
            if (isTargetDevice(name)) {
                _discoveredDevice.value = ScaleDevice(
                    macAddress = device.address,
                    name = name,
                    rssi = result.rssi
                )
                connect(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "scan failed: $errorCode")
            _state.value = ScaleConnectionState.IDLE
            scheduleRetry()
        }
    }

    // ---------------- GATT 回调 ----------------
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "GATT connected")
                    _state.value = ScaleConnectionState.CONNECTED
                    retryCount = 0
                    g.requestMtu(ScaleContract.TARGET_MTU)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.w(TAG, "GATT disconnected, status=$status")
                    cleanupConnection()
                    _state.value = ScaleConnectionState.DISCONNECTED
                    if (!manualStopped) scheduleRetry()
                }
            }
        }

        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            g.discoverServices()
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val service = g.getService(ScaleContract.SERVICE_SCALE)
            if (service == null) {
                Log.e(TAG, "scale service not found")
                g.disconnect()
                return
            }
            val notifyChar = service.getCharacteristic(ScaleContract.CHAR_MEASUREMENT_NOTIFY)
            if (notifyChar != null) {
                g.setCharacteristicNotification(notifyChar, true)
                notifyChar.getDescriptor(ScaleContract.DESC_CCCD)?.let { desc ->
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    g.writeDescriptor(desc)
                }
            }
            _state.value = ScaleConnectionState.MEASURING
        }

        @Deprecated("Deprecated in API 33, kept for minSdk 28 compatibility")
        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: android.bluetooth.BluetoothGattCharacteristic
        ) {
            characteristic.value?.let { handleData(it) }
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: android.bluetooth.BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleData(value)
        }
    }

    private fun handleData(bytes: ByteArray) {
        val measurement = parser.parse(bytes) ?: return
        _liveMeasurement.value = measurement
        if (measurement.isStable) {
            _state.value = ScaleConnectionState.DONE
            onMeasurementComplete?.invoke(measurement)
            // 完成后断开，回到扫描等待下一次上秤
            mainHandler.postDelayed({
                gatt?.disconnect()
            }, 500)
        }
    }

    // ---------------- 对外 API ----------------
    /** 蓝牙是否可用（开关已打开且设备支持 BLE） */
    fun isBluetoothReady(): Boolean = adapter != null && adapter.isEnabled

    /** 绑定设备 MAC；绑定后优先直连 */
    fun setBoundDevice(mac: String?) {
        boundMac = mac
    }

    /** 启动自动称重流程（扫描 → 连接） */
    fun start() {
        manualStopped = false
        if (!isBluetoothReady()) {
            _state.value = ScaleConnectionState.UNAVAILABLE
            return
        }
        startScan()
    }

    fun stop() {
        manualStopped = true
        stopScan()
        cleanupConnection()
        _state.value = ScaleConnectionState.IDLE
    }

    // ---------------- 内部实现 ----------------
    private fun isTargetDevice(name: String?): Boolean {
        if (name == null) return false
        return ScaleContract.NAME_PREFIXES.any { name.startsWith(it, ignoreCase = true) }
    }

    private fun startScan() {
        if (scanning) return
        if (!isBluetoothReady()) {
            _state.value = ScaleConnectionState.UNAVAILABLE
            return
        }
        _state.value = ScaleConnectionState.SCANNING
        scanning = true

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(ScaleContract.SERVICE_SCALE))
                .build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        adapter.bluetoothLeScanner.startScan(filters, settings, scanCallback)
        Log.i(TAG, "scan started, waiting for scale wake-up")
    }

    private fun stopScan() {
        if (!scanning) return
        runCatching {
            adapter.bluetoothLeScanner.stopScan(scanCallback)
        }
        scanning = false
    }

    private fun connect(device: BluetoothDevice) {
        stopScan()
        _state.value = ScaleConnectionState.CONNECTING
        boundMac = device.address
        gatt = device.connectGatt(
            context,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    private fun cleanupConnection() {
        gatt?.let {
            runCatching { it.close() }
        }
        gatt = null
    }

    private fun scheduleRetry() {
        mainHandler.removeCallbacks(retryRunnable)
        val delayMs = BACKOFF_MS.getOrElse(retryCount) { BACKOFF_MS.last() }
        retryCount++
        Log.i(TAG, "retry in ${delayMs}ms (count=$retryCount)")
        mainHandler.postDelayed(retryRunnable, delayMs)
    }

    private val retryRunnable = Runnable {
        if (!manualStopped) startScan()
    }

    companion object {
        private const val TAG = "ScaleBleManager"
        private const val BACKOFF_MS = longArrayOf(1000, 2000, 4000, 8000)
    }
}
