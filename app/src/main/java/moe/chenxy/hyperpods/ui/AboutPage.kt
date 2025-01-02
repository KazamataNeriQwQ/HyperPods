package moe.chenxy.hyperpods.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.LazyColumn
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.getWindowSize
import moe.chenxy.hyperpods.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.extra.SuperSwitch

@Composable
fun AboutPage(
    topAppBarScrollBehavior: ScrollBehavior,
    padding: PaddingValues
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.height(getWindowSize().height.dp).padding(12.dp),
        contentPadding = PaddingValues(top = padding.calculateTopPadding()),
        topAppBarScrollBehavior = topAppBarScrollBehavior
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = "HyperPods Logo", modifier = Modifier, colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onBackground))
                    Text(
                        text = "HyperPods",
                        modifier = Modifier,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "(AAP version)",
                        modifier = Modifier,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }

            Card(modifier = Modifier.padding(top = 12.dp)) {
                BasicComponent(
                    title = "Author",
                    summary = "Art_Chen",
                    enabled = false
                )
                BasicComponent(
                    title = "Github",
                    summary = "https://github.com/Art-Chen/HyperPods",
                    onClick = {
                        Intent(Intent.ACTION_VIEW).apply {
                            this.data = Uri.parse("https://github.com/Art-Chen/HyperPods")
                            context.startActivity(this)
                        }
                    },
                    enabled = true
                )
                BasicComponent(
                    title = stringResource(R.string.donate),
                    summary = stringResource(R.string.donate_detail),
                    onClick = {
                        Intent(Intent.ACTION_VIEW).apply {
                            this.data = Uri.parse("https://afdian.com/a/art_chen?tab=home")
                            context.startActivity(this)
                        }
                    },
                    enabled = true
                )
            }

        }
    }
}