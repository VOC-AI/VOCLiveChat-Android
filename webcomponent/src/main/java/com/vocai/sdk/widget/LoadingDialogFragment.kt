package com.vocai.sdk.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.vocai.sdk.databinding.FragmentLoadingBinding

class LoadingDialogFragment() : DialogFragment() {

    private lateinit var binding: FragmentLoadingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadingBinding.inflate(inflater)
        return binding.root
    }

}