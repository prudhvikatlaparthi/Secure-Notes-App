package com.pru.notes.help

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ImageUtil {
    private fun ImageUtil() {}

    companion object {
        @Throws(IOException::class)
        fun compressImage(
            imageFile: File, reqWidth: Int, reqHeight: Int,
            compressFormat: CompressFormat?, quality: Int, destinationPath: String?
        ): File? {
            var fileOutputStream: FileOutputStream? = null
            val file = File(destinationPath).parentFile
            if (!file.exists()) {
                file.mkdirs()
            }
            try {
                fileOutputStream = FileOutputStream(destinationPath)
                // write the compressed bitmap at the destination specified by destinationPath.
                decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight).compress(
                    compressFormat, quality,
                    fileOutputStream
                )
            } finally {
                if (fileOutputStream != null) {
                    fileOutputStream.flush()
                    fileOutputStream.close()
                }
            }
            return File(destinationPath)
        }

        @Throws(IOException::class)
        fun decodeSampledBitmapFromFile(
            imageFile: File,
            reqWidth: Int,
            reqHeight: Int
        ): Bitmap {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imageFile.absolutePath, options)
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            var scaledBitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options)
            //check the rotation of the image and display it properly
            val exif: ExifInterface
            exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
            } else if (orientation == 3) {
                matrix.postRotate(180f)
            } else if (orientation == 8) {
                matrix.postRotate(270f)
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height,
                matrix, true
            )
            return scaledBitmap
        }

        private fun calculateInSampleSize(
            options: BitmapFactory.Options, reqWidth: Int,
            reqHeight: Int
        ): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

        @Throws(IOException::class)
        fun createImageFile(
            context: Context,
            name: String?
        ): File? {
            // Create an image file name
            var mFileName: String?
            mFileName = name
            if (TextUtils.isEmpty(mFileName)) {
                mFileName = "UserProfile" + Date() + "_"
            }
            val storageDir =
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File.createTempFile(mFileName, ".png", storageDir)
        }

        /**
         * Get real file path from URI
         */
        fun getRealPathFromUri(
            context: Context,
            contentUri: Uri?
        ): String? {
            var cursor: Cursor? = null
            return try {
                val proj =
                    arrayOf(MediaStore.Images.Media.DATA)
                cursor = context.contentResolver.query(contentUri, proj, null, null, null)
                assert(cursor != null)
                val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                cursor.getString(column_index)
            } finally {
                cursor?.close()
            }
        }
    }
}