package com.fitbody.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitbody.app.ble.ScaleBleManager
import com.fitbody.app.ble.ScaleConnectionState
import com.fitbody.app.ble.ScaleMeasurement
import com.fitbody.app.data.db.AppDatabase
import com.fitbody.app.data.db.SEX_MALE
import com.fitbody.app.data.db.UserEntity
import com.fitbody.app.data.repo.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 全局 ViewModel：管理默认用户、BLE 自动称重与结果落库。
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val repo = HealthRepository(app)
    private val ble = ScaleBleManager(app)

    private val _userId = MutableStateFlow<Long?>(null)
    val userId: StateFlow<Long?> = _userId

    val scaleState: StateFlow<ScaleConnectionState> = ble.state
    val liveMeasurement: StateFlow<ScaleMeasurement?> = ble.liveMeasurement

    private val _lastSaved = MutableStateFlow<ScaleMeasurement?>(null)
    val lastSaved: StateFlow<ScaleMeasurement?> = _lastSaved

    init {
        ble.onMeasurementComplete = { measurement ->
            val uid = _userId.value
            if (uid != null) {
                viewModelScope.launch {
                    repo.saveMeasurement(uid, measurement)
                    _lastSaved.value = measurement
                }
            }
        }
        viewModelScope.launch {
            var user = db.userDao().getById(DEFAULT_USER_ID)
            if (user == null) {
                val id = db.userDao().upsert(buildDefaultUser())
                user = db.userDao().getById(id)
            }
            _userId.value = user?.id
        }
    }

    /** 权限授予后调用，开始自动称重 */
    fun startScale() {
        ble.setBoundDevice(null)
        ble.start()
    }

    fun stopScale() = ble.stop()

    private fun buildDefaultUser(): UserEntity {
        val cal = Calendar.getInstance().apply { set(1995, 0, 1) }
        return UserEntity(
            id = DEFAULT_USER_ID,
            nickname = "我",
            sex = SEX_MALE,
            birthday = cal.timeInMillis,
            heightCm = 175.0,
            activityLevel = 1.375
        )
    }

    companion object {
        const val DEFAULT_USER_ID = 1L
    }
}
