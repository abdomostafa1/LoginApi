package com.example.apidemo

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.apidemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var remoteDatabase : UserRemoteDataSource
    lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        remoteDatabase=UserRemoteDataSource(this)
        //var requestBody=RequestBody.create
        binding.create.setOnClickListener {

            val user = getUserData()
            remoteDatabase.createUser(user)
        }

        remoteDatabase.userCreation.observe(this) {
            if (it.isSuccessful == true) {
                val newUser = (it as SuccessfulResponse).body
                showUserData(newUser)
            } else {
                val errorMessage = (it as FailureResponse).errorMessage
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                Log.e(TAG, "errorMessage: $errorMessage")
            }

        }
        binding.update.setOnClickListener {
           val user=getUserData()
            remoteDatabase.updateUser(user.id, user)
        }

        remoteDatabase.userUpdate.observe(this){

                if (it.isSuccessful == true) {
                    val response = it as SuccessfulResponse
                    val user = response.body
                    showUserData(user)
                    Log.e(TAG, "user id: ${user.id}")

                } else {
                    val response = it as FailureResponse
                    val errorMessage = response.errorMessage
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
                Log.e(TAG, "Response code: ${it.statusCode}")

        }

        binding.delete.setOnClickListener {
            val id = binding.edtId.text.toString()
            Log.e(TAG, "id: $id")
            remoteDatabase.deleteUser(id)
        }
        remoteDatabase.userDeletion.observe(this){
            if (it.isSuccessful == true) {
                Toast.makeText(this, "User is deleted Successfully", Toast.LENGTH_LONG).show()
            } else {
                val response = it as FailureResponse
                val errorMessage = response.errorMessage
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                Log.e(TAG, "onCreate: ${response.statusCode}")
            }
        }

    }

    private fun getUserData(): User {
        val id = binding.edtId.text.toString()
        val firstName = binding.firstName.text.toString()
        val lastName = binding.lastName.text.toString()
        val email = binding.email.text.toString()

        val user=User( firstName, lastName, email)
        user.id=id
        Log.e(TAG, "user id: $id")
        return user
    }

    private fun showUserData(newUser: User) {
        binding.firstName1.text = newUser.firstName
        binding.lastName2.text = newUser.lastName

        binding.email3.text = newUser.email
        Log.e(TAG, "user id : ${newUser.id}")
        binding.edtId.setText(newUser.id)
    }


}