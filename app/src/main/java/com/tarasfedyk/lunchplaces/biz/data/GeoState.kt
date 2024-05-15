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
                val onWriteId = { parcel.writeInt(currentLocationStatus.id) }
                when (currentLocationStatus) {
                    is Status.Pending -> {
                        parcel.writeSerializable(StatusType.PENDING)
                        onWriteId()
                    }
                    is Status.Success -> {
                        parcel.writeSerializable(StatusType.SUCCESS)
                        onWriteId()
                        parcel.writeParcelable(currentLocationStatus.result, flags)
                    }
                    is Status.Failure -> {
                        parcel.writeSerializable(StatusType.FAILURE)
                        onWriteId()
                        parcel.writeSerializable(currentLocationStatus.errorType)
                    }
                }
            }
        }

        private fun GeoState.writeLunchPlacesStatus(parcel: Parcel, flags: Int) {
            val isLunchPlacesStatusNull = lunchPlacesStatus == null
            parcel.writeBool(isLunchPlacesStatusNull)
            if (lunchPlacesStatus != null) {
                val onWriteId = { parcel.writeInt(lunchPlacesStatus.id) }
                val onWriteArg = { parcel.writeParcelable(lunchPlacesStatus.arg, flags) }
                when (lunchPlacesStatus) {
                    is Status.Pending -> {
                        parcel.writeSerializable(StatusType.PENDING)
                        onWriteId()
                        onWriteArg()
                    }
                    is Status.Success -> {
                        parcel.writeSerializable(StatusType.SUCCESS)
                        onWriteId()
                        onWriteArg()
                        parcel.writeParcelableArray(lunchPlacesStatus.result.toTypedArray(), flags)
                    }
                    is Status.Failure -> {
                        parcel.writeSerializable(StatusType.FAILURE)
                        onWriteId()
                        onWriteArg()
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
                val id = parcel.readInt()
                return when (statusType) {
                    StatusType.PENDING -> {
                        Status.Pending(id, Unit)
                    }
                    StatusType.SUCCESS -> {
                        val classLoader = LocationSnapshot::class.java.classLoader
                        val result = parcel.readParcelable<LocationSnapshot>(classLoader)!!
                        Status.Success(id, Unit, result)
                    }
                    StatusType.FAILURE -> {
                        val errorType = parcel.readSerializable() as ErrorType
                        Status.Failure(id, Unit, errorType)
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
                val id = parcel.readInt()
                val arg = run {
                    val classLoader = SearchFilter::class.java.classLoader
                    parcel.readParcelable<SearchFilter>(classLoader)!!
                }
                return when (statusType) {
                    StatusType.PENDING -> {
                        Status.Pending(id, arg)
                    }
                    StatusType.SUCCESS -> {
                        val result = run {
                            val classLoader = LunchPlace::class.java.classLoader
                            parcel.readParcelableArray(classLoader)!!
                                .map { it as LunchPlace }
                                .toList()
                        }
                        Status.Success(id, arg, result)
                    }
                    StatusType.FAILURE -> {
                        val errorType = parcel.readSerializable() as ErrorType
                        Status.Failure(id, arg, errorType)
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