package com.kislaya.allrounder

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.size
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder
import java.net.URI
import javax.xml.transform.URIResolver

class OcrImageActivity : AppCompatActivity() {
    lateinit var txtDescriptionOne: TextView
    lateinit var txtResult: TextView
    lateinit var imgImage: ImageView

    val cameraRequest = 200
    val storageRequest = 400
    val imagePickGallaryCode = 1000
    val imagePickCameraCode = 1001

    var cameraPermission =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    var imgUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr_image)
        txtResult = findViewById(R.id.txtResult)
        txtDescriptionOne = findViewById(R.id.txtDescriptionOne)
        imgImage = findViewById(R.id.imgImage)
        supportActionBar?.title = "OCR Image Process"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.addImage) {
            showImageImportDialog()
        }
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showImageImportDialog() {
        val items = arrayOf("Camera", "Gallery")
        val dialog = AlertDialog.Builder(this@OcrImageActivity)
        dialog.setTitle("Select Image")
        dialog.setItems(items) { dialog, which ->
            if (which == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission()
                } else {
                    pickCamera()
                }

            }
            if (which == 1) {
                if (!checkStoragePermission()) {
                    requestStoragePermission()
                } else {
                    pickGallary()
                }

            }

        }
        dialog.create()
        dialog.show()
    }

    private fun pickGallary() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, imagePickGallaryCode)
    }

    private fun pickCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "NewPic")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to Text")
        imgUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
        startActivityForResult(cameraIntent, imagePickCameraCode)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this@OcrImageActivity, storagePermission, storageRequest)
    }

    private fun checkStoragePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this@OcrImageActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) ==
                PackageManager.PERMISSION_GRANTED

        return result
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this@OcrImageActivity, cameraPermission, cameraRequest)
    }

    private fun checkCameraPermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this@OcrImageActivity, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            this@OcrImageActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) ==
                PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            cameraRequest -> {
                if (grantResults.size > 0) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera()
                    } else {
                        Toast.makeText(
                            this@OcrImageActivity,
                            "Permission Denied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
            storageRequest -> {
                if (grantResults.size > 0) {
                    val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (writeStorageAccepted) {
                        pickGallary()
                    } else {
                        Toast.makeText(
                            this@OcrImageActivity,
                            "Permission Denied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == imagePickGallaryCode) {
                CropImage.activity(data!!.data)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this)
            }
            if (requestCode == imagePickCameraCode) {
                CropImage.activity(imgUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this)
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                txtDescriptionOne.visibility = View.GONE
                imgImage.setImageURI(resultUri)
                val bitmapDrawable = imgImage.drawable as BitmapDrawable
                val bitmap = bitmapDrawable.bitmap
                val recognizer = TextRecognizer.Builder(applicationContext).build()
                if (!recognizer.isOperational) {
                    Toast.makeText(this@OcrImageActivity, "Error", Toast.LENGTH_SHORT).show()
                } else {
                    val frame = Frame.Builder().setBitmap(bitmap).build()
                    val item = recognizer.detect(frame)
                    val sb = StringBuilder()
                    for (i in 0 until item.size()) {
                        val myItem = item.valueAt(i)
                        sb.append(myItem.value)
                        sb.append("\n")

                    }
                    txtResult.text = sb.toString()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this@OcrImageActivity, "$error", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
