package my.johnshaw21345.translation

import android.net.TrafficStats
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import cn.json.dict.JsonRootBean
import cn.json.dict.Trans
import cn.json.dict.Web_trans
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.EventListener
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.math.round


class MainActivity : AppCompatActivity() {

    var text1:TextView? = null
    var text2:TextView? = null
    var outputText:TextView? = null
    var exchangeBtn: Button? = null
    var submitBtn: Button? = null
    var inputText: EditText? = null
    var dataStats: TextView? = null
    var flag = 1
    var originalByteR = 0.0
    var originalByteS = 0.0
    var currentByteR = 0.0
    var currentByteS = 0.0
    var rKB = 0.0
    var sKB = 0.0

    var Uid = 0

    val okhttpListener = object : EventListener() {
        override fun dnsStart(call: Call, domainName: String) {
            super.dnsStart(call, domainName)
            runOnUiThread{
            outputText?.text = outputText?.text.toString() + "\nDns Search:" + domainName}
        }

        override fun responseBodyStart(call: Call) {
            super.responseBodyStart(call)
            runOnUiThread{
            outputText?.text = outputText?.text.toString() + "\nResponse Start"}
        }
    }
    val client: OkHttpClient = OkHttpClient
        .Builder()
        .eventListener(okhttpListener).build()

    val gson = GsonBuilder().create()


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text1 = findViewById(R.id.text1)
        text2 = findViewById(R.id.text2)
        outputText = findViewById(R.id.output)
        inputText = findViewById(R.id.input)
        exchangeBtn = findViewById(R.id.exchange)
        submitBtn = findViewById(R.id.submit)
        dataStats = findViewById(R.id.stats)

        var Uid = applicationInfo.uid



        originalByteR = TrafficStats.getUidRxBytes(Uid).toDouble()
        Log.e("-@>",originalByteR.toString())
        originalByteS = TrafficStats.getUidTxBytes(Uid).toDouble()


        exchangeBtn?.setOnClickListener {
            exchangeLanguage()
        }

        submitBtn?.setOnClickListener {
            click()
        }

        class RefreshTimer : TimerTask(){
            override fun run(){
                currentByteR = TrafficStats.getUidRxBytes(Uid).toDouble()
                currentByteS = TrafficStats.getUidTxBytes(Uid).toDouble()
                rKB = round((currentByteR-originalByteR)/1024*100)/100
                sKB = round((currentByteS-originalByteS)/1024*100)/100
                Log.e("-@>",rKB.toString() + sKB.toString())
                dataStats?.setText("当前使用流量：\n上行${sKB}KB\n下行${rKB}KB")
            }
        }

        val refresh = RefreshTimer()

        Timer().schedule(refresh, Date(),500)



    }

    fun exchangeLanguage(){
        flag = -flag

        if (flag == -1) {
            text1?.setText(R.string.english)
            text2?.setText(R.string.chinese)
            inputText?.setHint(R.string.inputE)
        }
        if (flag == 1){
            text1?.setText(R.string.chinese)
            text2?.setText(R.string.english)
            inputText?.setHint(R.string.inputC)
        }


    }

    fun request(url: String, callback: Callback){
        val request: Request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(callback)

    }

    fun click(){
        var url = "https://dict.youdao.com/jsonapi?q="+inputText?.text
        request(url, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                outputText?.text = e.message
            }



            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                try {
                    val translationBean = gson.fromJson(bodyString, JsonRootBean::class.java)

                    runOnUiThread {
                        //outputText?.text = bodyString
                            outputText?.text = "Json extraction failed"
                            if (translationBean.web_trans != null){
                            outputText?.text = translationBean.web_trans.web_translation[0].trans[0].value
                            }
                            if (translationBean.special != null){
                            outputText?.text = translationBean.special.entries[0].entry.trs[0].tr.nat}
                            if (translationBean.fanyi != null){
                            outputText?.text = translationBean.fanyi.tran()}


                    }
                } catch (ex: java.lang.NullPointerException) {
                    ex.printStackTrace()
                    outputText?.text = "Json extraction failed"
                }
            }
        })



    }



}


