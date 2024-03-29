package bangkit.project.fed.ui.captureegg.imagedisplay

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bangkit.project.fed.MainActivity
import bangkit.project.fed.R
import bangkit.project.fed.data.api.ApiConfig
import bangkit.project.fed.databinding.ActivityImageDisplayBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageDisplayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageDisplayBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.uploadButton.setOnClickListener {
            uploadImage()
        }
        getImage()
    }

//    private fun uploadImageUsingFirestore() {
//        val userId = auth.currentUser?.uid.toString()
//        val imageName = binding.photonameEd.text.toString().trim()
//
//        if (imageName.isEmpty()) {
//            showToast(getString(R.string.empty_image_warning))
//            return
//        }
//
//        showLoading(true)
//
//        try {
//            val uid = auth.currentUser?.uid.toString()
//            val image = (binding.imageView.drawable as BitmapDrawable).bitmap
//            val imageFile = convertBitmapToFile(image)
//
//            // Use a coroutine scope to call the suspend function
//            lifecycleScope.launch {
//                try {
//                    // Attempt to upload the image and get the URL
//                    val imageUrl = FirestoreHelper().uploadImage(imageFile, userId, imageName)
//
//                    // If successful, upload additional data
//                    val detectionTimestamp = System.currentTimeMillis()
//                    val formattedTimestamp = formatDate(detectionTimestamp)
//                    FirestoreHelper().uploadDataEgg(formattedTimestamp, "", imageUrl, imageName, userId)
//
//                    // Show success message or perform other actions on success
//                    showToast("Image uploaded successfully!")
//                    val intent = Intent(this@ImageDisplayActivity, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()
//
//                } catch (e: Exception) {
//                    // Handle errors
//                    showToast("Error uploading image: ${e.message}")
//                    e.printStackTrace()
//                } finally {
//                    // Ensure loading indicator is hidden
//                    showLoading(false)
//                }
//            }
//
//        } catch (e: Exception) {
//            // Handle other non-coroutine exceptions here
//            showToast("Error: ${e.message}")
//            e.printStackTrace()
//            showLoading(false)
//        }
//    }


    private fun uploadImage() {
        val imageName = binding.photonameEd.text.toString().trim()

        if (imageName.isEmpty()) {
            showToast(getString(R.string.empty_image_warning))
            return
        }
        showLoading(true)
        lifecycleScope.launch {
            val uid = auth.currentUser?.uid.toString()

            val image = (binding.imageView.drawable as BitmapDrawable).bitmap
            val file = convertBitmapToFile(image)
            val requestFile = file.asRequestBody("multipart/form-data".toMediaType())
            val filePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            Log.i("infoo", file.length().toString())
            val detectionTimestamp = System.currentTimeMillis()
            val formattedTimestamp = formatDate(detectionTimestamp)


            val apiService = ApiConfig.getApiService(uid)

            try {
                apiService.uploadImagetoDetect(filePart, imageName, uid, formattedTimestamp)
                showToast("Image uploaded successfully.")

                val intent = Intent(this@ImageDisplayActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                showToast("Error uploading image: ${e.message}")
                Log.e("UploadImage", "Error uploading image", e)
            } finally {
                showLoading(false)
                val intent = Intent(this@ImageDisplayActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun convertBitmapToFile(bitmap: Bitmap): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_${timeStamp}.jpg"
        val file = File(cacheDir, fileName)

        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            Log.e("ConvertBitmapToFile", "Error converting bitmap to file", e)
        }

        return file
    }
//
//    private fun File.reduceFileImage(): File {
//        val file = this
//        val bitmap = BitmapFactory.decodeFile(file.path)
//        var compressQuality = 100
//        var streamLength: Int
//        do {
//            val bmpStream = ByteArrayOutputStream()
//            bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
//            val bmpPicByteArray = bmpStream.toByteArray()
//            streamLength = bmpPicByteArray.size
//            compressQuality -= 5
//        } while (streamLength > MAXIMAL_SIZE)
//        bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
//        return file
//    }


    private fun getImage() {
        val imageUri = intent.getParcelableExtra<Uri>("imageUri")
        val capturedImage = intent.getParcelableExtra<Uri>("capturedImage")
        when {
            imageUri != null -> showImage(imageUri)
            capturedImage != null -> showCapturedImage(capturedImage)
            else -> {
                Toast.makeText(this@ImageDisplayActivity, "No Image Available", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showCapturedImage(capturedImage: Uri) {
        this.contentResolver.notifyChange(capturedImage, null)
        originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, capturedImage)
        binding.imageView.setImageBitmap(originalBitmap)

        binding.rotateButton.setOnClickListener {
            rotateBitmap(-90f)
        }

    }

    private fun showImage(uri: Uri) {

        originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        Glide.with(this)
            .load(originalBitmap)
            .into(binding.imageView)

        binding.rotateButton.setOnClickListener {
            rotateBitmap(-90f)
        }
    }

    private fun rotateBitmap(degrees: Float) {
        val matrix = Matrix()
        matrix.postRotate(degrees)

        val rotatedBitmap = Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )

        binding.imageView.setImageBitmap(rotatedBitmap)
        originalBitmap = rotatedBitmap

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ss a zzz", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }


    companion object {
        private const val MAXIMAL_SIZE = 1000000
    }
}