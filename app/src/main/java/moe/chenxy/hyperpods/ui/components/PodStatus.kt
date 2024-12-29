package moe.chenxy.hyperpods.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.chenxy.hyperpods.R
import moe.chenxy.hyperpods.pods.EarDetectionStatus
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.BatteryParams
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.EarDetectionParams
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.PodParams
import top.yukonga.miuix.kmp.basic.Box
import top.yukonga.miuix.kmp.basic.Text

fun lerp(start: Float, stop: Float, amount: Float): Float {
    return start + (stop - start) * amount
}

@Composable
fun BatteryIcon(batteryLevel: Int, isCharging: Boolean, isDarkMode: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(50.dp)
            .height(20.dp)
            .border(1.dp, if (isDarkMode) Color.White else Color.DarkGray, RoundedCornerShape(5.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
            // Battery level: changing color based on level
            val batteryLevelWidth = size.width * (batteryLevel / 100f)
            val batteryColor = when {
                isCharging -> Color(0xFF34C759) // Green (charging)
                batteryLevel > 30 -> if (isDarkMode) Color.LightGray else Color.Gray //  (normal)
                else -> Color(0xFFFF3B30) // Red (low battery)
            }

            drawRoundRect(
                color = batteryColor,
                size = size.copy(width = batteryLevelWidth, height = 16.dp.roundToPx().toFloat()),
                cornerRadius = CornerRadius(3.dp.roundToPx().toFloat(), 3.dp.roundToPx().toFloat()),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
        }

        Canvas(modifier = Modifier
            .align(Alignment.CenterEnd)) {
            // Battery cap (top part of the battery)
            drawRoundRect(
                color = if (isDarkMode) Color.White else Color.DarkGray,
                topLeft = androidx.compose.ui.geometry.Offset(x = 1f, y = -1.dp.roundToPx().toFloat()),
                cornerRadius = CornerRadius(2.dp.roundToPx().toFloat(), 2.dp.roundToPx().toFloat()),
                size = androidx.compose.ui.geometry.Size(2.dp.roundToPx().toFloat(), 4.dp.roundToPx().toFloat()),
            )
        }
    }
}

@SuppressLint("ResourceType")
@Composable
fun Battery(isCharging: Boolean, isDarkMode: Boolean, level: Int) {
    Row(modifier = Modifier.width(100.dp).height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        BatteryIcon(level, isCharging, isDarkMode)
        Text("$level %", fontSize = 12.sp)
    }

}

@Composable
fun Pod(batteryParams: BatteryParams, darkMode: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = modifier
            .heightIn(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Image(
                painter = painterResource(R.drawable.airpods_pro_left),
                contentDescription = "Left Pod",
                modifier = Modifier.weight(1f).padding(end = 10.dp),
                alignment = Alignment.CenterEnd
            )
            Image(
                painter = painterResource(R.drawable.airpods_pro_right),
                contentDescription = "Right Pod",
                modifier = Modifier.weight(1f).padding(end = 10.dp),
                alignment = Alignment.CenterStart
            )
        }

        AnimatedVisibility(batteryParams.left != null && batteryParams.left?.isConnected == true) {
            Row(
                modifier = Modifier.width(140.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.batt_left_pod), fontSize = 12.sp)
                Battery(batteryParams.left!!.isCharging, darkMode, batteryParams.left!!.battery)
            }
        }

        AnimatedVisibility(batteryParams.right != null && batteryParams.right?.isConnected == true) {
            Row(
                modifier = Modifier.width(140.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.batt_right_pod), fontSize = 12.sp)
                Battery(batteryParams.right!!.isCharging, darkMode, batteryParams.right!!.battery)
            }
        }
    }
}

@Composable
fun Case(podParams: PodParams?, darkMode: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = modifier
            .heightIn(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Image(
                painter = painterResource(R.drawable.airpods_pro_case),
                contentDescription = "Case",
                modifier = Modifier.padding(20.dp)
            )
        }
        podParams?.let {
            Row(modifier = Modifier.width(140.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.pod_case), fontSize = 12.sp)
                Battery(it.isCharging, darkMode, it.battery)
            }
        }
    }
}

@Composable
fun CaseWithPods(batteryParams: BatteryParams, darkMode: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = modifier
            .heightIn(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Image(
                painter = painterResource(R.drawable.airpods_pro),
                contentDescription = "Pods with Case",
            )
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.width(140.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Row(
                    modifier = Modifier.width(140.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.batt_left_pod), fontSize = 12.sp)
                    Battery(batteryParams.left!!.isCharging, darkMode, batteryParams.left!!.battery)
                }
                Row(
                    modifier = Modifier.width(140.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.batt_right_pod), fontSize = 12.sp)
                    Battery(
                        batteryParams.right!!.isCharging,
                        darkMode,
                        batteryParams.right!!.battery
                    )
                }
            }
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Row(
                    modifier = Modifier.width(140.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.pod_case), fontSize = 12.sp)
                    Battery(batteryParams.case!!.isCharging, darkMode, batteryParams.case!!.battery)
                }
            }
        }
    }
}

@Composable
fun PodStatus(batteryParams: BatteryParams, earDetectionParams: EarDetectionParams, modifier: Modifier = Modifier) {
    val currentDarkMode = isSystemInDarkTheme()
    val podAllExist = batteryParams.case?.isConnected == true
            && batteryParams.left?.isConnected == true
            && batteryParams.right?.isConnected == true
    val podsInCase = earDetectionParams.left == EarDetectionStatus.IN_CASE
            && earDetectionParams.right == EarDetectionStatus.IN_CASE
    val shouldShowPodWithCase = podsInCase && podAllExist

    Crossfade(shouldShowPodWithCase, label = "PodStatusSwitchAnim") { value ->
        if (!value) {
            Crossfade(batteryParams.case?.isConnected, label = "PodStatusSwitchAnim") { value ->
                if (value == true) {
                    Row(modifier = modifier
                        .fillMaxWidth()
                        .height(220.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly) {
                        Pod(batteryParams, currentDarkMode, modifier.weight(1f))
                        Case(batteryParams.case, currentDarkMode, modifier.weight(1f))
                    }
                } else {
                    Row(modifier = modifier
                        .fillMaxWidth()
                        .height(220.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly) {
                        Pod(batteryParams, currentDarkMode, modifier.weight(1f))
                    }
                }
            }
        } else {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(220.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CaseWithPods(batteryParams, currentDarkMode, modifier.weight(1f))
            }
        }
    }
}

@Composable
@Preview
fun PodStatusPreview() {
    PodStatus(BatteryParams(), EarDetectionParams(), modifier = Modifier.padding(12.dp))
}