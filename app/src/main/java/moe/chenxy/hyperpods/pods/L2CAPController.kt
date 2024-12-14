package moe.chenxy.hyperpods.pods

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.chenxy.hyperpods.utils.miuiStrongToast.MiuiStrongToastUtil
import moe.chenxy.hyperpods.utils.miuiStrongToast.MiuiStrongToastUtil.cancelPodsNotificationByMiuiBt
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.BatteryParams
import moe.chenxy.hyperpods.utils.miuiStrongToast.data.PodParams

@SuppressLint("MissingPermission", "StaticFieldLeak")
object L2CAPController {
    lateinit var socket: BluetoothSocket
    var mContext: Context? = null
    lateinit var mDevice: BluetoothDevice
    var mShowedConnectedToast = false

    @OptIn(ExperimentalStdlibApi::class)
    fun handleAirPodsPacket(packet: ByteArray) {
        if (AirPodsNotifications.EarDetection.isEarDetectionData(packet)) {
            AirPodsNotifications.EarDetection.setStatus(packet)
        } else if (AirPodsNotifications.ANC.isANCData(packet)) {
            AirPodsNotifications.ANC.setStatus(packet)
        } else if (AirPodsNotifications.BatteryNotification.isBatteryData(packet)) {
            AirPodsNotifications.BatteryNotification.setBattery(packet)
            val batteries = AirPodsNotifications.BatteryNotification.getBattery()
            var left = PodParams(batteries[0].level, batteries[0].status == BatteryStatus.CHARGING)
            var right = PodParams(batteries[1].level, batteries[1].status == BatteryStatus.CHARGING)
            var case = PodParams(batteries[2].level, batteries[2].status == BatteryStatus.CHARGING)

            Log.d("Art_Chen", "batt left ${left.battery} right ${right.battery} case ${case.battery}")
            if (!mShowedConnectedToast) {
                MiuiStrongToastUtil.showPodsBatteryToastByMiuiBt(mContext!!, BatteryParams(left, right, case))
                mShowedConnectedToast = true
            }
            MiuiStrongToastUtil.showPodsNotificationByMiuiBt(mContext!!, BatteryParams(left, right, case), mDevice)
        } else if (AirPodsNotifications.ConversationalAwarenessNotification.isConversationalAwarenessData(packet)) {
            AirPodsNotifications.ConversationalAwarenessNotification.setData(packet)
        } else {
            Log.v("Art_Chen", "Unknown AirPods Packet Received: ${packet.toHexString(HexFormat.UpperCase)}")
        }
    }

    fun connectPod(context: Context, device: BluetoothDevice) {
        mContext = context
        mDevice = device
        val uuid = ParcelUuid.fromString("74ec2172-0bad-4d01-8f77-997b2be0722a")

        CoroutineScope(Dispatchers.IO).launch {
            socket = BluetoothSocket::class.java.getDeclaredConstructor(IntType, BooleanType, BooleanType,
                BluetoothDevice::class.java, IntType,
                ParcelUuid::class.java).newInstance(3, true, true, device, 0x1001, uuid) as BluetoothSocket
            Log.d("Art_Chen", "connecting AirPods!")
            socket.connect()

            Log.d("Art_Chen", "connected!")
            socket.outputStream.write(Enums.HANDSHAKE.value)
            socket.outputStream.flush()
            socket.outputStream.write(Enums.SET_SPECIFIC_FEATURES.value)
            socket.outputStream.flush()
            socket.outputStream.write(Enums.REQUEST_NOTIFICATIONS.value)
            socket.outputStream.flush()
            while (socket.isConnected) {
                val buffer = ByteArray(1024)
                val bytesRead = socket.inputStream.read(buffer)
                Log.v("Art_Chen", "bytesRead $bytesRead!")
                if (bytesRead > 0) {
                    handleAirPodsPacket(buffer.copyOfRange(0, bytesRead))
                } else if (bytesRead == -1) {
                    // disconnected
                    socket.close()
                }
            }
        }
    }

    fun disconnectedPod(context: Context, device: BluetoothDevice) {
        socket.close()
        cancelPodsNotificationByMiuiBt(context, device)
        mContext = null
    }

    fun sendPacket(packet: String) {
        val fromHex = packet.split(" ").map { it.toInt(16).toByte() }
        socket.outputStream?.write(fromHex.toByteArray())
        socket.outputStream?.flush()
    }

    fun setANCMode(mode: Int) {
        Log.d("Art_Chen", "setANCMode: $mode")
        when (mode) {
            1 -> {
                socket.outputStream?.write(Enums.NOISE_CANCELLATION_OFF.value)
            }
            2 -> {
                socket.outputStream?.write(Enums.NOISE_CANCELLATION_ON.value)
            }
            3 -> {
                socket.outputStream?.write(Enums.NOISE_CANCELLATION_TRANSPARENCY.value)
            }
            4 -> {
                socket.outputStream?.write(Enums.NOISE_CANCELLATION_ADAPTIVE.value)
            }
        }
        socket.outputStream?.flush()
    }

    fun setCAEnabled(enabled: Boolean) {
        socket.outputStream?.write(if (enabled) Enums.SET_CONVERSATION_AWARENESS_ON.value else Enums.SET_CONVERSATION_AWARENESS_OFF.value)
        socket.outputStream?.flush()
    }

    fun setOffListeningMode(enabled: Boolean) {
        socket.outputStream?.write(byteArrayOf(0x04, 0x00 ,0x04, 0x00, 0x09, 0x00, 0x34, if (enabled) 0x01 else 0x02, 0x00, 0x00, 0x00))
        socket.outputStream?.flush()
    }

    fun setAdaptiveStrength(strength: Int) {
        val bytes = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x2E, strength.toByte(), 0x00, 0x00, 0x00)
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
    }

    fun setPressSpeed(speed: Int) {
        val bytes = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x17, speed.toByte(), 0x00, 0x00, 0x00)
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
    }

    fun setPressAndHoldDuration(speed: Int) {
        val bytes = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x18, speed.toByte(), 0x00, 0x00, 0x00)
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
    }

    fun setNoiseCancellationWithOnePod(enabled: Boolean) {
        val bytes = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x1B, if (enabled) 0x01 else 0x02, 0x00, 0x00, 0x00)
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
    }

    fun setVolumeControl(enabled: Boolean) {
        val bytes = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x25, if (enabled) 0x01 else 0x02, 0x00, 0x00, 0x00)
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
    }

    fun setVolumeSwipeSpeed(speed: Int) {
        val bytes = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x23, speed.toByte(), 0x00, 0x00, 0x00)
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
    }

    fun setToneVolume(volume: Int) {
        val bytes = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x1F, volume.toByte(), 0x50, 0x00, 0x00)
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
    }

    var earDetectionEnabled = true

    fun setCaseChargingSounds(enabled: Boolean) {
        val bytes = byteArrayOf(0x12, 0x3a, 0x00, 0x01, 0x00, 0x08, if (enabled) 0x00 else 0x01)
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
    }

    fun setEarDetection(enabled: Boolean) {
        earDetectionEnabled = enabled
    }

    fun disconnectAudio(context: Context, device: BluetoothDevice?) {
        val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter

        bluetoothAdapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.A2DP) {
                    try {
                        val method = proxy.javaClass.getMethod("disconnect", BluetoothDevice::class.java)
                        method.invoke(proxy, device)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, proxy)
                    }
                }
            }

            override fun onServiceDisconnected(profile: Int) { }
        }, BluetoothProfile.A2DP)

        bluetoothAdapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HEADSET) {
                    try {
                        val method = proxy.javaClass.getMethod("disconnect", BluetoothDevice::class.java)
                        method.invoke(proxy, device)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, proxy)
                    }
                }
            }

            override fun onServiceDisconnected(profile: Int) { }
        }, BluetoothProfile.HEADSET)
    }

    fun connectAudio(context: Context, device: BluetoothDevice?) {
        val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter

        bluetoothAdapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.A2DP) {
                    try {
                        val method = proxy.javaClass.getMethod("connect", BluetoothDevice::class.java)
                        method.invoke(proxy, device)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, proxy)
                    }
                }
            }

            override fun onServiceDisconnected(profile: Int) { }
        }, BluetoothProfile.A2DP)

        bluetoothAdapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HEADSET) {
                    try {
                        val method = proxy.javaClass.getMethod("connect", BluetoothDevice::class.java)
                        method.invoke(proxy, device)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, proxy)
                    }
                }
            }

            override fun onServiceDisconnected(profile: Int) { }
        }, BluetoothProfile.HEADSET)
    }

    fun setName(name: String) {
        val nameBytes = name.toByteArray()
        val bytes = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x1a, 0x00, 0x01,
            nameBytes.size.toByte(), 0x00) + nameBytes
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
        val hex = bytes.joinToString(" ") { "%02X".format(it) }
        Log.d("AirPodsService", "setName: $name, sent packet: $hex")
    }

    fun setPVEnabled(enabled: Boolean) {
        var hex = "04 00 04 00 09 00 26 ${if (enabled) "01" else "02"} 00 00 00"
        var bytes = hex.split(" ").map { it.toInt(16).toByte() }.toByteArray()
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
        hex = "04 00 04 00 17 00 00 00 10 00 12 00 08 E${if (enabled) "6" else "5"} 05 10 02 42 0B 08 50 10 02 1A 05 02 ${if (enabled) "32" else "00"} 00 00 00"
        bytes = hex.split(" ").map { it.toInt(16).toByte() }.toByteArray()
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
    }

    fun setLoudSoundReduction(enabled: Boolean) {
        val hex = "52 1B 00 0${if (enabled) "1" else "0"}"
        val bytes = hex.split(" ").map { it.toInt(16).toByte() }.toByteArray()
        socket.outputStream?.write(bytes)
        socket.outputStream?.flush()
    }
    fun findChangedIndex(oldArray: BooleanArray, newArray: BooleanArray): Int {
        for (i in oldArray.indices) {
            if (oldArray[i] != newArray[i]) {
                return i
            }
        }
        throw IllegalArgumentException("No element has changed")
    }
}