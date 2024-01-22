package bangkit.project.fed.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import bangkit.project.fed.data.EggData
import bangkit.project.fed.data.Phase
import bangkit.project.fed.data.api.FirestoreHelper
import bangkit.project.fed.databinding.FragmentHomeBinding
import bangkit.project.fed.ui.home.adapter.LibraryRvAdapter
import bangkit.project.fed.ui.home.adapter.RecentRvAdapter
import bangkit.project.fed.ui.home.detail.DetailActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.Arrays

class HomeFragment : Fragment(), LibraryRvAdapter.OnItemClickListener, RecentRvAdapter.OnItemClickListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: HomeViewModel
    private lateinit var libraryAdapter: LibraryRvAdapter
    private lateinit var recentAdapter: RecentRvAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        getLibraryList()
        getRecentList()

        return binding.root
    }

    private fun getRecentList() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        val currentUser = auth.currentUser
        currentUser?.uid?.let {
            viewModel.fetchEggDataByRecentDate(it)
        }

        recentAdapter = RecentRvAdapter(requireContext(), this)
        binding.recentRv.adapter = recentAdapter
        binding.recentRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val firestoreHelper = FirestoreHelper()
        firestoreHelper.getDataEggByUserId(uid) {eggDataList ->
            recentAdapter.submitList(eggDataList)
        }
        recentAdapter.notifyDataSetChanged()
    }

    private fun getLibraryList() {

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            viewModel.fetchEggDataByUserId(userId)
        }

        libraryAdapter = LibraryRvAdapter(requireContext(), this)
        binding.LibraryRv.adapter = libraryAdapter
        binding.LibraryRv.layoutManager = LinearLayoutManager(requireContext())

        val firestoreHelper = FirestoreHelper()
        firestoreHelper.getDataEggByUserId(uid) {eggDataList ->
            libraryAdapter.submitList(eggDataList)
        }
        libraryAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(data: EggData) {

        val intent = Intent(requireContext(), DetailActivity::class.java)

        // Pass phases data to DetailActivity
        val phases = data.phases.map { phase ->
            Phase(
                kelas = phase.kelas,
                probabilitas = phase.probabilitas
            )
        }.toTypedArray()
        Log.d("HomeFragment", "Sending phases: ${Arrays.toString(phases)}")

        intent.putExtra("phases", phases)

        intent.putExtra("label", data.label)
        intent.putExtra("imageURL", data.imageURL)
        intent.putExtra("timestamp", data.timestamp?.seconds)

        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}