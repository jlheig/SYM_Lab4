package ch.heigvd.iict.sym_labo4.viewmodels

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Year
import java.util.*

/**
 * Project: Labo4
 * Created by fabien.dutoit on 11.05.2019
 * Updated by fabien.dutoit on 18.10.2021
 * (C) 2019 - HEIG-VD, IICT
 */
class BleOperationsViewModel(application: Application) : AndroidViewModel(application) {

    private var ble = SYMBleManager(application.applicationContext)
    private var mConnection: BluetoothGatt? = null

    //live data - observer
    val isConnected = MutableLiveData(false)

    val currentTime = MutableLiveData(Calendar.getInstance())
    val btnClick = MutableLiveData(0)
    val temperature = MutableLiveData(0.0)

    //Services and Characteristics of the SYM Pixl
    private var timeService: BluetoothGattService? = null
    private var symService: BluetoothGattService? = null
    private var currentTimeChar: BluetoothGattCharacteristic? = null
    private var integerChar: BluetoothGattCharacteristic? = null
    private var temperatureChar: BluetoothGattCharacteristic? = null
    private var buttonClickChar: BluetoothGattCharacteristic? = null

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
        ble.disconnect()
    }

    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "User request connection to: $device")
        if (!isConnected.value!!) {
            ble.connect(device)
                    .retry(1, 100)
                    .useAutoConnect(false)
                    .enqueue()
        }
    }

    fun disconnect() {
        Log.d(TAG, "User request disconnection")
        ble.disconnect()
        mConnection?.disconnect()
    }

    /* TODO
        vous pouvez placer ici les différentes méthodes permettant à l'utilisateur
        d'interagir avec le périphérique depuis l'activité
     */

    fun readTemperature(): Boolean {
        if (!isConnected.value!! || temperatureChar == null)
            return false
        else
            return ble.readTemperature()
    }

    fun sendInteger(n : Int) : Boolean {
        if (!isConnected.value!! || integerChar == null)
            return false
        else
            return ble.sendInteger(n)
    }

    fun sendCurrentTime(currentTime : Calendar) : Boolean {
        if (!isConnected.value!! || currentTimeChar == null)
            return false
        else
            return ble.sendCurrentTime(currentTime)
    }

    private val bleConnectionObserver: ConnectionObserver = object : ConnectionObserver {
        override fun onDeviceConnecting(device: BluetoothDevice) {
            Log.d(TAG, "onDeviceConnecting")
            isConnected.value = false
        }

        override fun onDeviceConnected(device: BluetoothDevice) {
            Log.d(TAG, "onDeviceConnected")
            isConnected.value = true
        }

        override fun onDeviceDisconnecting(device: BluetoothDevice) {
            Log.d(TAG, "onDeviceDisconnecting")
            isConnected.value = false
        }

        override fun onDeviceReady(device: BluetoothDevice) {
            Log.d(TAG, "onDeviceReady")
        }

        override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
            Log.d(TAG, "onDeviceFailedToConnect")
        }

        override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
            if(reason == ConnectionObserver.REASON_NOT_SUPPORTED) {
                Log.d(TAG, "onDeviceDisconnected - not supported")
                Toast.makeText(getApplication(), "Device not supported - implement method isRequiredServiceSupported()", Toast.LENGTH_LONG).show()
            }
            else
                Log.d(TAG, "onDeviceDisconnected")
            isConnected.value = false
        }

    }

    private inner class SYMBleManager(applicationContext: Context) : BleManager(applicationContext) {
        /**
         * BluetoothGatt callbacks object.
         */
        private var mGattCallback: BleManagerGattCallback? = null

        public override fun getGattCallback(): BleManagerGattCallback {
            //we initiate the mGattCallback on first call, singleton
            if (mGattCallback == null) {
                mGattCallback = object : BleManagerGattCallback() {

                    public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
                        mConnection = gatt //trick to force disconnection


                        /* TODO
                        - Nous devons vérifier ici que le périphérique auquel on vient de se connecter possède
                          bien tous les services et les caractéristiques attendues, on vérifiera aussi que les
                          caractéristiques présentent bien les opérations attendues
                        - On en profitera aussi pour garder les références vers les différents services et
                          caractéristiques (déclarés en lignes 39 à 44)
                        */
                        val timeServiceUuid = "00001805-0000-1000-8000-00805f9b34fb"
                        val currentTimeCharUuid = "00002A2B-0000-1000-8000-00805f9b34fb"
                        val symServiceUuid = "3c0a1000-281d-4b48-b2a7-f15579a1c38f"
                        val integerCharUuid = "3c0a1001-281d-4b48-b2a7-f15579a1c38f"
                        val temperatureCharUuid = "3c0a1002-281d-4b48-b2a7-f15579a1c38f"
                        val buttonClickCharUuid = "3c0a1003-281d-4b48-b2a7-f15579a1c38f"


                        val uuidMap = mapOf(
                                timeServiceUuid to listOf(currentTimeCharUuid),
                                symServiceUuid to listOf(
                                        integerCharUuid,
                                        temperatureCharUuid,
                                        buttonClickCharUuid
                                )
                        )

                        for (elem in uuidMap) {
                            val service = gatt.getService(UUID.fromString(elem.key)) ?: return false

                            for (charUuid in elem.value) {
                                val char = service.getCharacteristic(UUID.fromString(charUuid)) ?: return false
                                when (charUuid) {
                                    currentTimeCharUuid -> currentTimeChar = char
                                    integerCharUuid -> integerChar = char
                                    temperatureCharUuid -> temperatureChar = char
                                    buttonClickCharUuid -> buttonClickChar = char
                                }
                            }
                            when (elem.key) {
                                timeServiceUuid -> timeService = service
                                symServiceUuid -> symService = service
                            }
                        }

                        return true //FIXME si tout est OK, on retourne true, sinon la librairie appelera la méthode onDeviceDisconnected() avec le flag REASON_NOT_SUPPORTED
                    }

                    override fun initialize() {
                        /*  TODO
                            Ici nous somme sûr que le périphérique possède bien tous les services et caractéristiques
                            attendus et que nous y sommes connectés. Nous pouvous effectuer les premiers échanges BLE:
                            Dans notre cas il s'agit de s'enregistrer pour recevoir les notifications proposées par certaines
                            caractéristiques, on en profitera aussi pour mettre en place les callbacks correspondants.
                         */

                        enableNotifications(currentTimeChar).enqueue()

                        setNotificationCallback(currentTimeChar).with{ _ , data : Data ->
                            val year = data.getIntValue(Data.FORMAT_UINT16, 0)
                            var month = data.getIntValue(Data.FORMAT_UINT8, 2)
                            val dayOfMonth = data.getIntValue(Data.FORMAT_UINT8, 3)
                            val hour = data.getIntValue(Data.FORMAT_UINT8, 4)
                            val minute = data.getIntValue(Data.FORMAT_UINT8, 5)
                            val second = data.getIntValue(Data.FORMAT_UINT8, 6)



                            if (year != null && month != null && dayOfMonth != null && hour != null && minute != null && second != null) {
                                var date = Calendar.getInstance()
                                month -= 1
                                date.set(year,month,dayOfMonth,hour, minute,second)
                                currentTime.postValue(date)
                            }

                        }

                        enableNotifications(buttonClickChar).enqueue()

                        setNotificationCallback(buttonClickChar).with{ _, data : Data ->
                            var click = data.getIntValue(Data.FORMAT_UINT8, 0)
                            btnClick.postValue(click)
                        }

                    }

                    override fun onServicesInvalidated() {
                        //we reset services and characteristics
                        timeService = null
                        currentTimeChar = null
                        symService = null
                        integerChar = null
                        temperatureChar = null
                        buttonClickChar = null
                    }
                }
            }
            return mGattCallback!!
        }

        fun readTemperature(): Boolean {
            /*  TODO
                on peut effectuer ici la lecture de la caractéristique température
                la valeur récupérée sera envoyée à l'activité en utilisant le mécanisme
                des MutableLiveData
                On placera des méthodes similaires pour les autres opérations
            */
            if (temperatureChar == null) return false

            readCharacteristic(temperatureChar).with{ _, data : Data ->
                val temp = data.getIntValue(Data.FORMAT_UINT16, 0)?.div(10.0)
                temperature.postValue(temp)
            }.enqueue()

            return true
        }

        fun sendInteger(n : Int) : Boolean {
            writeCharacteristic(integerChar, byteArrayOf(n.toByte()),WRITE_TYPE_DEFAULT).enqueue()
            return true
        }

        fun sendCurrentTime(currentTime : Calendar) : Boolean {
            val ct = ByteArray(10)

            val zero = 0

            val year = currentTime.get(Calendar.YEAR)
            val byteYear = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(year).array()

            ct[0] = byteYear[0]
            ct[1] = byteYear[1]
            ct[2] = (currentTime.get(Calendar.MONTH ) + 1 ).toByte()
            ct[3] = currentTime.get(Calendar.DAY_OF_MONTH).toByte()
            ct[4] = currentTime.get(Calendar.HOUR).toByte()
            ct[5] = currentTime.get(Calendar.MINUTE).toByte()
            ct[6] = currentTime.get(Calendar.SECOND).toByte()
            ct[7] = currentTime.get(Calendar.DAY_OF_WEEK).toByte()
            ct[8] = currentTime.get(Calendar.MILLISECOND).toByte()
            ct[9] = zero.toByte()

            writeCharacteristic(currentTimeChar, ct,WRITE_TYPE_DEFAULT).enqueue()
            return true
        }
    }

    companion object {
        private val TAG = BleOperationsViewModel::class.java.simpleName
    }

    init {
        ble.setConnectionObserver(bleConnectionObserver)
    }

}