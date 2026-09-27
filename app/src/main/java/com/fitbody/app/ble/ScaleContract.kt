package com.fitbody.app.ble

import java.util.UUID

/**
 * FG25028WB（货号 A1）体脂秤对接常量。
 *
 * 重要说明：该设备为蚂蚁阿福 × 沃莱科技联名定制款，GATT 服务/特征 UUID 与数据帧
 * 格式属于厂商私有协议。下列 UUID 与帧布局为“通用 BLE 体重秤参考值”，正式联调前
 * 必须以沃莱科技提供的《BLE 通信协议文档》或安卓 SDK 为准进行替换。
 */
object ScaleContract {

    /** 目标设备型号与货号 */
    const val MODEL = "FG25028WB"
    const val ITEM_NO = "A1"

    /**
     * 广播设备名匹配前缀（候选）。定制款实际名称以协议/实测为准，
     * 因此这里保留多个候选，命中任意一个即视为目标设备。
     */
    val NAME_PREFIXES = listOf("FG25028", "FG", "ICOMON", "FITDAYS", "Welland", "A1")

    /** 自定义数据服务（参考 UUID，待协议确认） */
    val SERVICE_SCALE: UUID = UUID.fromString("0000ffb0-0000-1000-8000-00805f9b34fb")

    /** 通知特征：设备上报体重/阻抗（参考 UUID，待协议确认） */
    val CHAR_MEASUREMENT_NOTIFY: UUID = UUID.fromString("0000ffb1-0000-1000-8000-00805f9b34fb")

    /** 写特征：App 下发测量/查询指令（参考 UUID，待协议确认） */
    val CHAR_COMMAND: UUID = UUID.fromString("0000ffb2-0000-1000-8000-00805f9b34fb")

    /** 标准 CCCD 描述符 */
    val DESC_CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    /** 申请的 MTU，提升单帧承载能力 */
    const val TARGET_MTU = 247

    /** 体重合理量程（kg），超出视为非人体测量 */
    const val MIN_HUMAN_WEIGHT_KG = 20.0
    const val MAX_HUMAN_WEIGHT_KG = 200.0
}
