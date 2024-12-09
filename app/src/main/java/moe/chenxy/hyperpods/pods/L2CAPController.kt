package moe.chenxy.hyperpods.pods

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import de.robv.android.xposed.XposedHelpers
import java.io.IOException

@SuppressLint("MissingPermission", "StaticFieldLeak")
object L2CAPController {
    lateinit var mContext: Context
    fun connectPod(device: BluetoothDevice) {
        val uuid = ParcelUuid.fromString("74ec2172-0bad-4d01-8f77-997b2be0722a")
//        val btSocketCls = Class.forName(BluetoothSocket::class.java.name, true, mContext.classLoader)
//        val socketV = btSocketCls.getDeclaredConstructor(IntType, BooleanType, BooleanType,
//            BluetoothDevice::class.java, IntType,
//            ParcelUuid::class.java).newInstance(3, true, true, device, 0x1001, uuid) as BluetoothSocket
        val socketU = BluetoothSocket::class.java.getDeclaredConstructor(IntType, IntType, BooleanType, BooleanType,
            BluetoothDevice::class.java, IntType,
            ParcelUuid::class.java).newInstance(3, -1, true, true, device, 0x1001, uuid) as BluetoothSocket
        Log.e("Art_Chen", "connecting!")
        socketU.connect()

        Log.e("Art_Chen", "connected!")
        val isConnected = socketU.isConnected
        Log.e("Art_Chen", "socketU $isConnected!")
    }
}