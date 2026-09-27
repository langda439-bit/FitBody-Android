package com.fitbody.app.ble

/**
 * 体脂秤连接状态机。
 */
enum class ScaleConnectionState {
    /** 未绑定或蓝牙未初始化 */
    IDLE,

    /** 蓝牙关闭或缺少权限 */
    UNAVAILABLE,

    /** 正在扫描已绑定设备（等待用户上秤唤醒） */
    SCANNING,

    /** 发现设备，正在建立 GATT 连接 */
    CONNECTING,

    /** 已连接，正在发现服务/订阅通知 */
    CONNECTED,

    /** 正在接收实时体重与 BIA 数据 */
    MEASURING,

    /** 本次测量完成并已入库 */
    DONE,

    /** 连接断开，准备自动重连 */
    DISCONNECTED
}

/**
 * 一次扫描中发现的体脂秤设备摘要。
 */
data class ScaleDevice(
    /** 蓝牙 MAC 地址，已绑定设备据此快速直连 */
    val macAddress: String,
    /** 广播设备名 */
    val name: String?,
    /** 信号强度 RSSI */
    val rssi: Int
)

/**
 * 一次完整称重的结果。
 *
 * @param weightKg 稳定体重，单位 kg
 * @param impedanceOhm BIA 阻抗，单位 Ω；穿鞋或未测脂时为 null
 * @param isStable 体重是否已稳定
 * @param batteryLevel 设备电量 0..100，未知为 null
 * @param timestamp 测量时间（System.currentTimeMillis）
 */
data class ScaleMeasurement(
    val weightKg: Double,
    val impedanceOhm: Int? = null,
    val isStable: Boolean = false,
    val batteryLevel: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
