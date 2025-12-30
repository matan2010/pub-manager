package com.example.pubmanager.ui.emails

sealed class SendEmailResult {
    object Success : SendEmailResult()
    data class NetworkError(val message: String) : SendEmailResult()
    data class HttpError(val code: Int, val body: String?) : SendEmailResult()
    data class UnknownError(val message: String) : SendEmailResult()
}