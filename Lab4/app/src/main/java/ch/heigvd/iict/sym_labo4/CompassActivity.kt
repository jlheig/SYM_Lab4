package ch.heigvd.iict.sym_labo4

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import ch.heigvd.iict.sym_labo4.gl.OpenGLRenderer

/**
 * Project: Labo4
 * Created by fabien.dutoit on 21.11.2016
 * Updated by fabien.dutoit on 06.11.2020
 * (C) 2016 - HEIG-VD, IICT
 */
class CompassActivity : AppCompatActivity(), SensorEventListener {

    //opengl
    private lateinit var opglr: OpenGLRenderer
    private lateinit var m3DView: GLSurfaceView

    //Sensors
    private lateinit var sManager: SensorManager
    private lateinit var sAccel: Sensor
    private lateinit var sMagnetic: Sensor

    //Datas
    private var dAccel = FloatArray(3)
    private var dMagnetic = FloatArray(3)
    private var dRotation = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // we need fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // we initiate the view
        setContentView(R.layout.activity_compass)

        //we create the renderer
        opglr = OpenGLRenderer(applicationContext)

        // link to GUI
        m3DView = findViewById(R.id.compass_opengl)

        //init opengl surface view
        m3DView.setRenderer(opglr)

        // Set sensors
        sManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sAccel = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sMagnetic = sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        //Register listeners
        sManager.registerListener(this, sAccel, SensorManager.SENSOR_DELAY_UI)
        sManager.registerListener(this, sMagnetic, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        //unregister listenener
        sManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> dAccel = event.values
            Sensor.TYPE_MAGNETIC_FIELD -> dMagnetic = event.values
        }

        SensorManager.getRotationMatrix(dRotation, null, dAccel, dMagnetic)

        dRotation = opglr.swapRotMatrix(dRotation)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}

