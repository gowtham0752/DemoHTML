package com.fiserv.dps.mobile.sdk.handlers

import android.content.ContentResolver
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import com.fiserv.dps.mobile.sdk.bridge.controller.BridgeFragment
import com.fiserv.dps.mobile.sdk.utils.Constants
import com.fiserv.dps.mobile.sdk.utils.ThresholdLimitPreference
import com.fiserv.dps.mobile.sdk.utils.TimePickerUtil
import com.fiserv.dps.mobile.sdk.utils.Validator
import org.json.JSONArray
import org.json.JSONObject

/**
 * ContactsThreadHandler created to get all the contacts in a background thread
 * Created by F6W0W4F on 10,December,2021
 */
class ContactsThreadHandler(var fragment: BridgeFragment, var evaluateJS: (String) -> Unit) : Runnable {
    private val jsonObject = JSONObject()

    /**
     * This run function will create new thread to get all contacts from mobile and send it to ui
     */
    override fun run() {
        getContactList()
    }

    /**
     * getContactList function will return the entire contact using content resolver
     * This function will return name, phone and email after validation
     * Finally all the results are sent to ui
     * Created by F5SP0MG on 01,July,2021
     */
    fun getContactList(): JSONObject {
        val cr: ContentResolver = fragment.requireContext().contentResolver
        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        val jArray = JSONArray()
        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
                val cursorPhone: Cursor = fragment.requireContext().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id),
                    null
                )!!
                var tempPhone = ""
                while (cursorPhone.moveToNext()) {
                    val phone =
                        cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                    //  if (Validator.validateMobileNumber(phone)) {
                    val number = phone.filter { it.isDigit() }
                    if (Validator.validateName(name) && tempPhone != number) {
                        tempPhone = number
                        val jObject = JSONObject()
                        jObject.put("name", name)
                        jObject.put("phone", number)
                        jObject.put("tokenType", "phone")
                        jArray.put(jObject)
                    }
                    //   }
                }
                cursorPhone.close()
                val cursor: Cursor = fragment.requireContext().contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", arrayOf(id),
                    null
                )!!
                var tempEmail = ""
                while (cursor.moveToNext()) {
                    val email =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))

                    //  if (Validator.validateEmail(email)) {
                    if (Validator.validateName(name) && email.lowercase() != tempEmail.lowercase()) {
                        tempEmail = email
                        val jObject = JSONObject()
                        jObject.put("name", name)
                        jObject.put("email", email.lowercase())
                        jObject.put("tokenType", "email")
                        jArray.put(jObject)
                    }
                    //  }
                }
                cursor.close()
            }
        }
        cur?.close()

        /**
         * This loop will remove duplicate contacts
         */
        for (i in 0 until jArray.length()-1) {
            for (j in i + 1 until jArray.length()-1) {

                if (jArray.get(i).toString() == jArray.get(j).toString()) {
                    jArray.remove(j)
                }
            }
        }
        val js = "window.localStorage.setItem('contact','$jArray');"

        Handler(Looper.getMainLooper()).postDelayed({
            ThresholdLimitPreference.save(Constants.CACHING_TIME, TimePickerUtil.getCurrentTime(), fragment.requireContext())
            evaluateJS(js)
            Log.e("contact stored in the local storage", "------------------------>${jArray}")
        }, 1000)

        jsonObject.put("cached", true)
        Handler(Looper.getMainLooper()).postDelayed({
            evaluateJS("callbackContacts({contact: '${jsonObject}'})")
        }, 1000)
        return jsonObject
    }
}