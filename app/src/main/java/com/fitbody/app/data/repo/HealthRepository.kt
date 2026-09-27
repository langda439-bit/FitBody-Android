package com.fitbody.app.data.repo

import android.content.Context
import com.fitbody.app.ble.ScaleMeasurement
import com.fitbody.app.data.db.AppDatabase
import com.fitbody.app.data.db.BodyCompositionEntity
import com.fitbody.app.data.db.MeasurementEntity
import com.fitbody.app.domain.BodyCompositionCalculator
import com.fitbody.app.domain.CalorieCalculator
import java.util.Calendar

/**
 * 健康数据仓库：统一处理称重结果落库与体成分计算。
 */
class HealthRepository(context: Context) {

    private val db = AppDatabase.get(context)

    /**
     * 保存一次称重：写入测量记录，并计算/写入体成分。
     * @return 测量记录主键
     */
    suspend fun saveMeasurement(userId: Long, m: ScaleMeasurement): Long {
        val user = db.userDao().getById(userId) ?: error("user not found")
        val age = ageYears(user.birthday)

        val measurementId = db.measurementDao().insert(
            MeasurementEntity(
                userId = userId,
                deviceMac = null,
                timestamp = m.timestamp,
                weightKg = m.weightKg,
                impedanceOhm = m.impedanceOhm,
                isStable = m.isStable,
                source = "ble"
            )
        )

        val bmi = BodyCompositionCalculator.bmi(m.weightKg, user.heightCm)
        val bodyFat = if (m.impedanceOhm != null) {
            BodyCompositionCalculator.bodyFatPct(user.sex, age, bmi, m.impedanceOhm)
        } else null
        val estimate = bodyFat?.let {
            BodyCompositionCalculator.estimate(m.weightKg, it, user.sex)
        }
        val bmr = CalorieCalculator.bmr(user.sex, m.weightKg, user.heightCm, age)

        db.bodyCompositionDao().insert(
            BodyCompositionEntity(
                measurementId = measurementId,
                bmi = bmi,
                bodyFatPct = bodyFat,
                muscleKg = estimate?.muscleKg,
                waterPct = estimate?.waterPct,
                boneKg = estimate?.boneKg,
                visceralFatLevel = estimate?.visceralFatLevel,
                bmr = bmr
            )
        )
        return measurementId
    }

    private fun ageYears(birthday: Long): Int {
        val birth = Calendar.getInstance().apply { timeInMillis = birthday }
        val now = Calendar.getInstance()
        var age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--
        return age.coerceAtLeast(0)
    }
}
