package com.pru.notes.activities

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.pru.notes.BuildConfig
import com.pru.notes.R
import com.pru.notes.custom_views.SignatureDrawView
import com.pru.notes.help.Constants.hideKeyboard
import com.pru.notes.help.FileCompressor
import com.pru.notes.help.ImageUtil.Companion.createImageFile
import com.pru.notes.help.ImageUtil.Companion.getRealPathFromUri
import com.pru.notes.help.RequestCodes.Companion.CAMERA_ACTIVITY_REQUEST_CODE
import com.pru.notes.help.RequestCodes.Companion.GALLERY_ACTIVITY_REQUEST_CODE
import com.pru.notes.help.RequestCodes.Companion.REQUEST_CAMERA_PERMISSION
import com.pru.notes.help.RequestCodes.Companion.REQUEST_STORAGE_PERMISSION
import com.pru.notes.listeners.ImagePickerListener
import kotlinx.android.synthetic.main.activity_add_note.*
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.tool_bar.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*


class AddEditNoteActivity : BaseActivity() {
    private lateinit var signatureDrawView: SignatureDrawView
    private var imgBytesArray: ByteArray? = null
    private var signBytesArray: ByteArray? = null
    private var mPhotoFile: File? = null
    private lateinit var addlayout: CoordinatorLayout
    private lateinit var modifiedDate: Date
    private var mCompressor: FileCompressor? = null

    companion object {
        const val EXTRA_ID = "com.pru.notes.EXTRA_ID"
        const val EXTRA_DESCRIPTION = "com.pru.notes.EXTRA_DESCRIPTION"
        const val EXTRA_MODIFIED_DATE = "com.pru.notes.EXTRA_MODIFIED_DATE"
        const val EXTRA_IMAGE = "com.pru.notes.EXTRA_IMAGE"
        const val EXTRA_IMAGE_SIGN = "com.pru.notes.EXTRA_IMAGE_SIGN"
    }

    override fun initialize() {
        addlayout = myInflater.inflate(R.layout.activity_add_note, null) as CoordinatorLayout
        addlayout.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
        llBody.addView(addlayout)
        modifiedDate = Date();
        if (intent.hasExtra(EXTRA_ID)) {
            tb_title.text = "Edit Notes"
            note_description.setText(intent.getStringExtra(EXTRA_DESCRIPTION))
            note_description.setSelection(note_description.length())
            imgBytesArray = intent.getByteArrayExtra(EXTRA_IMAGE)
            if (imgBytesArray != null) {
                Glide.with(this@AddEditNoteActivity).load(imgBytesArray)
                    .into(note_image)
            }
            signBytesArray = intent.getByteArrayExtra(EXTRA_IMAGE_SIGN)
            if (signBytesArray != null) {
                Glide.with(this@AddEditNoteActivity).load(signBytesArray)
                    .into(note_sign_image)
                note_sign_image.visibility = View.VISIBLE
            }
        } else {
            tb_title.text = "Add Notes"
        }
        note_description.requestFocus()
        drawViewInitialize()
        onClickListener()
        mCompressor = FileCompressor(this@AddEditNoteActivity)
    }

    private fun drawViewInitialize() {
        signatureDrawView = SignatureDrawView(this@AddEditNoteActivity)
        signature_pad_view.addView(signatureDrawView)
        signatureDrawView.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun onClickListener() {
        val rotate = ObjectAnimator.ofFloat(swipe_anim, "rotation", 0f, 270f)
        rotate.start()
        swipe_anim.setOnClickListener {
            if (it.tag.equals("0")) {
                it.tag = "1"
                val rotate = ObjectAnimator.ofFloat(swipe_anim, "rotation", 0f, 90f)
                rotate.start()
                bottom_layout.visibility = View.VISIBLE
            } else {
                it.tag = "0"
                val rotate = ObjectAnimator.ofFloat(swipe_anim, "rotation", 90f, 270f)
                rotate.start()
                bottom_layout.visibility = View.GONE
            }
        }
        add_text.setOnClickListener {
            note_description.requestFocus()
            add_erase.visibility = View.GONE
            lightup(1)
            checkPaint()
        }

        add_paint.setOnClickListener {
            addlayout.requestFocus()
            signature_pad_view.visibility = View.VISIBLE
            note_sign_image.visibility = View.GONE
            add_erase.visibility = View.VISIBLE
            lightup(2)
        }

        add_erase.setOnClickListener {
            signatureDrawView.clearSignature()
            lightup(3)
        }

        add_photo.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val permissionRequest =
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                requestPermissions(
                    permissionRequest,
                    REQUEST_CAMERA_PERMISSION
                )
            } else {
                doAddPhoto()
            }
        }
    }

    private fun doAddPhoto() {
        addlayout.requestFocus()
        lightup(0)
        checkPaint()
        add_erase.visibility = View.GONE
        showCaptureOptions(object : ImagePickerListener {
            override fun isPicked(isCamera: Boolean) {
                if (isCamera) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val permissionRequest =
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        requestPermissions(
                            permissionRequest,
                            REQUEST_CAMERA_PERMISSION
                        )
                    } else {
                        startCameraApp()
                    }
                } else {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        ) {
                            startGalleryApp()
                        } else {
                            val permission =
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            requestPermissions(permission, REQUEST_STORAGE_PERMISSION)
                        }
                    } else {
                        startGalleryApp()
                    }

                }
            }
        })
    }

    private fun checkPaint() {
        if (signature_pad_view.visibility == View.VISIBLE) {
            signatureDrawView.getBitmapS()?.let { it1 -> convertBimap_ByteArray(it1) }
            if (signBytesArray != null) {
                Glide.with(this@AddEditNoteActivity).load(signBytesArray)
                    .into(note_sign_image)
            }
            signature_pad_view.visibility = View.GONE
            note_sign_image.visibility = View.VISIBLE
        }
    }

    private fun lightup(position: Int) {
        when (position) {
            0 -> {
                // image
                add_photo.setColorFilter(
                    ContextCompat.getColor(this, R.color.blue),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_text.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_paint.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_erase.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
            }
            1 -> {
                // text
                add_photo.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_text.setColorFilter(
                    ContextCompat.getColor(this, R.color.blue),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_paint.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_erase.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
            }
            2 -> {
                //paint
                add_photo.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_text.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_paint.setColorFilter(
                    ContextCompat.getColor(this, R.color.blue),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_erase.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
            }
            3 -> {
                //eraser
                add_photo.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_text.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_paint.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                add_erase.setColorFilter(
                    ContextCompat.getColor(this, R.color.blue),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_ACTIVITY_REQUEST_CODE) {
                try {
                    if (mPhotoFile == null) {
                        return
                    }
                    mPhotoFile = mCompressor!!.compressToFile(mPhotoFile!!)
                    if (mPhotoFile == null) {
                        return
                    }
                    convertFile_ByteArray(mPhotoFile)
                    Glide.with(this@AddEditNoteActivity).load(imgBytesArray)
                        .into(note_image)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == GALLERY_ACTIVITY_REQUEST_CODE) {
                val selectedImage = data!!.data ?: return
                try {
                    mPhotoFile = mCompressor!!.compressToFile(
                        File(
                            getRealPathFromUri(
                                this@AddEditNoteActivity,
                                selectedImage
                            )
                        )
                    )
                    if (mPhotoFile == null) {
                        return
                    }
                    convertFile_ByteArray(mPhotoFile)
                    Glide.with(this@AddEditNoteActivity).load(imgBytesArray)
                        .into(note_image)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun saveNote() {
        addlayout.hideKeyboard()
        checkPaint()
        val data = Intent().apply {
            putExtra(EXTRA_DESCRIPTION, note_description.text.toString())
            putExtra(EXTRA_MODIFIED_DATE, modifiedDate.time)
            if (intent.getIntExtra(EXTRA_ID, -1) != -1) {
                putExtra(
                    EXTRA_ID, intent.getIntExtra(
                        EXTRA_ID, -1
                    )
                )
            }
            if (imgBytesArray != null) {
                putExtra(EXTRA_IMAGE, imgBytesArray)
            }
            if (signBytesArray != null) {
                putExtra(EXTRA_IMAGE_SIGN, signBytesArray)
            }
        }
        setResult(Activity.RESULT_OK, data)
        onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryApp()
                } else {
                    showSnackMessage("Storage permission denied.")
                }
            }
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    doAddPhoto()
                } else {
                    if (grantResults.size > 1) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            showSnackMessage("Camera permission denied.")
                        } else if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                            showSnackMessage("Storage permission denied.")
                        }
                    } else {
                        showSnackMessage("Camera permission denied.")
                    }
                }
            }
            else -> {
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startCameraApp() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile(
                    this@AddEditNoteActivity,
                    "I" + System.currentTimeMillis()
                )
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this@AddEditNoteActivity,
                    BuildConfig.APPLICATION_ID.toString() + ".provider",
                    photoFile
                )
                mPhotoFile = photoFile
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(
                    takePictureIntent,
                    CAMERA_ACTIVITY_REQUEST_CODE
                )
            }
        }

    }

    private fun startGalleryApp() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, GALLERY_ACTIVITY_REQUEST_CODE)

    }

    private fun convertFile_ByteArray(file: File?) {
        if (file != null) {
            imgBytesArray = ByteArray(file.length().toInt())
            val fis = FileInputStream(file)
            fis.read(imgBytesArray) //read file into bytes[]
            fis.close()
        }
    }

    private fun convertBimap_ByteArray(bmp: Bitmap) {
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 80, stream)
        signBytesArray = stream.toByteArray()
        bmp.recycle()
    }
}