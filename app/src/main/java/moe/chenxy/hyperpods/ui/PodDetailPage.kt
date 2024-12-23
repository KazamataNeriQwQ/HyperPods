package moe.chenxy.hyperpods.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.chenxy.hyperpods.pods.NoiseControlMode
import moe.chenxy.hyperpods.ui.components.AncSwitch
import moe.chenxy.hyperpods.ui.components.PodStatus
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.BatteryParams
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.EarDetectionParams
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.LazyColumn
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.utils.getWindowSize

@Composable
fun PodDetailPage(
    topAppBarScrollBehavior: ScrollBehavior,
    padding: PaddingValues,
    batteryParams: BatteryParams,
    earDetectionParams: EarDetectionParams,
    earDetectionEnable: Boolean,
    onEarDetectionChanged: (Boolean) -> Unit,
    autoSwitchToSpeaker: Boolean,
    onAutoSwitchToSpeakerChange: (Boolean) -> Unit,
    ancMode: NoiseControlMode,
    onAncModeChange: (NoiseControlMode) -> Unit
) {
    LazyColumn(
        modifier = Modifier.height(getWindowSize().height.dp),
        contentPadding = PaddingValues(top = padding.calculateTopPadding()),
        topAppBarScrollBehavior = topAppBarScrollBehavior
    ) {
        item {
            Card(
                modifier = Modifier
                    .padding(12.dp)
            ) {
                PodStatus(batteryParams, earDetectionParams, modifier = Modifier.padding(12.dp))
            }

            AncSwitch(ancMode, onAncModeChange)

            Card(
                modifier = Modifier
                    .padding(12.dp)
            ) {
                SuperSwitch(
                    title = "Ear Detection",
                    summary = "TODO",
                    checked = earDetectionEnable,
                    onCheckedChange = onEarDetectionChanged
                )
                AnimatedVisibility(
                    visible = earDetectionEnable,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SuperSwitch(
                        title = "Auto Switch to Speaker",
                        summary = "Switch to Speaker when both pod are out of ear",
                        checked = autoSwitchToSpeaker,
                        onCheckedChange = onAutoSwitchToSpeakerChange,
                    )
                }
            }
        }
    }
}