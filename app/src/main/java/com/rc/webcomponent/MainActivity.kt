package com.rc.webcomponent

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vocai.sdk.Vocai

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        Vocai.Companion.getInstance().init(this,true)
        findViewById<TextView>(R.id.mChatStart).setOnClickListener {
            Vocai.Companion.getInstance().startChat("19365","6731F71BE4B0187458389512", extra =  hashMapOf(
                "email" to "boyuan.gao@shulex-tech.com"
            ))
        }

    }
}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    WebComponentTheme {
//        Greeting("Android")
//    }
//}