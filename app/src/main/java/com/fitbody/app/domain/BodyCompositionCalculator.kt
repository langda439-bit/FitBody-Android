package com.fitbody.app.domain

import com.fitbody.app.data.db.SEX_MALE
import kotlin.math.round

/**
 * 体成分估算（参考实现）。
 *
 * 体脂秤厂商的 BIA 体成分模型属于专有算法（沃莱有 DEXA 实验室标定模型）。
 * 这里给出基于 BMI / 年龄 / 性别的公开经验公式（Deurenberg）作为占位参考，
 * 正式版应替换为沃莱 SDK 返回值或经标定的模型结果。
 */
object BodyCompositionCalculator {

    fun bmi(weightKg: Double, heightCm: Double): Double {
        val m = heightCm / 100.0
        return round2(weightKg / (m * m))
    }

    /**
     * 体脂率参考估算（Deurenberg）：
     * BF% = 1.2·BMI + 0.23·年龄 − 10.8·性别(男1女0) − 5.4
     * 若有阻抗，可在后续模型中替代/修正该结果。
     */
    fun bodyFatPct(
        sex: Int,
        ageYears: Int,
        bmi: Double,
        impedanceOhm: Int?
    ): Double {
        val sexValue = if (sex == SEX_MALE) 1.0 else 0.0
        var bf = 1.2 * bmi + 0.23 * ageYears - 10.8 * sexValue - 5.4
        // 阻抗参考微调（占位，正式模型应删除）
        if (impedanceOhm != null) {
            bf += ((impedanceOhm - 500) / 200.0).coerceIn(-1.5, 1.5)
        }
        return round2(bf.coerceIn(MIN_BF, MAX_BF))
    }

    /** 由体重与体脂率估算去脂体重、肌肉量、水分、骨量（参考比例） */
    fun estimate(
        weightKg: Double,
        bodyFatPct: Double,
        sex: Int
    ): BodyEstimate {
        val fatMass = weightKg * bodyFatPct / 100.0
        val leanMass = weightKg - fatMass
        val muscleKg = round2(leanMass * if (sex == SEX_MALE) 0.78 else 0.73)
        val waterPct = round2((leanMass * 0.73 / weightKg) * 100.0)
        val boneKg = round2(weightKg * if (sex == SEX_MALE) 0.045 else 0.04)
        val visceral = round2(((bodyFatPct - 10).coerceAtLeast(1.0)) / 1.4)
        return BodyEstimate(
            fatMassKg = round2(fatMass),
            muscleKg = muscleKg,
            waterPct = waterPct,
            boneKg = boneKg,
            visceralFatLevel = visceral
        )
    }

    private fun round2(v: Double): Double = round(v * 10.0) / 10.0

    private const val MIN_BF = 4.0
    private const val MAX_BF = 60.0
}

data class BodyEstimate(
    val fatMassKg: Double,
    val muscleKg: Double,
    val waterPct: Double,
    val boneKg: Double,
    val visceralFatLevel: Double
)
