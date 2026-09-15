package com.example.cpen321application.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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

private const val TAG = "MainHomeScreen"

/**
 * Main Home Dashboard Page containing action buttons for all features
 */
@Composable
fun MainHomeScreen(
    button1State: Button1State,
    onLoginSuccess: (userName: String, userEmail: String, givenName: String?, familyName: String?, idToken: String?) -> Unit,
    onNavigateToServerInfo: () -> Unit,
    onNavigateToPixelArt: () -> Unit,
    onNavigateToTimerTrivia: () -> Unit,
    onLogout: () -> Unit,
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
                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(clientId).build()
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
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Feature Card 1: Google OAuth & Server Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Button 1: Server & Google Auth",
                    style = MaterialTheme.typography.titleLarge
                )

                if (button1State.isLoggedIn) {
                    Text(
                        text = "Logged in as ${button1State.googleUserName ?: "Google User"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToServerInfo,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("View Server Info")
                        }
                        OutlinedButton(
                            onClick = onLogout
                        ) {
                            Text("Sign Out")
                        }
                    }
                } else {
                    Text(
                        text = "Authenticate via Google OAuth to view server & client IP and time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = { triggerGoogleSignIn() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Sign in with Google")
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )

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

        // Feature Card 2: Live Pixel Art Canvas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Button 2: Live Pixel Art Stream",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Watch 16x16 pixel art assemble live in real time from WebSocket stream.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onNavigateToPixelArt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Open Live Pixel Stream")
                }
            }
        }

        // Feature Card 3: User-Defined Timer & Interactive Surprise Trivia Challenge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Button 3: Timer & Interactive Surprise",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Set a custom countdown timer. When it expires, enjoy an interactive Trivia Surprise game!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onNavigateToTimerTrivia,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Open Timer & Trivia Surprise")
                }
            }
        }
    }
}
