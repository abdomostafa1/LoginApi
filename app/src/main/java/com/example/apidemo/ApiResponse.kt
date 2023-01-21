package com.example.apidemo
import android.util.Log
import org.json.JSONObject

open class ApiResponse {
    var isSuccessful: Boolean? = null
    var statusCode: Int? = null

    companion object{
        val NO_INTERNET="No Internet Connection"
        val CONNECTION_TIMEOUT="No Internet Connection"
        val NOT_FOUND="Resource not found"
        val UNAUTHORIZED="you aren't authorized to do that"
        val SERVICE_UNAVAILABLE ="you aren't authorized to do that"
        val INTERNEL_SERVER_ERROR="Sorry Internal Server Error\ntry again later"
    }
}

class SuccessfulResponse : ApiResponse {
    var body: User

    constructor(code: Int, body: User) {
        this.statusCode = code
        this.body = body
        isSuccessful = true
    }

}

class FailureResponse : ApiResponse {
    lateinit var errorMessage: String

    constructor( code: Int = 0, errorModel: ErrorModel?) {
        this.statusCode = code
        isSuccessful = false
        setErrorMessage(errorModel)
    }

    @JvmName("setErrorMessage1")
    private fun setErrorMessage(errorModel: ErrorModel?) {

        errorMessage=when(statusCode){

            0-> NO_INTERNET
            404-> NOT_FOUND
            408-> CONNECTION_TIMEOUT
            500-> INTERNEL_SERVER_ERROR
            503-> SERVICE_UNAVAILABLE
            else-> handleResponseError(errorModel!!)
        }

    }

    private fun handleResponseError(errorModel: ErrorModel):String{

        return when (errorModel.error) {
            "APP_ID_MISSING" -> "App Id is Missing"
            "APP_ID_NOT_EXIST" -> "App ID is not correct"
            "PARAMS_NOT_VALID" -> "Id is not correct"
            "PATH_NOT_FOUND" -> "Request path is not valid"
            "BODY_NOT_VALID" -> getBodyError(errorModel.fields!!)
            else -> {
                "unexpected error occurred \ncode is $statusCode"
            }
        }
    }

    private fun getBodyError(fields:HashMap<String,String>): String {

        var errorMessage = ""
        fields.forEach { s, s2 ->
            Log.e(s, "$s2")
            errorMessage+=s2+"\n"
        }
        return errorMessage
    }
}
