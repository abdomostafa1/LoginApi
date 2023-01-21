package com.example.apidemo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
){
    @PrimaryKey
    var id="0"
}
