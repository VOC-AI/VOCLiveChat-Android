package com.vocai.pdfviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.Size
import androidx.annotation.Keep
import com.rajat.pdfviewer.util.CacheManager
import com.rajat.pdfviewer.util.CommonUtils
import com.rajat.pdfviewer.util.CommonUtils.Companion.calculateDynamicPrefetchCount
import com.vocai.sdk.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap


@Keep
internal class PdfRendererCore(
    private val context: Context,
    fileDescriptor: ParcelFileDescriptor
) {

    private var isRendererOpen = false

    constructor(context: Context, file: File) : this(
        context = context,
        fileDescriptor = getFileDescriptor(file)
    )

    private val openPages = ConcurrentHashMap<Int, PdfRenderer.Page>()
    private var pdfRenderer: PdfRenderer =
        PdfRenderer(fileDescriptor).also { isRendererOpen = true }
    private val cacheManager = CacheManager(context)

    companion object {

        private fun sanitizeFilePath(filePath: String): String {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val path = Paths.get(filePath)
                    if (Files.exists(path)) {
                        filePath
                    } else {
                        "" // Return a default safe path or handle the error
                    }
                } else {
                    filePath
                }
            } catch (e: Exception) {
                "" // Handle the exception and return a safe default path
            }
        }

        internal fun getFileDescriptor(file: File): ParcelFileDescriptor {
            val safeFile = File(sanitizeFilePath(file.path))
            return ParcelFileDescriptor.open(safeFile, ParcelFileDescriptor.MODE_READ_ONLY)
        }
    }


    init {
        cacheManager.initCache()
    }

    internal fun getBitmapFromCache(pageNo: Int): Bitmap? =
        cacheManager.getBitmapFromCache(pageNo)

    private fun addBitmapToMemoryCache(pageNo: Int, bitmap: Bitmap) =
        cacheManager.addBitmapToCache(pageNo, bitmap)

    private fun writeBitmapToCache(pageNo: Int, bitmap: Bitmap) {
        cacheManager.writeBitmapToCache(pageNo, bitmap)
    }

    fun pageExistInCache(pageNo: Int): Boolean =
        cacheManager.pageExistsInCache(pageNo)

    fun prefetchPages(currentPage: Int, width: Int, height: Int) {
        val dynamicPrefetchCount = calculateDynamicPrefetchCount(context, pdfRenderer)
        (currentPage - dynamicPrefetchCount..currentPage + dynamicPrefetchCount)
            .filter { it in 0 until pdfRenderer.pageCount && !pageExistInCache(it) }
            .forEach { pageNo ->
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = CommonUtils.Companion.BitmapPool.getBitmap(width, height)
                    renderPage(pageNo, bitmap) { success, _, _ ->
                        if (success) writeBitmapToCache(pageNo, bitmap)
                        else CommonUtils.Companion.BitmapPool.recycleBitmap(bitmap)
                    }
                }
            }
    }

    fun getPageCount(): Int {
        synchronized(this) {
            if (!isRendererOpen) return 0
            return pdfRenderer.pageCount
        }
    }

    fun renderPage(
        pageNo: Int,
        bitmap: Bitmap,
        onBitmapReady: ((success: Boolean, pageNo: Int, bitmap: Bitmap?) -> Unit)? = null
    ) {
        if (pageNo >= getPageCount()) {
            onBitmapReady?.invoke(false, pageNo, null)
            return
        }

        val cachedBitmap = getBitmapFromCache(pageNo)
        if (cachedBitmap != null) {
            CoroutineScope(Dispatchers.Main).launch {
                onBitmapReady?.invoke(
                    true,
                    pageNo,
                    cachedBitmap
                )
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            var success = false
            var renderedBitmap: Bitmap? = null

            synchronized(this@PdfRendererCore) {
                if (!isRendererOpen) return@launch

                openPageSafely(pageNo)?.use { pdfPage ->
                    try {
                        bitmap.eraseColor(Color.WHITE) // Clear bitmap

                        // Render PDF page
                        pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                        // Cache the rendered bitmap
                        addBitmapToMemoryCache(pageNo, bitmap)

                        // Prepare results outside the synchronized block
                        success = true
                        renderedBitmap = bitmap
                    } catch (e: Exception) {
                        Log.e("PdfRendererCore", "Error rendering page: ${e.message}", e)
                    }
                }
            }

            // Switch to Main thread **after** synchronized block releases the lock
            withContext(Dispatchers.Main) {
                onBitmapReady?.invoke(success, pageNo, renderedBitmap)
            }
        }
    }


    private suspend fun <T> withPdfPage(pageNo: Int, block: (PdfRenderer.Page) -> T): T? =
        withContext(Dispatchers.IO) {
            synchronized(this@PdfRendererCore) {
                runCatching {
                    pdfRenderer.openPage(pageNo).use { page ->
                        return@withContext block(page)
                    }
                }
            }
            null
        }

    private val pageDimensionCache = mutableMapOf<Int, Size>()

    fun getPageDimensionsAsync(pageNo: Int, callback: (Size) -> Unit) {
        pageDimensionCache[pageNo]?.let {
            callback(it)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val size = withPdfPage(pageNo) { page ->
                Size(page.width, page.height).also { pageSize ->
                    pageDimensionCache[pageNo] = pageSize
                }
            } ?: Size(1, 1) // Fallback to a default minimal size

            withContext(Dispatchers.Main) {
                callback(size)
            }
        }
    }

    private fun openPageSafely(pageNo: Int): PdfRenderer.Page? {
        synchronized(this) {
            if (!isRendererOpen) return null
            closeAllOpenPages()
            try {
                return pdfRenderer.openPage(pageNo).also { page ->
                    openPages[pageNo] = page
                }
            } catch (e: Exception) {
                LogUtil.info("page number:$pageNo open failed!")
                return null
            }
        }
    }

    private fun closeAllOpenPages() {
        synchronized(this) {
            openPages.values.forEach { page ->
                try {
                    page.close()
                } catch (e: IllegalStateException) {
                    Log.e("PDFRendererCore", "Page was already closed")
                }
            }
            openPages.clear() // Clear the map after closing all pages.
        }
    }

    fun closePdfRender() {
        synchronized(this) {
            closeAllOpenPages()
            if (isRendererOpen) {
                pdfRenderer?.close()
                isRendererOpen = false
            }
            cacheManager.clearCache()
        }
    }

}
