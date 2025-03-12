package com.vocai.sdk.bottomsheet

import android.graphics.Color
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
abstract class BaseBottomSheetDialogFragment() : BottomSheetDialogFragment() {

    private var heightPercent: Float = 0f

    var backgroundTransparent: Boolean = false

    fun setBottomSheetHeightPercent(percent: Int) {
        heightPercent = (percent / 100F).toFloat()
    }

    override fun onStart() {
        super.onStart()
        val view =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        if (backgroundTransparent) {
            view?.setBackgroundColor(Color.TRANSPARENT)
        }
        if (heightPercent > 0) {
            val expectedHeight = (resources.displayMetrics.heightPixels * heightPercent).toInt()
            view?.let {
                it.layoutParams.height = expectedHeight
                val behavior = BottomSheetBehavior.from(view)
                behavior.setPeekHeight(expectedHeight)
            }
        }
    }

}