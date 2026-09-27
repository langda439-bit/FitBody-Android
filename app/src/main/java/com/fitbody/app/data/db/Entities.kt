package com.fitbody.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 性别 */
const val SEX_MALE = 1
const val SEX_FEMALE = 0

/** 体重目标类型 */
const val GOAL_LOSE = "lose"
const val GOAL_MAINTAIN = "maintain"
const val GOAL_GAIN = "gain"

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nickname: String,
    val sex: Int,
    val birthday: Long,
    val heightCm: Double,
    val activityLevel: Double = 1.2,
    val goalType: String = GOAL_LOSE,
    val targetWeightKg: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "device")
data class DeviceEntity(
    @PrimaryKey val macAddress: String,
    val name: String?,
    val model: String,
    val itemNo: String,
    val firmwareVersion: String? = null,
    val batteryLevel: Int? = null,
    val boundAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "measurement",
    indices = [Index("userId"), Index("timestamp")]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val deviceMac: String?,
    val timestamp: Long,
    val weightKg: Double,
    val impedanceOhm: Int?,
    val isStable: Boolean,
    val source: String = "ble"
)

@Entity(tableName = "body_composition")
data class BodyCompositionEntity(
    @PrimaryKey val measurementId: Long,
    val bmi: Double,
    val bodyFatPct: Double?,
    val muscleKg: Double?,
    val waterPct: Double?,
    val boneKg: Double?,
    val visceralFatLevel: Double?,
    val bmr: Double?
)

@Entity(tableName = "food")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** 每 100g 热量 kcal */
    val kcalPer100g: Double,
    val proteinPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val custom: Boolean = false
)

const val MEAL_BREAKFAST = 0
const val MEAL_LUNCH = 1
const val MEAL_DINNER = 2
const val MEAL_SNACK = 3

@Entity(
    tableName = "diet_log",
    indices = [Index("userId"), Index("day")]
)
data class DietLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val day: Long,
    val meal: Int,
    val foodName: String,
    val grams: Double,
    val kcal: Double,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val confidence: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "workout",
    indices = [Index("userId"), Index("day")]
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val planId: Long?,
    val title: String,
    val durationMin: Int,
    val kcalBurned: Double,
    val day: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "plan")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val goalType: String,
    val weeks: Int,
    val daysPerWeek: Int,
    /** 周安排（JSON 文本，由计划生成器写入） */
    val scheduleJson: String,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
