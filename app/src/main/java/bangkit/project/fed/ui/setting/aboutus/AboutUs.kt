package bangkit.project.fed.ui.setting.aboutus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bangkit.project.fed.databinding.ActivityAboutUsBinding

class AboutUs : AppCompatActivity() {
    private lateinit var binding: ActivityAboutUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}