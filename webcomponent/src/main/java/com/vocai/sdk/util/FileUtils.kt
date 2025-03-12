package com.vocai.sdk.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.vocai.sdk.bottomsheet.ChooserBottomSheet
import java.io.File

object FileUtils {

    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}${ChooserBottomSheet.AUTHORITIES_FILE_PROVIDER}", file)
    }

}