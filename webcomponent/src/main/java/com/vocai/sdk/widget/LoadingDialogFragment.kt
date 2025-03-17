//package com.vocai.sdk.widget
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.DialogFragment
//import com.rc.webcomponent.databinding.FragmentComposeBinding
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.launch
//
//class LoadingDialogFragment() : DialogFragment() {
//
//    private lateinit var binding: FragmentComposeBinding
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentComposeBinding.inflate(inflater)
//        return binding.mComposeView
//    }
//
//    var loadingText: MutableStateFlow<String> = MutableStateFlow("")
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
////        binding.mComposeView.setContent {
//
////            val text = loadingText.collectAsState()
////
////            Column(
////                modifier = Modifier
////                    .padding(10.dp),
////            ) {
////                CircularProgressIndicator(
////                    modifier = Modifier
////                        .size(24.dp)
////                        .align(Alignment.CenterHorizontally),
////                    color = Color(0xff5f9ea0)
////                )
////                if(text.value.isNotEmpty()) {
////                    Text(
////                        text = text.value,
////                        textAlign = TextAlign.Center,
////                        modifier = Modifier.align(Alignment.CenterHorizontally)
////                    )
////                }
////            }
////        }
//    }
//
//    fun updateText(text: String) {
//        GlobalScope.launch {
//            loadingText.emit(text)
//        }
//    }
//
//}