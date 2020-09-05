package com.example.flightmobileapp;

import Api
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    var islocalhost:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {

        //set the global variable
        islocalhost=false
        super.onCreate(savedInstanceState)
        val dao = AppDataBase.getInstance(application).urlDAO()

        setContentView(R.layout.activity_main)
        //call initialize
        CoroutineScope(Dispatchers.IO).launch {initialize()  }
    }
    fun initialize(){
        //set all five buttons of the urls.
        val buttonurl_1:Button=findViewById(R.id.UrlOne)
        buttonurl_1.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {localHostButton(1)  }
        }
        val buttonurl_2:Button=findViewById(R.id.UrlTwo)
        buttonurl_2.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {localHostButton(2)  }
        }
        val buttonurl_3:Button=findViewById(R.id.UrlThree)
        buttonurl_3.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {localHostButton(3)  }
        }
        val buttonurl_4:Button=findViewById(R.id.UrlFour)
        buttonurl_4.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {localHostButton(4)  }
        }
        val buttonurl_5:Button=findViewById(R.id.UrlFive)
        buttonurl_5.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {localHostButton(5)  }
        }

        //create a button object for connect-
        val buttonConnect: Button = findViewById(R.id.button)
        buttonConnect.setOnClickListener {

            val intent = Intent(this, MainActivity2::class.java)
            //Save the URL We Inserted To A Variable
            if (islocalhost==false){
                var input = TypeUrl.text
                //save the url address as the last used addresed
                CoroutineScope(Dispatchers.IO).launch {insrt_room(input.toString())  }
                //try to connect
                connect(input.toString())
            }
            else{
                val URL: EditText =findViewById(R.id.TypeUrl)
                //save the url address as the last used addresed
                CoroutineScope(Dispatchers.IO).launch {update_room(URL.hint.toString())  }
                //try to connect
                connect(URL.hint.toString())
            }
            startActivity(intent)
        }
    }

    fun localHostButton(num:Int){
        val intent = Intent(this, MainActivity2::class.java)
        var db:AppDataBase=AppDataBase.getInstance(this)
        var x:List<String> = db.urlDAO().getRecentUrl()
        islocalhost=true
        if (x.size>num-1){
            val URL:EditText=findViewById(R.id.TypeUrl)
            URL.hint=x.get(num-1)
        }
        else{
            runOnUiThread {
                val toast = Toast.makeText(this, "this localhost doesn't contain url",Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    //update the room
    fun update_room(a: String){

        var db:AppDataBase=AppDataBase.getInstance(this)
        //save the url address as the last used addresed
        db.urlDAO().updateUrl(a,System.currentTimeMillis())

    }
    fun insrt_room(a: String) {
        var db:AppDataBase=AppDataBase.getInstance(this)
        //Toast.makeText(this, a, Toast.LENGTH_LONG).show()

        //save
        var url = Url_Entity()
        url.url_name = a
        url.URL_Date= System.currentTimeMillis()
        db.urlDAO().saveUrl(url)
        //db.urlDAO().deleteAll()

    }

    //CALL THIS FUNCTION WHEN CONNECTING TO SERVER
    fun connect(selectUrl: String) {
        val intent = Intent(this, MainActivity2::class.java)
        val gson = GsonBuilder().setLenient().create()
        try {
            var retrofit = Retrofit.Builder().baseUrl(selectUrl)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
            var api = retrofit.create(Api::class.java)
            val body = api.getImg().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    startActivity(intent)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(applicationContext, "Login attempt failed", Toast.LENGTH_SHORT).show()
                }

            })

        }catch (e : Exception) {
        }

    }
}