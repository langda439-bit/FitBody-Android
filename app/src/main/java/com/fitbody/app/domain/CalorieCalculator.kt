package com.fitbody.app.domain

import com.fitbody.app.data.db.GOAL_GAIN
import com.fitbody.app.data.db.GOAL_LOSE
import com.fitbody.app.data.db.GOAL_MAINTAIN
import com.fitbody.app.data.db.SEX_MALE
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 热量计算：BMR / TDEE / 目标热量。
 * BMR 采用 Mifflin-St Jeor 公式。
 */
object CalorieCalculator {

    /** 基础代谢率（kcal/天） */
    fun bmr(sex: Int, weightKg: Double, heightCm: Double, ageYears: Int): Double {
        val base = 10.0 * weightKg + 6.25 * heightCm - 5.0 * ageYears
        return if (sex == SEX_MALE) base + 5.0 else base - 161.0
    }

    /** 每日总能量消耗（kcal/天） */
    fun tdee(bmr: Double, activityFactor: Double): Double = bmr * activityFactor

    /**
     * 按目标返回每日建议摄入热量。
     *
     * - 减脂：缺口不超过 500 kcal 且不超过 TDEE 的 20%，不低于安全下限；
     * - 增肌：盈余 200–400 kcal；
     * - 维持：等于 TDEE。
     */
    fun targetIntake(
        sex: Int,
        goalType: String,
        tdee: Double
    ): Int {
        val safeFloor = if (sex == SEX_MALE) MALE_MIN_KCAL else FEMALE_MIN_KCAL
        val raw = when (goalType) {
            GOAL_LOSE -> {
                val deficit = min(MAX_DEFICIT_KCAL, tdee * MAX_DEFICIT_RATIO)
                tdee - deficit
            }
            GOAL_GAIN -> tdee + SURPLUS_KCAL
            GOAL_MAINTAIN -> tdee
            else -> tdee
        }
        return max(safeFloor, raw).roundToInt()
    }

    /** 依据累计热量缺口估算可减少的脂肪重量（kg） */
    fun fatKgFromDeficit(totalDeficitKcal: Double): Double =
        totalDeficitKcal / KCAL_PER_KG_FAT

    private const val MALE_MIN_KCAL = 1500.0
    private const val FEMALE_MIN_KCAL = 1200.0
    private const val MAX_DEFICIT_KCAL = 500.0
    private const val MAX_DEFICIT_RATIO = 0.20
    private const val SURPLUS_KCAL = 300.0
    private const val KCAL_PER_KG_FAT = 7700.0
}
