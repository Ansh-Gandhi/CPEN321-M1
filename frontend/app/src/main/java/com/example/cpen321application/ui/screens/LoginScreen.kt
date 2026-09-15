package com.example.cpen321application.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.cpen321application.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private const val TAG = "LoginScreen"

/**
 * Login screen with Google Sign-In using Android Credential Manager
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (userName: String, userEmail: String, givenName: String?, familyName: String?, idToken: String?) -> Unit,
    onNavigateToButton2: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val clientId = BuildConfig.GOOGLE_CLIENT_ID

    fun triggerGoogleSignIn() {
        if (clientId.isBlank()) {
            errorMessage = "Google Client ID is missing in local.properties"
            return
        }

        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(clientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context = context, request = request)

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val email = googleIdTokenCredential.id
                    val displayName = googleIdTokenCredential.displayName ?: "Google User"
                    val givenName = googleIdTokenCredential.givenName
                    val familyName = googleIdTokenCredential.familyName
                    val idToken = googleIdTokenCredential.idToken

                    Log.d(TAG, "Google Sign-In successful for $email ($displayName)")
                    isLoading = false
                    onLoginSuccess(displayName, email, givenName, familyName, idToken)
                } else {
                    isLoading = false
                    errorMessage = "Received unexpected credential type"
                }
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google Sign-In failed", e)
                isLoading = false
                errorMessage = "Sign-In canceled or failed: ${e.message}"
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In error", e)
                isLoading = false
                errorMessage = "Error: ${e.message}"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 24.dp))
            Text(
                text = "Opening Google Sign-In...",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            // Button 1: Google OAuth Sign-In
            Button(
                onClick = { triggerGoogleSignIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Button 2: Direct action button on main page
            OutlinedButton(
                onClick = onNavigateToButton2,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Live Pixel Art Stream",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        onLoginSuccess("Test User", "testuser@gmail.com", "Test", "User", "dummy_token")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Demo / Bypass Sign-In (Dev Mode)")
                }
            }
        }
    }
}
