# 体重训练管家（FitBody-Android）

融合 **蓝牙体脂秤自动称重 · 拍照识别食物热量 · 个性化训练计划 · 热量计算** 的安卓健康管理 App。

## 核心体验

打开 App → 赤脚踩上体脂秤 → 蓝牙自动连接 → 自动记录体重与体脂数据，全程无需手动点击“连接”。

## 对接硬件

- 蚂蚁阿福 × 沃莱科技 联名智能体脂秤
- 型号 **FG25028WB**，货号 **A1**
- BLE 蓝牙、BIA 交流测脂、Type-C 充电、约 18 项身体数据

## 功能模块

| 模块 | 说明 |
|---|---|
| 蓝牙体脂秤接入 | 扫描/连接状态机、自动重连、前台服务、数据帧解析、多用户识别 |
| 体重管理 | 18 项指标、趋势曲线、目标管理、数据导出 |
| 热量计算 | BMR/TDEE（Mifflin-St Jeor）、热量缺口、摄入/消耗仪表盘 |
| 拍照食物识别 | CameraX 拍照、菜品识别、重量与营养估算、人工校正 |
| 训练计划 | 个性化计划生成、训练日历、动作库、训练记录与数据联动 |

## 技术栈

- Kotlin、Jetpack Compose、Material3、Navigation Compose
- Room（KSP）、Coroutines、WorkManager
- CameraX
- minSdk 28 / targetSdk 36（Android 16），首发适配红米 K90 至尊版（HyperOS 4）

## 构建方式

- **Android Studio**：直接打开本工程根目录，等待 Gradle 同步后运行。
- **命令行**：`./gradlew assembleDebug`（需本机安装 Android SDK，并配置 `local.properties` 的 `sdk.dir`）。
- **CI**：每次推送自动通过 GitHub Actions（`.github/workflows/android.yml`）编译并产出 debug APK。

## 关键对接说明（重要）

FG25028WB/A1 为联名定制款，其 GATT 服务/特征 UUID 与数据帧格式属于厂商私有协议。
当前工程中：

- `ble/ScaleContract.kt` 内的 UUID、设备名前缀为**参考值**；
- `ble/ScaleProtocolParser.kt` 为基于通用体脂秤帧布局的**参考解析器**；
- `domain/BodyCompositionCalculator.kt` 为公开经验公式的**占位实现**。

正式联调前，必须取得沃莱科技提供的 **BLE 通信协议文档或安卓 SDK**，替换上述常量/解析逻辑；
体成分结果优先使用厂商 SDK 返回值。

## 当前状态

- 工程骨架、BLE 自动称重链路、数据层、热量计算、Compose 界面与拍照入口已就绪；
- 食物识别模型与训练动作明细为占位，待接入模型与内容。
