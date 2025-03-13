package com.rc.webcomponent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rc.webcomponent.ui.theme.WebComponentTheme
import com.vocai.sdk.Vocai

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Vocai.Companion.getInstance().init(this,true)
        setContent {
            WebComponentTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
//                            boyuan.gao@shulex-tech.com
                            Vocai.Companion.getInstance().startChat("19365","6731F71BE4B0187458389512", extra =  hashMapOf(
                                "email" to "boyuan.gao@shulex-tech.com"
                            ))
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WebComponentTheme {
        Greeting("Android")
    }
}