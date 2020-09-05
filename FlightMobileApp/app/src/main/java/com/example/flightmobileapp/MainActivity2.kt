package com.example.flightmobileapp

import Api
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.ramotion.fluidslider.FluidSlider
import io.github.controlwear.virtual.joystick.android.JoystickView
import io.github.controlwear.virtual.joystick.android.JoystickView.OnMoveListener
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.coroutines.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.cos
import kotlin.math.sin

class MainActivity2 : AppCompatActivity() {

    var finish:Boolean = true
    var currentURL: String = ""
    var stop:Boolean = false;
    var isfirst:Boolean= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        //get the last url
        Thread {
            var db: AppDataBase = AppDataBase.getInstance(this)
            currentURL = db.urlDAO().getLastURL()
            //send to post
        }.start()


        GlobalScope.launch {
            while (true) {
                delay(1500)
                CoroutineScope(Dispatchers.IO).launch { getImage(currentURL) }
            }
        }

        //all vars to change
        if (isfirst == false) {
            //set the screenshot of the flight
            var imageView: ImageView = findViewById(R.id.imageViewNew)
            var data = intent.getByteArrayExtra("FlightImage")
            val v = data?.size?.let { BitmapFactory.decodeByteArray(data, 0, it) }
            runOnUiThread { imageView.setImageBitmap(v) }
            isfirst = true;
        }

        var rudder: Double = 0.0
        var elivator: Double = 0.0
        var aileron: Double = 0.0
        var throttle: Double = 0.0


        //JOYSTICK.
        //define new Joystick for the app
        val joy: JoystickView = findViewById(R.id.joystick)
        joy.setOnMoveListener(OnMoveListener { angle, strength ->
            //set values
            var angle_rad = Math.toRadians(angle.toDouble())
            var base_radius: Double = ((240 * 80) / 200).toDouble()
            var joy_radius: Double = (strength * base_radius) / 100
            //calcolate
            val y: Double = joy_radius * sin(angle_rad)
            val x: Double = joy_radius * cos(angle_rad)
            //normlize the value between 0 to 1
            val normalize_y: Double = y / base_radius
            val normalize_x: Double = x / base_radius
            aileron = normalize_x
            elivator = normalize_y


            CoroutineScope(Dispatchers.IO).launch {
                sendPost(
                    aileron,
                    rudder,
                    elivator,
                    throttle,
                    currentURL
                )
            }
        })


        //SLIDERS.
        //values for the sliders.
        val max = 1
        val min = 0
        val maxRudder = 1
        val minRudder = -1
        val total = max - min
        val totalRudder = maxRudder - minRudder

        var new_rudder: Double = 0.0

        //the first slider
        val slider = findViewById<FluidSlider>(R.id.fluidSlider)
        slider.positionListener = { p ->
            Log.d("MYTAG", "current position is: $p")
        }
        slider.positionListener = { pos ->
            slider.bubbleText =
                "${String.format("%.3f", min + (total * pos).toDouble()).toDouble()}"
            //change the value of the slider
            new_rudder = min + (total * pos).toDouble()
        }

        slider.position = 0.3f
        slider.startText = "$min"
        slider.endText = "$max"
        slider.setOnClickListener {
            if ((new_rudder - rudder) >= 0.01) {
                rudder = new_rudder
                CoroutineScope(Dispatchers.IO).launch {
                    sendPost(
                        aileron,
                        rudder,
                        elivator,
                        throttle,
                        currentURL
                    )
                }
            }
        }

        var new_throttle: Double = 0.0
        //the second slider
        val slider2 = findViewById<FluidSlider>(R.id.fluidSlider2)
        slider2.positionListener = { p -> Log.d("MainActivity", "current position is: $p") }
        slider2.positionListener = { pos ->
            slider2.bubbleText =
                "${String.format("%.3f", min + (totalRudder * pos).toDouble()).toDouble()}"
            //change the value of the slider
            new_throttle = min + (totalRudder * pos).toDouble()
        }
        slider2.position = 0.3f
        slider2.startText = "$minRudder"
        slider2.endText = "$maxRudder"


        slider2.setOnClickListener {
            if ((new_throttle - throttle) >= 0.02) {
                throttle = new_throttle
                CoroutineScope(Dispatchers.IO).launch {
                    sendPost(
                        aileron,
                        rudder,
                        elivator,
                        throttle,
                        currentURL
                    )
                }
            }
        }


        //the return button
        val ret: Button = findViewById(R.id.Back)
        ret.setOnClickListener {
            //disconnect from server.
            !finish
            stop = true
            //return to first activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
    }

    //the function that send the value to the server.
    fun sendPost(
        aileronS: Double,
        rudderS: Double,
        elevatorS: Double,
        throttelS: Double,
        urlOne: String
    ) {
        val url = URL("http://10.0.2.2:" + urlOne)
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        val json: String =
            "{\"aileron\":" + aileronS + ",\n \"rudder\":" + rudderS + ",\n\"elevator\":" + elevatorS + ",\n\"throttle\":" + throttelS + "\n}"

        val rb: RequestBody = RequestBody.create(MediaType.parse("application/json"), json)
        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder().baseUrl(url.toString()).addConverterFactory(
            GsonConverterFactory.create(gson)
        ).build()

        val api = retrofit.create(Api::class.java)
        api.post(rb).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {


            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "error sending post",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }


    fun getImage(url: String) {
        // while (finish) {
        //build the json
        val gson = GsonBuilder().setLenient().create()
        var retrofit: Retrofit
        var api: Api
        try {
            //bulid the retrofit
            val url = "http://10.0.2.2:" + url
            retrofit = Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
            //create the retrofit
            api = retrofit.create(Api::class.java)
            //return api
        } catch (ex: java.lang.Exception) {
            Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show()
            return
        }

        api.getImg().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                if (!response.isSuccessful()) {
                    Toast.makeText(applicationContext, "error 400", Toast.LENGTH_LONG).show()

                } else {
                    val I = response.body()?.byteStream()
                    val C = BitmapFactory.decodeStream(I)
                    runOnUiThread { imageViewNew.setImageBitmap(C) }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "error in getting pictrue",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
        })
    }
}