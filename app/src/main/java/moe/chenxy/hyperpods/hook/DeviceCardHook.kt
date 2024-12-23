package moe.chenxy.hyperpods.hook

import android.util.Log
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import de.robv.android.xposed.XposedHelpers

object DeviceCardHook : YukiBaseHooker() {
    override fun onHook() {
        "miui.systemui.devicecenter.devices.DeviceInfoWrapper".toClass().apply {
            method {
                name = "performClicked"
                param(ContextClass)
            }.hook {
                before {
                    val deviceInfo = XposedHelpers.callMethod(this.instance, "getDeviceInfo")
                    val id = XposedHelpers.callMethod(deviceInfo, "getId")
                    Log.i("Art_Chen", "performClicked $id")
//                    this.result = null
                }
            }
        }
    }
}