package com.example.apidemo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


class UserRemoteDataSource(val context: Context) {

    val userCreation=MutableLiveData<ApiResponse>()
    val userDeletion=MutableLiveData<ApiResponse>()
    val userUpdate=MutableLiveData<ApiResponse>()
    private val TAG = "UserRemoteDataSource"
    private val appId = "637296ed244877538ce3ba88"

    var isOnline: Boolean? = null
    var retrofit = Retrofit.Builder()
        .baseUrl("https://dummyapi.io/data/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var userRetrofit: UserRetrofit = retrofit.create(UserRetrofit::class.java)

    @RequiresApi(Build.VERSION_CODES.M)
    fun createUser(user: User) {
        isOnline = checkInternetConnection()
        if (!isOnline!!) {
            userCreation.value=FailureResponse(0,null)
            return
        }
        userRetrofit.createUser(user, appId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {

                Log.e(TAG, "response Code: ${response.code()}")
                Log.e(TAG, "response body: ${response.body().toString()}")

                if (response.isSuccessful)
                    userCreation.value= SuccessfulResponse(response.code(), response.body()!!)
                 else {
                    val errorBody: String? = response.errorBody()?.string()
                    val errorModel = parseErrorResponse(errorBody!!)
                    userCreation.value = FailureResponse(response.code(), errorModel)

                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {

                Log.e(TAG, "onFailure: ${t.message}")
                userCreation.value=FailureResponse(0, ErrorModel(t.message!!, null))
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun updateUser(id: String, user: User) {
        isOnline = checkInternetConnection()
        if (!isOnline!!) {
            userUpdate.value=FailureResponse(0, null)
            return
        }
        var apiResponse: ApiResponse? = null
        userRetrofit.updateUser(id, user, appId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                Log.e(TAG, "onResponse code: ${response.code()}")
                apiResponse = if (response.isSuccessful)
                    SuccessfulResponse(response.code(), response.body()!!)
                else {
                    val errorModel = parseErrorResponse(response.errorBody()?.string()!!)
                    FailureResponse(response.code(), errorModel)
                }
                userUpdate.value=apiResponse!!
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                var response = FailureResponse(-1, ErrorModel(t.message!!, null))
                userUpdate.value=response
            }
        })
    }

    fun deleteUser(userId: String) {
        isOnline = checkInternetConnection()
        if (!isOnline!!) {
           userDeletion.value=FailureResponse(0, null)
            return
        }
        userRetrofit.deleteUser(userId, appId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var apiResponse: ApiResponse? = null
                if (response.isSuccessful) {
                    apiResponse = ApiResponse()
                    apiResponse.isSuccessful = true
                    apiResponse.statusCode = response.code()
                    userDeletion.value=apiResponse
                } else {
                    val errorMessage = response.errorBody()?.string()!!
                    val errorModel = parseErrorResponse(errorMessage)
                    apiResponse = FailureResponse(response.code(), errorModel)
                    userDeletion.value=apiResponse
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                userDeletion.value=FailureResponse(0, ErrorModel(t.message!!, null))
            }
        })
    }

    private fun parseErrorResponse(errorBody: String): ErrorModel {

        val root = JSONObject(errorBody)
        val error = root.getString("error")
        var fields = HashMap<String, String>()
        if (root.has("data")) {
            val data = root.getJSONObject("data")
            data.keys().iterator().forEach {
                fields.put(it, data.getString(it))
            }
        }
        return ErrorModel(error, fields)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkInternetConnection(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}

public interface UserRetrofit {

    @POST("user/create")
    fun createUser(@Body user: User, @Header("app-id") appId: String): Call<User>

    @PUT("user/{id}")
    fun updateUser(
        @Path("id") id: String,
        @Body user: User,
        @Header("app-id") appId: String
    ): Call<User>

    @DELETE("user/{id}")
    fun deleteUser(@Path("id") id: String, @Header("app-id") appId: String): Call<ResponseBody>
}