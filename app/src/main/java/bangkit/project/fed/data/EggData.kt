package bangkit.project.fed.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class EggData(
    val phases : List<Phase> = emptyList(),
    val userId: String? = null,
    val label: String? = null,
    val imageURL : String? = null,
    val timestamp : Timestamp? = null
)

data class Phase(
    val kelas: String? = null,
    val probabilitas: Double? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(kelas)
        parcel.writeValue(probabilitas)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Phase> {
        override fun createFromParcel(parcel: Parcel): Phase {
            return Phase(parcel)
        }

        override fun newArray(size: Int): Array<Phase?> {
            return arrayOfNulls(size)
        }
    }
}




