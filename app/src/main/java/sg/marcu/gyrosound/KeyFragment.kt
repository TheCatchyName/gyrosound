package sg.marcu.gyrosound

import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.hardware.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.util.Half.EPSILON
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.properties.Delegates

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var soundPool: SoundPool
private var streamId = 0
private lateinit var sensorManager: SensorManager
private lateinit var gyroscope: Sensor
private lateinit var accelerometer: Sensor
private lateinit var magnetometer: Sensor
private var NS2S = 1.0f / 1000000000.0f
private var deltaRotationVector = FloatArray(4)
private var timestamp by Delegates.notNull<Float>()
private var rotationMatrix = FloatArray(9)
private var iMatrix = FloatArray(9)
private var orientation = FloatArray(3)
private var mGeomagnetic = FloatArray(9)
private var mGravity = FloatArray(9)
private var roll = 0.0f

/**
 * A simple [Fragment] subclass.
 * Use the [KeyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class KeyFragment : Fragment(), View.OnTouchListener, SensorEventListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_key, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val buttonPlay = requireActivity().findViewById<Button>(R.id.buttonPlay)
        soundPool = SoundPool(6, AudioManager.STREAM_MUSIC, 0)
        soundPool!!.load(activity?.applicationContext, R.raw.key01, 1)
        buttonPlay.setOnTouchListener(this)
        sensorManager = activity?.getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, gyroscope,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometer,SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)

    }

    override fun onSensorChanged(event: SensorEvent?) {
        val x = event!!.values[0]
        val y = event!!.values[1]
        val z = event!!.values[2]

        val accel = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        activity?.findViewById<TextView>(R.id.gyroscopeDisplay)?.text = "X: $x"
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values
        }

        if (mGravity != null && mGeomagnetic != null) {

            val success = SensorManager.getRotationMatrix(rotationMatrix, iMatrix, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation);
                val azimuth = orientation[0]
                val pitch = orientation[1]
                roll = orientation[2]
                activity?.findViewById<TextView>(R.id.azimuthDisplay)?.text = "azimuth: $azimuth"
                activity?.findViewById<TextView>(R.id.azimuthDisplay)?.text = "pitch: $pitch"
                activity?.findViewById<TextView>(R.id.azimuthDisplay)?.text = "roll: $roll"
                if (streamId > 0) {
                    soundPool.setRate(streamId, abs(roll))
                }
            }
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        var soundId = 1
        if (event.getAction()== MotionEvent.ACTION_DOWN) {
            streamId = soundPool.play(soundId, 1F, 1F, 0, -1, abs(roll))
        }
        if(event.getAction()==MotionEvent.ACTION_UP){
            soundPool.stop(streamId)
        }
        return true
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment KeyFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            KeyFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}