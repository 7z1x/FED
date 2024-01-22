package bangkit.project.fed.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bangkit.project.fed.data.datastore.PreferencesDataStore
import kotlinx.coroutines.launch

// Di SharedViewModel.kt
class SharedViewModel(private val preferencesDataStore: PreferencesDataStore) : ViewModel() {

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> get() = _selectedImageUri

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
        uri?.let {
            viewModelScope.launch {
                preferencesDataStore.saveProfileImagePath(it.toString())
            }
        }
    }
}
