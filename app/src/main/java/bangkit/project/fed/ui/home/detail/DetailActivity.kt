package bangkit.project.fed.ui.home.detail

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import bangkit.project.fed.data.Phase
import bangkit.project.fed.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val label = intent.getStringExtra("label")
        val imageURL = intent.getStringExtra("imageURL")
        val timestamp = intent.getLongExtra("timestamp", 0)

        // Retrieve phases data from intent
        val phases = intent.getParcelableArrayExtra("phases")?.mapNotNull {
            it as? Phase
        }
        // Log phases data
        Log.d("DetailActivity", "Received phases: $phases")
        // Display data with phases
        displayData(label, imageURL, timestamp, phases)
    }

    private fun displayData(label: String?, imageURL: String?, timestamp: Long, phases: List<Phase>?) {
        binding.recentName.text = label

        // Konversi timestamp ke format tanggal yang diinginkan
        val formattedDate = convertTimestampToDate(timestamp)
        binding.recentDate.text = formattedDate

        // Display phases data
        val phasesText = phases?.joinToString(separator = "\n") { phase ->
            "Kelas: ${phase.kelas}, Probabilitas: ${phase.probabilitas}"
        } ?: "No phases data available"

        binding.Desc.text = phasesText

        Glide.with(this)
            .load(imageURL)
            .into(binding.recentImage)
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy hh:mma", Locale.getDefault())
        val date = Date(timestamp * 1000L)
        return sdf.format(date)
    }
}