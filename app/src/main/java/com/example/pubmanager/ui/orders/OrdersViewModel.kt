package com.example.pubmanager.ui.orders

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pubmanager.data.model.Order
import com.example.pubmanager.domain.mapper.toUi
import com.example.pubmanager.domain.repository.EmailRepository
import com.example.pubmanager.domain.repository.OrderRepository
import com.example.pubmanager.ui.emails.EmailConfig
import com.example.pubmanager.ui.emails.EmailUi
import com.example.pubmanager.ui.emails.SendEmailResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val emailRepository: EmailRepository
) : ViewModel() {
    private val MAILERSEND_URL = "https://api.mailersend.com/v1/email"
    private val client = OkHttpClient()
    private val _orders = MutableStateFlow<List<OrderUi>>(emptyList())
    val orders: StateFlow<List<OrderUi>> = _orders.asStateFlow()

    private val _emails = MutableStateFlow<List<EmailUi>>(emptyList())
    val emails: StateFlow<List<EmailUi>> = _emails.asStateFlow()

    fun loadOrders(eventId: Long) {
        viewModelScope.launch {
            val dbOrders = repository.getOrdersForEvent(eventId)
            _orders.value = dbOrders.map { it.toUi() }
        }
    }

    fun loadEmails() {
        viewModelScope.launch {
            val dbEmails = emailRepository.getAllEmails()
            _emails.value = dbEmails.map { it.toUi() }
        }
    }

    fun addToFamilyOrder(eventId: Long, familyId: Long, quantitiesToAdd: Map<Long, Int>) {
        viewModelScope.launch {
            val existing = repository
                .getOrdersForFamilyInEvent(eventId, familyId)
                .associate { it.productId to it.quantity }

            val merged: Map<Long, Int> =
                (existing.keys + quantitiesToAdd.keys).associateWith { productId ->
                    val oldQty = existing[productId] ?: 0
                    val addQty = quantitiesToAdd[productId] ?: 0
                    (oldQty + addQty).coerceAtLeast(0)
                }

            repository.deleteOrdersForFamilyInEvent(eventId, familyId)

            merged
                .filter { (_, qty) -> qty > 0 }
                .forEach { (productId, qty) ->
                    repository.insertOrder(
                        Order(
                            id = 0L,
                            eventId = eventId,
                            familyId = familyId,
                            productId = productId,
                            quantity = qty
                        )
                    )
                }

            loadOrders(eventId)
        }
    }

    fun setFamilyOrder(eventId: Long, familyId: Long, quantitiesSet: Map<Long, Int>) {
        viewModelScope.launch {
            repository.deleteOrdersForFamilyInEvent(eventId, familyId)

            quantitiesSet
                .filter { (_, qty) -> qty > 0 }
                .forEach { (productId, qty) ->
                    repository.insertOrder(
                        Order(
                            id = 0L,
                            eventId = eventId,
                            familyId = familyId,
                            productId = productId,
                            quantity = qty
                        )
                    )
                }

            loadOrders(eventId)
        }
    }

    fun deleteFamilyOrder(eventId: Long, familyId: Long) {
        viewModelScope.launch {
            repository.deleteOrdersForFamilyInEvent(eventId, familyId)
            loadOrders(eventId)
        }
    }

    suspend fun sendEmail(
        toEmails: List<String>,
        subject: String,
        text: String,
        html: String = text,
        attachmentName: String? = null,
        attachmentBytes: ByteArray? = null
    ): SendEmailResult = withContext(Dispatchers.IO) {

        val fromEmail = EmailConfig.FROM_EMAIL

        val apiKey = EmailConfig.MAILERSEND_API_KEY

        if (apiKey.isBlank()) {
            return@withContext SendEmailResult.UnknownError("API key is empty")
        }

        if (toEmails.isEmpty()) {
            return@withContext SendEmailResult.UnknownError("No recipients (toEmails is empty)")
        }

        val toJsonArray = toEmails.joinToString(",") { email ->
            """{ "email": "$email" }"""
        }

        val jsonBody = if (attachmentName != null && attachmentBytes != null) {
            val base64 = Base64.encodeToString(attachmentBytes, Base64.NO_WRAP)

            """
            {
              "from": { "email": "$fromEmail" },
              "to": [ $toJsonArray ],
              "subject": "$subject",
              "text": "$text",
              "html": "$html",
              "attachments": [
                {
                  "filename": "$attachmentName",
                  "content": "$base64",
                  "disposition": "attachment"
                }
              ]
            }
            """.trimIndent()
        } else {
            """
            {
              "from": { "email": "$fromEmail" },
              "to": [ $toJsonArray ],
              "subject": "$subject",
              "text": "$text",
              "html": "$html"
            }
            """.trimIndent()
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(MAILERSEND_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()
                return@withContext if (response.isSuccessful) {
                    SendEmailResult.Success
                } else {
                    SendEmailResult.HttpError(response.code, bodyString)
                }
            }
        } catch (e: UnknownHostException) {
            return@withContext SendEmailResult.NetworkError("No internet connection or DNS error")
        } catch (e: IOException) {
            return@withContext SendEmailResult.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            return@withContext SendEmailResult.UnknownError("Unexpected error: ${e.message}")
        }
    }

    fun sendEmailToSelected(
        toEmails: List<String>,
        excelBytes: ByteArray,
        attachmentName: String,
        subject: String,
        text: String,
        onDone: (String) -> Unit
    ) {
        viewModelScope.launch {
            val results = mutableListOf<String>()

            for (email in toEmails) {
                val res = sendEmail(
                    toEmails = listOf(email),
                    subject = subject,
                    text = text,
                    attachmentName = attachmentName,
                    attachmentBytes = excelBytes
                )

                val msg = when (res) {
                    is SendEmailResult.Success -> "✅ נשלח בהצלחה אל $email"
                    is SendEmailResult.NetworkError -> "❌ שגיאת רשת אל $email: ${res.message}"
                    is SendEmailResult.HttpError -> "❌ שגיאת שרת אל $email: code=${res.code}"
                    is SendEmailResult.UnknownError -> "❌ שגיאה לא ידועה אל $email: ${res.message}"
                }

                results.add(msg)
            }

            onDone(results.joinToString("\n"))
        }
    }
}
