package com.example.apidemo

data class ErrorModel(
    val error: String,
    var fields :HashMap<String,String>?
)