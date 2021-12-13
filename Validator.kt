package com.fiserv.dps.mobile.sdk.utils

import android.graphics.Bitmap
import com.fiserv.dps.mobile.sdk.utils.Constants.Companion.EMAIL_PATTERN
import com.fiserv.dps.mobile.sdk.utils.Constants.Companion.PHONE_PATTERN
import com.fiserv.dps.mobile.sdk.utils.Constants.Companion.PHONE_PATTERN_TWO
import org.json.JSONObject

/**
 * It helps to validate name, email and phone
 * Created by F5SP0MG on 22,June,2021
 */
object Validator {

    /**
     * This function will validate phone number and email address
     * @param constants
     * @return Boolean
     */
    fun validateContact(constants: JSONObject):Boolean{
        if(validateName(constants.get("name").toString())){
            if(constants.has("phone")){
                if(validateMobileNumber(constants.getString("phone")))
                    return true
            }else if (constants.has("email")){
                if(validateEmail(constants.getString("email")))
                    return true
            }
        }
        return false
    }

    /**
     * this function will validate phone number
     * @param number1
     * @return Boolean
     */
    fun validateMobileNumber(number1 : String) : Boolean {

        if (!number1.matches(PHONE_PATTERN_TWO.toRegex())) {
            return false
        }
        var  number = number1.filter { it.isDigit() }

        if (number.length == 11 && number.startsWith("1")) {
            number=number.drop(1)
            return (number.matches(PHONE_PATTERN.toRegex()) )
        } else if (number.length == 10) {
            return (number.matches(PHONE_PATTERN.toRegex()))
        }
        return false
    }

    /**
     * This function will validate name
     * @param name
     * @return Boolean
     */
    fun validateName(name : String) : Boolean {
        return (name.trim().isNotEmpty() && (name.length in 2..30))
    }

    /**
     * This function will validate email id
     * @param email
     * @return Boolean
     */
    fun validateEmail(email : String) : Boolean {
        return (email.matches(EMAIL_PATTERN.toRegex()))
    }

    fun validateBitmap(bitmap: Bitmap?) : Boolean {
        return bitmap != null
    }
}