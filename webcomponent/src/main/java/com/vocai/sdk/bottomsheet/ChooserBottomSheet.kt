package com.vocai.sdk.bottomsheet

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.vocai.sdk.LogUtil
import com.vocai.sdk.R
import com.vocai.sdk.Vocai
import com.vocai.sdk.extension.toi18nString
import com.vocai.sdk.model.FILE_TYPE_OTHER
import com.vocai.sdk.model.FILE_TYPE_PIC
import com.vocai.sdk.model.FILE_TYPE_VIDEO
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ChooserBottomSheet() : BaseBottomSheetDialogFragment() {

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Intent>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryPermissionLauncher: ActivityResultLauncher<Array<String>>

    companion object {
        const val TAG_VIDEO = 1
        const val TAG_PICTURE = 2
        const val TAG_GALLERY = 3
        const val TAG_FILE = 4
        const val KEY_RETURN_DATA = "return-data"
        const val AUTHORITIES_FILE_PROVIDER = ".vocai.sdk.fileprovider"
    }

    private var clickTag = -1
    private var resultFile: File? = null

    var onResultChosen: ((File, Int, String?) -> Unit)? = null
    var onCancelled: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backgroundTransparent = true
        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it.containsValue(false)) {
                    showPermissionDenyMsg()
                } else {
                    launchBehaviorPerClickTag(clickTag)
                }
            }
        takeVideoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    resultFile?.let {
                        onResultChosen?.invoke(it, FILE_TYPE_VIDEO, null)
                    }
                }
                clickTag = -1
            }
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    resultFile?.let {
                        onResultChosen?.invoke(it, FILE_TYPE_PIC, null)
                    }
                }
                clickTag = -1
            }
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val uri: Uri? = it.data?.data
                    uri?.let {
                        processGalleryChosen(it)
                    }
                }
                clickTag = -1
            }
        fileChooserLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val uri: Uri? = it.data?.data
                    uri?.let {
                        processFileChooser(it)
                    }
                }
                clickTag = -1
            }
        galleryPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it.containsValue(false)) {
                    showPermissionDenyMsg()
                } else {
                    launchBehaviorPerClickTag(clickTag)
                }
            }
    }

    private fun showPermissionDenyMsg() {
        val dialog =
            AlertDialog.Builder(requireContext()).setMessage("key_permission_deny".toi18nString())
                .show()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            dialog.dismiss()
         }, 2000)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_chooser, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.mTakeVideoTv).apply {
            text = "key_take_video".toi18nString()
            setOnClickListener {
                clickTag = TAG_VIDEO
                val permissions =
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                if (isPermissionGranted(permissions)) {
                    launchBehaviorPerClickTag(clickTag)
                } else {
                    cameraPermissionLauncher.launch(permissions)
                }
            }
        }
        view.findViewById<TextView>(R.id.mTakePhotoTv).apply {
            text = "key_take_photo".toi18nString()
            setOnClickListener {
                clickTag = TAG_PICTURE
                val permissions = arrayOf(Manifest.permission.CAMERA)
                if (isPermissionGranted(permissions)) {
                    launchBehaviorPerClickTag(clickTag)
                } else {
                    cameraPermissionLauncher.launch(permissions)
                }
            }
        }
        view.findViewById<TextView>(R.id.mGalleryTv).apply {
            text = "key_choose_from_gallery".toi18nString()
            setOnClickListener {
                clickTag = TAG_GALLERY
                val permissions = getGalleryPermissionCompat()
                if (isPermissionGranted(permissions)) {
                    launchBehaviorPerClickTag(clickTag)
                } else {
                    galleryPermissionLauncher.launch(permissions)
                }
            }
        }
        view.findViewById<TextView>(R.id.mFileChooserTv).apply {
            text = "key_choose_from_file".toi18nString()
            setOnClickListener {
                clickTag = TAG_FILE
                val permissions = getGalleryPermissionCompat()
                if (isPermissionGranted(permissions)) {
                    launchBehaviorPerClickTag(clickTag)
                } else {
                    galleryPermissionLauncher.launch(permissions)
                }
            }
        }
        view.findViewById<TextView>(R.id.mCancelTv).apply {
            text = "key_cancel".toi18nString()
            setOnClickListener {
                onCancelled?.invoke()
                dismiss()
            }
        }
    }
    
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onCancelled?.invoke()
    }

    private fun launchBehaviorPerClickTag(tag: Int) {
        when (tag) {
            TAG_VIDEO -> {
                takeVideoLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                    resultFile = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        System.currentTimeMillis().toString().plus(".mp4")
                    )
                    putExtra(KEY_RETURN_DATA, false)
                    putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        createFileFromProvider(requireContext(), resultFile!!)
                    )
                })
            }

            TAG_PICTURE -> {
                takePictureLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    resultFile = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        System.currentTimeMillis().toString().plus(".png")
                    )
                    putExtra(KEY_RETURN_DATA, false)
                    putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        createFileFromProvider(requireContext(), resultFile!!)
                    )
                })
            }

            TAG_GALLERY -> {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    this.setType("image/*")
                }
                galleryLauncher.launch(intent)
            }

            TAG_FILE -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    this.setType("*/*")
                    this.putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf("image/*", "video/*", "application/pdf")
                    )
                    this.addCategory(Intent.CATEGORY_OPENABLE)
                }
                fileChooserLauncher.launch(intent)
            }

        }
    }

    private fun processGalleryChosen(uri: Uri) {
        val contentResolver = getVocaiInstance().wrapper.getContext()?.contentResolver
        val mimeType = contentResolver?.getType(uri)
        LogUtil.info("chosen from gallery->$uri mimeType->$mimeType")
        getVocaiInstance().wrapper.getContext()?.let { ctx ->
            try {
                // 先尝试直接获取文件路径（适用于本地相册）
                var file = getFileIfExists(ctx, uri)
                
                // 如果直接获取失败，使用流复制方式（适用于云端相册如 Google Photos）
                if (file == null || !file.exists()) {
                    val fileName = getFileName(ctx, uri) ?: "temp_${System.currentTimeMillis()}"
                    val path = readFileFromUri(ctx, uri, fileName)
                    if (path.isNotEmpty()) {
                        file = File(path)
                    }
                }
                
                file?.let {
                    if (mimeType?.startsWith("image/") == true) {
                        onResultChosen?.invoke(it, FILE_TYPE_PIC, null)
                    } else if (mimeType?.startsWith("video/") == true) {
                        onResultChosen?.invoke(it, FILE_TYPE_VIDEO, null)
                    } else {
                        LogUtil.info("unknown mimeType->$mimeType")
                        // 未知类型也尝试作为图片处理
                        onResultChosen?.invoke(it, FILE_TYPE_PIC, null)
                    }
                } ?: run {
                    LogUtil.info("Failed to get file from uri: $uri")
                    onCancelled?.invoke()
                }
            } catch (e: Exception) {
                LogUtil.info("Error processing gallery chosen: ${e.message}")
                e.printStackTrace()
                onCancelled?.invoke()
            }
        } ?: run {
            onCancelled?.invoke()
        }
    }

    private fun getVocaiInstance(): Vocai = Vocai.getInstance()

    private fun processFileChooser(uri: Uri) {
        val contentResolver = getVocaiInstance().wrapper.getContext()?.contentResolver
        val mimeType = contentResolver?.getType(uri)
        LogUtil.info("chosen from file chooser->$uri mimeType->$mimeType")
        getVocaiInstance().wrapper.getContext()?.let { ctx ->
            try {
                val fileName = getFileName(requireContext(), uri)
                val path = readFileFromUri(requireContext(), uri, fileName ?: "")
                if (path.isNotEmpty()) {
                    onResultChosen?.invoke(File(path), getTagByMimeType(mimeType), fileName)
                } else {
                    LogUtil.info("invalid path:$path")
                    onCancelled?.invoke()
                }
            } catch (e: Exception) {
                LogUtil.info("Error processing file chooser: ${e.message}")
                e.printStackTrace()
                onCancelled?.invoke()
            }
        } ?: run {
            onCancelled?.invoke()
        }
    }

    private fun getTagByMimeType(mimeType: String?): Int {
        if (mimeType?.startsWith("image/") == true) {
            return FILE_TYPE_PIC
        } else if (mimeType?.startsWith("video/") == true) {
            return FILE_TYPE_VIDEO
        } else {
            return FILE_TYPE_OTHER
        }
    }

    private fun createFileFromProvider(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}$AUTHORITIES_FILE_PROVIDER",
            file
        )
    }

    private fun isPermissionGranted(permissions: Array<String>): Boolean = permissions.firstOrNull {
        requireActivity().checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
    } == null

    private fun getGalleryPermissionCompat(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 不需要权限，使用系统选择器
            emptyArray()
        } else {
            getGalleryPermission()
        }

    private fun getGalleryPermission(): Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private fun getFile(context: Context, uri: Uri): File {
        var path = ""
        val colum = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, colum, null, null, null)
        if (cursor != null) {
            cursor.moveToNext()
            val index = cursor.getColumnIndex(colum[0])
            path = cursor.getString(index)
            cursor.close()
        }
        return File(path)
    }
    
    /**
     * 尝试获取文件，如果文件不存在则返回 null
     * 这个方法适用于本地存储的文件
     */
    private fun getFileIfExists(context: Context, uri: Uri): File? {
        return try {
            var path: String? = null
            val colum = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, colum, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(colum[0])
                    if (index >= 0) {
                        path = cursor.getString(index)
                    }
                }
                cursor.close()
            }
            
            if (path != null && path.isNotEmpty()) {
                val file = File(path)
                if (file.exists()) file else null
            } else {
                null
            }
        } catch (e: Exception) {
            LogUtil.info("getFileIfExists error: ${e.message}")
            null
        }
    }

    fun readFileFromUri(context: Context, uri: Uri?, fileName: String): String {
        if (uri == null) return ""
        LogUtil.info("readFileFromUri fileName:$fileName")
        var inputStream: java.io.InputStream? = null
        var outputStream: FileOutputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            // 使用输入流读取文件内容（如保存到临时文件）
            if (inputStream != null) {
                // 示例：保存到临时文件
                val tempFile =
                    File.createTempFile("temp", ".${getSuffix(fileName)}", context.cacheDir)
                outputStream = FileOutputStream(tempFile)
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
                return tempFile.absolutePath
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return ""
    }

    private fun getSuffix(fileName: String): String? = fileName.split(".").getOrNull(1)

    private fun getFileName(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        var fileName: String? = null
        try {
            // 查询 DISPLAY_NAME 字段
            cursor = context.contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
        return fileName
    }

}