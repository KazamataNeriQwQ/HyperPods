package moe.chenxy.hyperpods.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.highcapable.yukihookapi.hook.factory.prefs
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import moe.chenxy.hyperpods.MainActivity
import moe.chenxy.hyperpods.R
import moe.chenxy.hyperpods.pods.NoiseControlMode
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.BatteryParams
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.EarDetectionParams
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.HyperPodsAction
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.HyperPodsPrefsKey
import top.yukonga.miuix.kmp.basic.HorizontalPager
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.Info
import top.yukonga.miuix.kmp.icon.icons.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(FlowPreview::class)
@Composable
fun MainUI() {
    val topAppBarScrollBehavior0 = MiuixScrollBehavior(rememberTopAppBarState())
    val topAppBarScrollBehavior1 = MiuixScrollBehavior(rememberTopAppBarState())

    val topAppBarScrollBehaviorList = listOf(
        topAppBarScrollBehavior0, topAppBarScrollBehavior1
    )

    val pagerState = rememberPagerState(pageCount = { 2 })
    var targetPage by remember { mutableIntStateOf(pagerState.currentPage) }
    val coroutineScope = rememberCoroutineScope()

    val currentScrollBehavior = when (pagerState.currentPage) {
        0 -> topAppBarScrollBehaviorList[0]
        else -> topAppBarScrollBehaviorList[1]
    }

    val mainTitle = remember { mutableStateOf("") }
    val aboutTitle = stringResource(R.string.about)
    val currentTitle = when (pagerState.currentPage) {
        0 -> mainTitle.value
        else -> aboutTitle
    }

    val items = listOf(
        NavigationItem(stringResource(R.string.pod_info), MiuixIcons.Settings),
        NavigationItem(stringResource(R.string.about), MiuixIcons.Info),
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.debounce(150).collectLatest {
            targetPage = pagerState.currentPage
        }
    }
    val context = LocalContext.current

    val earDetectionEnable = remember { mutableStateOf(context.prefs().getBoolean(HyperPodsPrefsKey.EAR_DETECTION, true)) }
    val earDetectionParams = remember { mutableStateOf(EarDetectionParams()) }
    val batteryParams = remember { mutableStateOf(BatteryParams()) }
    val autoSwitchToSpeaker = remember { mutableStateOf(context.prefs().getBoolean(HyperPodsPrefsKey.EAR_DETECTION_SWITCH_SPEAKER, true)) }
    val canShowDetailPage = remember { mutableStateOf(false) }
    val ancMode = remember { mutableStateOf(NoiseControlMode.OFF) }
    val init = remember { mutableStateOf(false) }
    var restoreAncJob: Job? = null

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (p1?.action) {
                HyperPodsAction.ACTION_PODS_ANC_CHANGED -> {
                    ancMode.value =
                        NoiseControlMode.entries.toTypedArray()[p1.getIntExtra("status", 1) - 1]
                    restoreAncJob?.cancel()
                }

                HyperPodsAction.ACTION_EAR_DETECTION_STATUS_CHANGED -> {
                    earDetectionParams.value =
                        p1.getParcelableExtra("status", EarDetectionParams::class.java)!!
                }

                HyperPodsAction.ACTION_PODS_BATTERY_CHANGED -> {
                    batteryParams.value = p1.getParcelableExtra("status", BatteryParams::class.java)!!
                }

                HyperPodsAction.ACTION_PODS_CONNECTED -> {
                    val deviceName = p1.getStringExtra("device_name")
                    mainTitle.value = deviceName ?: ""
                    canShowDetailPage.value = true
                    Log.i("Art_Chen", "pod connected deviceName: $deviceName")
                }

                HyperPodsAction.ACTION_PODS_DISCONNECTED -> {
                    mainTitle.value = ""
                    canShowDetailPage.value = false
                    if (p0 is MainActivity) {
                        p0.finish()
                    }
                }
            }
        }
    }

    if (!init.value) {
        context.registerReceiver(broadcastReceiver, IntentFilter().apply {
            this.addAction(HyperPodsAction.ACTION_PODS_ANC_CHANGED)
            this.addAction(HyperPodsAction.ACTION_EAR_DETECTION_STATUS_CHANGED)
            this.addAction(HyperPodsAction.ACTION_PODS_BATTERY_CHANGED)
            this.addAction(HyperPodsAction.ACTION_PODS_CONNECTED)
            this.addAction(HyperPodsAction.ACTION_PODS_DISCONNECTED)
        }, Context.RECEIVER_EXPORTED)

        context.sendBroadcast(Intent(HyperPodsAction.ACTION_PODS_UI_INIT))
        init.value = true
    }

    fun setAncMode(mode: NoiseControlMode) {
        if (restoreAncJob?.isActive == true) {
            return
        }
        Intent(HyperPodsAction.ACTION_ANC_SELECT).apply {
            this.putExtra("status", mode.ordinal + 1)
            context.sendBroadcast(this)
        }
        restoreAncJob = CoroutineScope(Dispatchers.Default).launch {
            val oldAncMode = ancMode.value
            ancMode.value = mode
            delay(1000)
            ancMode.value = oldAncMode
        }
    }

    fun setEarDetection(main: Boolean, disconnect: Boolean) {
        context.prefs().edit {
            putBoolean(HyperPodsPrefsKey.EAR_DETECTION, main)
            putBoolean(HyperPodsPrefsKey.EAR_DETECTION_SWITCH_SPEAKER, disconnect)
        }
        Intent(HyperPodsAction.ACTION_EAR_DETECTION_SWITCH_CHANGED).apply {
            this.putExtra("ear_detection", main)
            this.putExtra("disconnect_audio", disconnect)
            context.sendBroadcast(this)
        }
    }

    val hazeState = remember { HazeState() }
    val hazeStyle = HazeStyle(
        backgroundColor = if (currentScrollBehavior.state.heightOffset > -1) Color.Transparent else MiuixTheme.colorScheme.background,
        tint = HazeTint(
            MiuixTheme.colorScheme.background.copy(
                if (currentScrollBehavior.state.heightOffset > -1) 1f
                else lerp(1f, 0.67f, (currentScrollBehavior.state.heightOffset + 1) / -143f)
            )
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BoxWithConstraints {
                if (maxWidth > 840.dp) {
                    SmallTopAppBar(
                        color = Color.Transparent,
                        title = currentTitle,
                        modifier = Modifier
                            .hazeChild(
                                hazeState
                            ) {
                                style = hazeStyle
                                blurRadius = 25.dp
                                noiseFactor = 0f
                            },
                        scrollBehavior = currentScrollBehavior
                    )
                } else {
                    TopAppBar(
                        color = Color.Transparent,
                        title = currentTitle,
                        scrollBehavior = currentScrollBehavior,
                        modifier = Modifier
                            .hazeChild(
                                hazeState
                            ) {
                                style = hazeStyle
                                blurRadius = 25.dp
                                noiseFactor = 0f
                            }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                color = Color.Transparent,
                modifier = Modifier
                    .hazeChild(
                        hazeState
                    ) {
                        style = hazeStyle
                        blurRadius = 25.dp
                        noiseFactor = 0f
                    },
                items = items,
                selected = targetPage,
                onClick = { index ->
                    if (index in 0..2) {
                        targetPage = index
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                }
            )
        },
    ) { padding ->
        AppHorizontalPager(
            modifier = Modifier.imePadding().haze(state = hazeState),
            pagerState = pagerState,
            topAppBarScrollBehaviorList = topAppBarScrollBehaviorList,
            padding = padding,
            canShowDetailPage = canShowDetailPage.value,
            batteryParams = batteryParams.value,
            earDetectionParams = earDetectionParams.value,
            earDetectionEnable = earDetectionEnable.value,
            onEarDetectionChanged = {
                earDetectionEnable.value = it
                setEarDetection(it, it && autoSwitchToSpeaker.value)
            },
            autoSwitchToSpeaker = autoSwitchToSpeaker.value,
            onAutoSwitchToSpeakerChange = {
                autoSwitchToSpeaker.value = it
                setEarDetection(earDetectionEnable.value, earDetectionEnable.value && it)
            },
            ancMode = ancMode.value,
            onAncModeChange = {
                setAncMode(it)
            },
        )
    }
}

@Composable
fun AppHorizontalPager(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    topAppBarScrollBehaviorList: List<ScrollBehavior>,
    padding: PaddingValues,
    canShowDetailPage: Boolean,
    batteryParams: BatteryParams,
    earDetectionParams: EarDetectionParams,
    earDetectionEnable: Boolean,
    onEarDetectionChanged: (Boolean) -> Unit,
    autoSwitchToSpeaker: Boolean,
    onAutoSwitchToSpeakerChange: (Boolean) -> Unit,
    ancMode: NoiseControlMode,
    onAncModeChange: (NoiseControlMode) -> Unit
) {
    HorizontalPager(
        modifier = modifier,
        pagerState = pagerState,
        pageContent = { page ->
            when (page) {
                0 -> Crossfade(canShowDetailPage, label = "MainUIShowDetailAnim") { value ->
                        if (value) {
                            PodDetailPage(
                                topAppBarScrollBehavior = topAppBarScrollBehaviorList[0],
                                padding = padding,
                                batteryParams = batteryParams,
                                earDetectionParams = earDetectionParams,
                                earDetectionEnable = earDetectionEnable,
                                onEarDetectionChanged = onEarDetectionChanged,
                                autoSwitchToSpeaker = autoSwitchToSpeaker,
                                onAutoSwitchToSpeakerChange = onAutoSwitchToSpeakerChange,
                                ancMode = ancMode,
                                onAncModeChange = onAncModeChange
                            )
                        } else {
                            WaitingPodsPage()
                        }
                    }

                1 -> AboutPage(
                    topAppBarScrollBehavior = topAppBarScrollBehaviorList[1],
                    padding = padding
                )
            }
        }
    )
}

@Composable
@Preview
fun PodDetailPreview() {
    val ancMode = remember { mutableStateOf(NoiseControlMode.OFF) }
    val earDetectionEnable = remember { mutableStateOf(true) }
    val autoSwitchToSpeaker = remember { mutableStateOf(true) }
    val earDetectionParams = remember { mutableStateOf(EarDetectionParams()) }
    val batteryParams = remember { mutableStateOf(BatteryParams()) }
    AppTheme {
        Scaffold {
            PodDetailPage(
                topAppBarScrollBehavior = MiuixScrollBehavior(rememberTopAppBarState()),
                padding = PaddingValues(),
                earDetectionParams = earDetectionParams.value,
                batteryParams = batteryParams.value,
                earDetectionEnable = earDetectionEnable.value,
                onEarDetectionChanged = { earDetectionEnable.value = it },
                autoSwitchToSpeaker = autoSwitchToSpeaker.value,
                onAutoSwitchToSpeakerChange = { autoSwitchToSpeaker.value = it },
                ancMode = ancMode.value,
                onAncModeChange = { ancMode.value = it },
            )
        }
    }
}

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }


@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

@Composable
fun Float.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }