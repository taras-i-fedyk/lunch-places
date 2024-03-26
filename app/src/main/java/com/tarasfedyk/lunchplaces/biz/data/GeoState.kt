package com.tarasfedyk.lunchplaces.biz.data

import android.location.Location
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Stable
@Parcelize
data class GeoState(
    val currentLocationStatus: Status<Unit, Location>? = null,
    val lunchPlacesStatus: Status<String, List<LunchPlace>>? = null
) : Parcelable {
    private companion object : Parceler<GeoState> {
        override fun GeoState.write(parcel: Parcel, flags: Int) {
            /*when (currentLocationStatus) {
                null -> {}
                is Status.Pending -> {}
                is Status.Success -> {
                    val isSuccess = true
                    parcel.writeInt(if (isSuccess) 1 else 0)
                    parcel.writeParcelable(currentLocationStatus.result, flags)
                }
                is Status.Failure -> {
                    val isSuccess = false
                    parcel.writeInt(if (isSuccess) 1 else 0)
                    parcel.writeSerializable(currentLocationStatus.error)
                }
            }*/
        }

        override fun create(parcel: Parcel): GeoState {
            /*val isSuccess = parcel.readInt() != 0
            val currentLocationStatus: Status<Unit, Location>? =
                if (isSuccess) {
                    val result = parcel.readParcelable<Location>(Location::class.java.classLoader) as Location
                    Status.Success(Unit, result)
                } else {
                    val error = parcel.readSerializable() as Error
                    Status.Failure(Unit, error)
                }
            return GeoState(currentLocationStatus, null)*/
            return GeoState()
        }
    }
}