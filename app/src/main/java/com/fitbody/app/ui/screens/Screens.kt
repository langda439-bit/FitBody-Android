package com.fitbody.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitbody.app.ble.ScaleConnectionState
import com.fitbody.app.ble.ScaleMeasurement

@Composable
fun HomeScreen(
    scaleState: ScaleConnectionState,
    live: ScaleMeasurement?,
    lastSaved: ScaleMeasurement?,
    onStart: () -> Unit,
    onOpenCamera: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "自动称重",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        StatusCard(scaleState)
        Spacer(Modifier.height(20.dp))

        val display = live ?: lastSaved
        Text(
            text = display?.let { String.format("%.2f kg", it.weightKg) } ?: "--.-- kg",
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = when {
                live?.isStable == true -> "体重已稳定，正在记录…"
                scaleState == ScaleConnectionState.SCANNING -> "请赤脚踩上体脂秤"
                scaleState == ScaleConnectionState.CONNECTING -> "正在连接…"
                scaleState == ScaleConnectionState.MEASURING -> "正在测量，请保持站立"
                scaleState == ScaleConnectionState.UNAVAILABLE -> "请先开启蓝牙并授权"
                else -> "打开 App 后上秤即自动记录"
            },
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(28.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text("开始等待上秤")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenCamera, modifier = Modifier.fillMaxWidth()) {
            Text("拍照记录饮食")
        }
    }
}

@Composable
private fun StatusCard(state: ScaleConnectionState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .padding(0.dp)
            )
            Spacer(Modifier.size(12.dp))
            Column {
                Text("连接状态", fontWeight = FontWeight.Bold)
                Text(stateText(state))
            }
        }
    }
}

private fun stateText(s: ScaleConnectionState): String = when (s) {
    ScaleConnectionState.IDLE -> "空闲"
    ScaleConnectionState.UNAVAILABLE -> "蓝牙不可用/未授权"
    ScaleConnectionState.SCANNING -> "正在扫描体脂秤…"
    ScaleConnectionState.CONNECTING -> "正在连接…"
    ScaleConnectionState.CONNECTED -> "已连接"
    ScaleConnectionState.MEASURING -> "测量中…"
    ScaleConnectionState.DONE -> "记录完成"
    ScaleConnectionState.DISCONNECTED -> "已断开，准备重连"
}

@Composable
fun WeightScreen() {
    PlaceholderScreen(
        title = "体重趋势",
        body = "此处展示体重、体脂率、肌肉量的日/周/月/年趋势曲线与目标进度。"
    )
}

@Composable
fun DietScreen(onOpenCamera: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("热量管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("此处展示每日摄入/消耗热量环与三大营养素占比。")
        Spacer(Modifier.height(20.dp))
        Button(onClick = onOpenCamera) { Text("拍照识别食物热量") }
    }
}

@Composable
fun TrainingScreen() {
    PlaceholderScreen(
        title = "训练计划",
        body = "此处展示个性化训练日历、动作库、训练记录与完成度。"
    )
}

@Composable
fun ProfileScreen() {
    PlaceholderScreen(
        title = "我的",
        body = "此处管理个人资料、目标体重、设备绑定、数据导出与隐私设置。"
    )
}

@Composable
private fun PlaceholderScreen(title: String, body: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(body, modifier = Modifier.fillMaxWidth())
    }
}
