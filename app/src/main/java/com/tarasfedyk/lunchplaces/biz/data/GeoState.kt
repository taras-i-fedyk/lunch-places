package com.tarasfedyk.lunchplaces.biz.data

import android.os.Parcel
import android.os.Parcelable
import com.tarasfedyk.lunchplaces.biz.util.readBool
import com.tarasfedyk.lunchplaces.biz.util.writeBool
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeoState(
    val currentLocationStatus: Status<Unit, LocationSnapshot>? = null,
    val lunchPlacesStatus: Status<SearchFilter, List<LunchPlace>>? = null
) : Parcelable {

    private companion object : Parceler<GeoState> {
        override fun GeoState.write(parcel: Parcel, flags: Int) {
            writeCurrentLocationStatus(parcel, flags)
            writeLunchPlacesStatus(parcel, flags)
        }

        private fun GeoState.writeCurrentLocationStatus(parcel: Parcel, flags: Int) {
            val isCurrentLocationStatusNull = currentLocationStatus == null
            parcel.writeBool(isCurrentLocationStatusNull)
            if (currentLocationStatus != null) {
                when (currentLocationStatus) {
                    is Status.Pending -> {
                        parcel.writeSerializable(StatusType.PENDING)
                    }
                    is Status.Success -> {
                        parcel.writeSerializable(StatusType.SUCCESS)
                        parcel.writeParcelable(currentLocationStatus.result, flags)
                    }
                    is Status.Failure -> {
                        parcel.writeSerializable(StatusType.FAILURE)
                        parcel.writeSerializable(currentLocationStatus.errorType)
                    }
                }
            }
        }

        private fun GeoState.writeLunchPlacesStatus(parcel: Parcel, flags: Int) {
            val isLunchPlacesStatusNull = lunchPlacesStatus == null
            parcel.writeBool(isLunchPlacesStatusNull)
            if (lunchPlacesStatus != null) {
                val writeArg = { parcel.writeParcelable(lunchPlacesStatus.arg, flags) }
                when (lunchPlacesStatus) {
                    is Status.Pending -> {
                        parcel.writeSerializable(StatusType.PENDING)
                        writeArg()
                    }
                    is Status.Success -> {
                        parcel.writeSerializable(StatusType.SUCCESS)
                        writeArg()
                        parcel.writeParcelableArray(lunchPlacesStatus.result.toTypedArray(), flags)
                    }
                    is Status.Failure -> {
                        parcel.writeSerializable(StatusType.FAILURE)
                        writeArg()
                        parcel.writeSerializable(lunchPlacesStatus.errorType)
                    }
                }
            }
        }

        override fun create(parcel: Parcel): GeoState {
            val currentLocationStatus = readCurrentLocationStatus(parcel)
            val lunchPlacesStatus = readLunchPlacesStatus(parcel)
            return GeoState(currentLocationStatus, lunchPlacesStatus)
        }

        private fun readCurrentLocationStatus(parcel: Parcel): Status<Unit, LocationSnapshot>? {
            val isCurrentLocationStatusNull = parcel.readBool()
            if (isCurrentLocationStatusNull) {
                return null
            } else {
                val statusType = parcel.readSerializable() as StatusType
                return when (statusType) {
                    StatusType.PENDING -> {
                        Status.Pending(Unit)
                    }
                    StatusType.SUCCESS -> {
                        val classLoader = LocationSnapshot::class.java.classLoader
                        val result = parcel.readParcelable<LocationSnapshot>(classLoader)!!
                        Status.Success(Unit, result)
                    }
                    StatusType.FAILURE -> {
                        val errorType = parcel.readSerializable() as ErrorType
                        Status.Failure(Unit, errorType)
                    }
                }
            }
        }

        private fun readLunchPlacesStatus(parcel: Parcel): Status<SearchFilter, List<LunchPlace>>? {
            val isLunchPlacesStatusNull = parcel.readBool()
            if (isLunchPlacesStatusNull) {
                return null
            } else {
                val statusType = parcel.readSerializable() as StatusType
                val arg = run {
                    val classLoader = SearchFilter::class.java.classLoader
                    parcel.readParcelable<SearchFilter>(classLoader)!!
                }
                return when (statusType) {
                    StatusType.PENDING -> {
                        Status.Pending(arg)
                    }
                    StatusType.SUCCESS -> {
                        val result = run {
                            val classLoader = LunchPlace::class.java.classLoader
                            parcel.readParcelableArray(classLoader)!!
                                .map { it as LunchPlace }
                                .toList()
                        }
                        Status.Success(arg, result)
                    }
                    StatusType.FAILURE -> {
                        val errorType = parcel.readSerializable() as ErrorType
                        Status.Failure(arg, errorType)
                    }
                }
            }
        }
    }
}

private enum class StatusType {
    PENDING,
    SUCCESS,
    FAILURE
}

val Status<SearchFilter, List<LunchPlace>>?.isFailureDueToLocationPermissions: Boolean
    get() = (this is Status.Failure && errorType == ErrorType.LOCATION_PERMISSIONS)