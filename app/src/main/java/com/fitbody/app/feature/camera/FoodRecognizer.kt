package com.fitbody.app.feature.camera

import android.graphics.Bitmap

/** 识别出的食物条目 */
data class RecognizedFood(
    val name: String,
    val grams: Double,
    val kcal: Double,
    val confidence: Float
)

/**
 * 食物识别器。
 *
 * 正式实现两条路径：
 * 1. 端侧：TFLite / NNAPI（骁龙 8 至尊版 NPU）菜品检测 + 重量估计，支持离线；
 * 2. 云端：复杂图片上传，返回菜品与营养。
 * 当前为骨架占位，返回空结果并由 UI 引导手动录入。
 */
interface FoodRecognizer {
    suspend fun recognize(bitmap: Bitmap): List<RecognizedFood>
}

/** 占位识别器：尚未接入模型时返回空列表 */
class PlaceholderFoodRecognizer : FoodRecognizer {
    override suspend fun recognize(bitmap: Bitmap): List<RecognizedFood> = emptyList()
}
