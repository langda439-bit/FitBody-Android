package com.fitbody.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        DeviceEntity::class,
        MeasurementEntity::class,
        BodyCompositionEntity::class,
        FoodEntity::class,
        DietLogEntity::class,
        WorkoutEntity::class,
        PlanEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deviceDao(): DeviceDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun bodyCompositionDao(): BodyCompositionDao
    abstract fun foodDao(): FoodDao
    abstract fun dietLogDao(): DietLogDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun planDao(): PlanDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitbody.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
