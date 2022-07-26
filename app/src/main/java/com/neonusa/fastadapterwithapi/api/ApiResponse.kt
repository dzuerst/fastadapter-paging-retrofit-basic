package com.neonusa.fastadapterwithapi.api

import com.google.gson.annotations.SerializedName
import com.neonusa.fastadapterwithapi.model.CharacterData

//todo : wrap with the response

data class ApiResponse(
    val results: List<CharacterData>,
    val info: Info
)

data class Info(val count: Int?, val pages: String?, val next: String?,val prev: String?)

