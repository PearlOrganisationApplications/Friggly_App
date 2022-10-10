package com.rank.me.data.dto.login

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Saurabh, 27th sept 2022
 */

@Parcelize
data class LoginResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val streetName: String,
    val buildingNumber: String,
    val postalCode: String,
    val state: String,
    val countryCode: String,
    val email: String,
    val otp: String? = null,
    val isNewUser: Boolean? = false


) : Parcelable {
    override fun toString(): String {
        return "LoginResponse(id='$id', firstName='$firstName', lastName='$lastName', streetName='$streetName', buildingNumber='$buildingNumber', postalCode='$postalCode', state='$state', countryCode='$countryCode', email='$email', otp=$otp, isNewUser=$isNewUser)"
    }
}
