package com.fitbody.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity): Long

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM user ORDER BY id ASC")
    fun observeAll(): Flow<List<UserEntity>>
}

@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: DeviceEntity)

    @Query("SELECT * FROM device LIMIT 1")
    suspend fun getBound(): DeviceEntity?
}

@Dao
interface MeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: MeasurementEntity): Long

    @Query("SELECT * FROM measurement WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun observeLatest(userId: Long, limit: Int = 30): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurement WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(userId: Long): MeasurementEntity?

    @Query("SELECT * FROM measurement ORDER BY ABS(:weightKg - weightKg) ASC LIMIT 1")
    suspend fun closestByWeight(weightKg: Double): MeasurementEntity?
}

@Dao
interface BodyCompositionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(body: BodyCompositionEntity)

    @Query("SELECT * FROM body_composition WHERE measurementId = :id")
    suspend fun getByMeasurement(id: Long): BodyCompositionEntity?
}

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: FoodEntity): Long

    @Query("SELECT * FROM food WHERE name LIKE '%' || :keyword || '%' LIMIT 30")
    suspend fun search(keyword: String): List<FoodEntity>
}

@Dao
interface DietLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DietLogEntity): Long

    @Query("SELECT * FROM diet_log WHERE userId = :userId AND day = :day ORDER BY timestamp ASC")
    fun observeDay(userId: Long, day: Long): Flow<List<DietLogEntity>>

    @Query("SELECT COALESCE(SUM(kcal),0) FROM diet_log WHERE userId = :userId AND day = :day")
    fun observeDayKcal(userId: Long, day: Long): Flow<Double>
}

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity): Long

    @Query("SELECT * FROM workout WHERE userId = :userId AND day = :day ORDER BY timestamp ASC")
    fun observeDay(userId: Long, day: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT COALESCE(SUM(kcalBurned),0) FROM workout WHERE userId = :userId AND day = :day")
    fun observeDayKcal(userId: Long, day: Long): Flow<Double>
}

@Dao
interface PlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: PlanEntity): Long

    @Update
    suspend fun update(plan: PlanEntity)

    @Query("SELECT * FROM plan WHERE userId = :userId AND active = 1 LIMIT 1")
    suspend fun activePlan(userId: Long): PlanEntity?
}
