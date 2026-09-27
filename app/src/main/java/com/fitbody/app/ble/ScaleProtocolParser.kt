package com.fitbody.app.ble

/**
 * 体脂秤数据帧解析器（参考实现）。
 *
 * 字段偏移、字节序、倍率与校验算法均依据“通用 BLE 体脂秤”帧布局给出，
 * 联调 FG25028WB/A1 时必须按沃莱协议文档校正 [FRAME_*] 常量。
 */
class ScaleProtocolParser {

    /** 解析结果：成功为测量数据，帧不完整/校验失败为 null */
    fun parse(bytes: ByteArray): ScaleMeasurement? {
        if (bytes.size < FRAME_MIN_LEN) return null
        if (!verifyChecksum(bytes)) return null

        val type = bytes[OFFSET_TYPE].toInt() and 0xFF
        // 仅处理测量数据帧（参考类型 0x01 / 0x02）
        if (type != TYPE_REALTIME && type != TYPE_FINAL) return null

        val rawWeight = (bytes[OFFSET_WEIGHT_LOW].toInt() and 0xFF) or
            ((bytes[OFFSET_WEIGHT_HIGH].toInt() and 0xFF) shl 8)
        val weightKg = rawWeight * WEIGHT_SCALE

        // 体重合理性校验，过滤物品称重
        if (weightKg < ScaleContract.MIN_HUMAN_WEIGHT_KG ||
            weightKg > ScaleContract.MAX_HUMAN_WEIGHT_KG
        ) {
            return null
        }

        val stableFlag = bytes[OFFSET_FLAGS].toInt() and 0xFF
        val isStable = (stableFlag and STABLE_BIT) != 0 || type == TYPE_FINAL

        val rawImpedance = (bytes[OFFSET_IMPEDANCE_LOW].toInt() and 0xFF) or
            ((bytes[OFFSET_IMPEDANCE_HIGH].toInt() and 0xFF) shl 8)
        val impedance = if (rawImpedance in VALID_IMPEDANCE_RANGE) rawImpedance else null

        val battery = bytes[OFFSET_BATTERY].toInt() and 0xFF
        val batteryLevel = if (battery in 0..100) battery else null

        return ScaleMeasurement(
            weightKg = round2(weightKg),
            impedanceOhm = impedance,
            isStable = isStable,
            batteryLevel = batteryLevel
        )
    }

    private fun verifyChecksum(bytes: ByteArray): Boolean {
        // 参考校验：前 N-1 字节异或 == 末字节
        var xorSum = 0
        for (i in 0 until bytes.size - 1) {
            xorSum = xorSum xor (bytes[i].toInt() and 0xFF)
        }
        return xorSum == (bytes[bytes.size - 1].toInt() and 0xFF)
    }

    private fun round2(v: Double): Double = Math.round(v * 100.0) / 100.0

    companion object {
        // ---- 参考帧布局（待协议文档确认） ----
        private const val FRAME_MIN_LEN = 13

        private const val OFFSET_TYPE = 0
        private const val OFFSET_WEIGHT_LOW = 2
        private const val OFFSET_WEIGHT_HIGH = 3
        private const val OFFSET_FLAGS = 4
        private const val OFFSET_IMPEDANCE_LOW = 8
        private const val OFFSET_IMPEDANCE_HIGH = 9
        private const val OFFSET_BATTERY = 10

        private const val TYPE_REALTIME = 0x01
        private const val TYPE_FINAL = 0x02

        private const val STABLE_BIT = 0x01

        /** 体重原始值倍率：raw × 0.05 = kg（部分型号为 0.01） */
        private const val WEIGHT_SCALE = 0.05

        private val VALID_IMPEDANCE_RANGE = 200..1200
    }
}
