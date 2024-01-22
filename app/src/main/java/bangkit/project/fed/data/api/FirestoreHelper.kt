package bangkit.project.fed.data.api

import android.net.Uri
import android.util.Log
import android.widget.Toast
import bangkit.project.fed.data.EggData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class FirestoreHelper {

    private val firestore = FirebaseFirestore.getInstance()
    private val eggDetectedCollection = firestore.collection("Egg-detected")
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    fun getDataEggByUserId(userId: String?, onComplete: (List<EggData>) -> Unit) {
        eggDetectedCollection
            .whereEqualTo("userId", "\"${userId}\"")
            .get()
            .addOnSuccessListener { documents ->
                val eggDataList = mutableListOf<EggData>()
                for (document in documents) {
                    val eggData = document.toObject(EggData::class.java)
                    eggDataList.add(eggData)
                }
                onComplete(eggDataList)
            }.addOnFailureListener { exception ->
                Log.e("FirestoreHelper", "Error fetching eggs: ${exception.message}", exception)
                onComplete(emptyList())
            }
    }

    fun getDataEggByRecentDate(userId: String, onComplete: (List<EggData>) -> Unit){
        eggDetectedCollection
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val eggDataList = mutableListOf<EggData>()
                for (document in documents) {
                    val eggData = document.toObject(EggData::class.java)
                    eggDataList.add(eggData)
                }
                Log.d("FirestoreHelper", "Received ${eggDataList.size} recent eggs for user $userId")
                onComplete(eggDataList)
            }.addOnFailureListener {
                Log.e("FirestoreHelper", "Error fetching recent eggs: $it")
                onComplete(emptyList())
            }
    }

    fun deleteData(label: String?, userId: String?, timestamp: Timestamp?, onComplete: () -> Unit) {
        eggDetectedCollection
            .whereEqualTo("label", label)
            .whereEqualTo("userId", userId)
            .whereEqualTo("timestamp", timestamp)
            .get().addOnSuccessListener {documents ->
                for(document in documents){
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("FirestoreHelper", "Success Delete Data")
                            onComplete()
                        }.addOnFailureListener {
                            Log.e("FirestoreHelper", "Error delete Data: $it")
                            onComplete()
                        }
                }
            }.addOnFailureListener {
                Log.e("FirestoreHelper", "Error Querying Document: $it")
                onComplete
            }
    }


    //Backup jika API tidak sukses
    fun uploadDataEgg(
        detectionTimestamp: Timestamp,
        fertilization: String,
        imageUrl: String,
        label: String,
        userId: String

    ) {
        try {
            val eggData = EggData(emptyList(), userId, label, imageUrl, detectionTimestamp)

            eggDetectedCollection
                .add(eggData)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun uploadImage(imageFile: File, userId: String, imageName: String): String = withContext(Dispatchers.IO) {
        try {
            val imageRef = storageReference.child("images/$userId/${UUID.randomUUID()}_$imageName.jpg")
            val uploadTask = imageRef.putFile(Uri.fromFile(imageFile)).await()

            // Check for any exceptions during the upload
            if (uploadTask.task.isSuccessful) {
                // Get the download URL after the upload is complete
                val downloadUrl = imageRef.downloadUrl.await().toString()

                // Return the download URL
                downloadUrl
            } else {
                // Handle the case where the upload was not successful
                throw Exception("Upload failed. Status: ${uploadTask.task.exception?.message}")
            }
        } catch (e: Exception) {
            // Handle the exception, e.g., log an error
            e.printStackTrace()
            throw e
        }
    }
}

