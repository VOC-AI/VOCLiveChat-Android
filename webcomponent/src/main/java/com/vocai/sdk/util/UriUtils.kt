package com.vocai.sdk.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore


object UriUtils {
    fun getFilePath(context: Context, uri: Uri?): String? {
        if (uri == null) return null

        // 1. 处理 file:// 类型的 Uri
        if (ContentResolver.SCHEME_FILE == uri.scheme) {
            return uri.path
        }

        // 2. 处理 content:// 类型的 Uri
        if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // 处理 DocumentProvider 的 Uri
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if ("primary".equals(split[0], ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else {
                // 普通 content:// Uri（如媒体文件）
                val projection = arrayOf(MediaStore.MediaColumns.DATA)
                try {
                    context.contentResolver.query(uri, projection, null, null, null).use { cursor ->
                        if (cursor != null && cursor.moveToFirst()) {
                            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }
}