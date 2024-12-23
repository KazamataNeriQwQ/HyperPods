package moe.chenxy.hyperpods.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.chenxy.hyperpods.pods.NoiseControlMode
import moe.chenxy.hyperpods.R
import top.yukonga.miuix.kmp.basic.Box
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun AncSwitch(ancStatus: NoiseControlMode, onAncModeChange: (NoiseControlMode) -> Unit) {
    val isDarkMode = isSystemInDarkTheme()
    val switchWidth = 95.dp
    val switchFullWidth = switchWidth * 4
    Box(Modifier.padding(start = 12.dp, end = 12.dp)
        .fillMaxWidth()
        .height(60.dp)
        .background(if (isDarkMode) Color.DarkGray else Color(0xFFE2E2E8) , RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center) {
        val offsetX = animateDpAsState(targetValue = when(ancStatus) {
                NoiseControlMode.OFF -> 0.dp
                NoiseControlMode.NOISE_CANCELLATION -> switchWidth
                NoiseControlMode.TRANSPARENCY -> switchWidth * 2
                NoiseControlMode.ADAPTIVE -> switchWidth * 3
            }, label = "AncSwitchAnimation")


        Box {
            Row(Modifier
                .width(switchFullWidth)
                .height(60.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = if (isDarkMode) Color.Gray else Color.White,
                    modifier = Modifier
                        .width(switchWidth)
                        .height(60.dp)
                        .padding(3.dp)
                        .offset(x = offsetX.value)
                        .shadow(10.dp, RoundedCornerShape(8.dp))) {}
            }
            Row(
                modifier = Modifier.width(switchFullWidth),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.width(switchWidth).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onAncModeChange(NoiseControlMode.OFF) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painterResource(R.drawable.noise_cancellation),
                        contentDescription = "ANC Off",
                        colorFilter = ColorFilter.tint(if (isDarkMode) Color.LightGray else Color.Gray)
                    )
                }
                Column(
                    modifier = Modifier.width(switchWidth).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onAncModeChange(NoiseControlMode.NOISE_CANCELLATION) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painterResource(R.drawable.noise_cancellation),
                        contentDescription = "ANC On",
                        colorFilter = ColorFilter.tint(if (isDarkMode) Color.White else Color.DarkGray)
                    )
                }
                Column(
                    modifier = Modifier.width(switchWidth).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onAncModeChange(NoiseControlMode.TRANSPARENCY) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painterResource(R.drawable.transparency),
                        contentDescription = "Transparency",
                        colorFilter = ColorFilter.tint(if (isDarkMode) Color.White else Color.DarkGray)
                    )
                }
                Column(
                    modifier = Modifier.width(switchWidth).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onAncModeChange(NoiseControlMode.ADAPTIVE) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painterResource(R.drawable.adaptive),
                        contentDescription = "Adaptive",
                        colorFilter = ColorFilter.tint(if (isDarkMode) Color.White else Color.DarkGray)
                    )
                }
            }
        }
    }

    Box(Modifier.padding(start = 12.dp, end = 12.dp, top = 5.dp, bottom = 5.dp)
        .fillMaxWidth(),
        contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.width(switchFullWidth),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.width(switchWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text("Off", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Column(
                modifier = Modifier.width(switchWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text("NC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Column(
                modifier = Modifier.width(switchWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text("Transparency", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Column(
                modifier = Modifier.width(switchWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text("Adaptive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}