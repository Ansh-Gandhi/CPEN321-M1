package com.example.cpen321application.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen321application.repository.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class Button1State(
    val isLoggedIn: Boolean = false,
    val googleUserName: String? = null,
    val googleUserFirstName: String? = null,
    val googleUserLastName: String? = null,
    val googleUserEmail: String? = null,
    val serverIp: String? = null,
    val clientIp: String? = null,
    val serverTime: String? = null,
    val clientTime: String? = null,
    val myFirstName: String? = null,
    val myLastName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class Button1ViewModel(private val apiRepository: ApiRepository) : ViewModel() {
    private val _state = MutableStateFlow(Button1State())
    val state: StateFlow<Button1State> = _state

    fun setLoggedIn(
        userName: String,
        userEmail: String,
        givenName: String? = null,
        familyName: String? = null,
        idToken: String? = null
    ) {
        val parsedFirstName = givenName ?: userName.split(" ").firstOrNull() ?: userName
        val parsedLastName = familyName ?: userName.split(" ").drop(1).joinToString(" ").ifEmpty { "" }

        _state.value = _state.value.copy(
            isLoggedIn = true,
            googleUserName = userName,
            googleUserFirstName = parsedFirstName,
            googleUserLastName = parsedLastName,
            googleUserEmail = userEmail
        )
        fetchServerData()
    }

    private fun fetchServerData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Local device IP and time
            val clientIp = getClientIp()
            val clientTime = getFormattedClientTime()

            _state.value = _state.value.copy(
                clientIp = clientIp,
                clientTime = clientTime
            )

            val ipResult = apiRepository.getServerIp()
            val timeResult = apiRepository.getServerTime()
            val nameResult = apiRepository.getMyName()

            val serverIp = ipResult.getOrNull()?.ip
            val serverTime = timeResult.getOrNull()?.time
            val nameResponse = nameResult.getOrNull()

            val errorMsg = when {
                ipResult.isFailure -> "Failed to connect to backend server (${ipResult.exceptionOrNull()?.message ?: "Connection refused"}). Make sure the backend server is running on port 3000!"
                timeResult.isFailure -> "Failed to fetch server time (${timeResult.exceptionOrNull()?.message})."
                nameResult.isFailure -> "Failed to fetch developer name (${nameResult.exceptionOrNull()?.message})."
                else -> null
            }

            _state.value = _state.value.copy(
                serverIp = serverIp,
                serverTime = serverTime,
                myFirstName = nameResponse?.firstName,
                myLastName = nameResponse?.lastName,
                isLoading = false,
                error = errorMsg
            )
        }
    }

    private fun getClientIp(): String {
        return try {
            NetworkInterface.getNetworkInterfaces()
                .asSequence()
                .flatMap { it.inetAddresses.asSequence() }
                .filterIsInstance<Inet4Address>()
                .filter { !it.isLoopbackAddress && !it.hostAddress.startsWith("127.") }
                .map { it.hostAddress }
                .firstOrNull() ?: "127.0.0.1"
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }

    private fun getFormattedClientTime(): String {
        val now = Date()
        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        val timeStr = timeFormatter.format(now)

        val offsetMinutes = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (1000 * 60)
        val absOffsetMs = Math.abs(offsetMinutes)
        val offsetHours = absOffsetMs / 60
        val offsetMins = absOffsetMs % 60
        val sign = if (offsetMinutes >= 0) "+" else "-"

        return "$timeStr GMT$sign${String.format("%02d", offsetHours)}:${String.format("%02d", offsetMins)}"
    }

    fun logout() {
        _state.value = Button1State()
    }
}
