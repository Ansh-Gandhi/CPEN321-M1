package com.example.cpen321application.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Button 3 Screen: Timer & Interactive Trivia Surprise Challenge
 */
@Composable
fun TimerTriviaScreen(
    state: TimerTriviaState,
    onMinutesChanged: (String) -> Unit,
    onSecondsChanged: (String) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSelectAnswer: (String) -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Button 3: User-Defined Timer",
                    style = MaterialTheme.typography.titleLarge
                )

                // Time Input Fields (Minutes & Seconds)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.minutesInput,
                        onValueChange = onMinutesChanged,
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        enabled = !state.isTimerRunning
                    )

                    OutlinedTextField(
                        value = state.secondsInput,
                        onValueChange = onSecondsChanged,
                        label = { Text("Seconds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        enabled = !state.isTimerRunning
                    )
                }

                // Format countdown time as mm:ss
                val mins = state.remainingSeconds / 60
                val secs = state.remainingSeconds % 60
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

                // Countdown Timer Display
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (state.isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                // Timer Action Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.isTimerRunning) {
                        Button(
                            onClick = onPauseTimer,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) {
                            Text("Pause")
                        }
                    } else {
                        Button(
                            onClick = onStartTimer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start Timer")
                        }
                    }

                    OutlinedButton(
                        onClick = onResetTimer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset")
                    }
                }

                // Error message if input invalid
                if (state.error != null) {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Surprise Interactive Trivia Card (when timer finishes)
        if (state.isTimerFinished) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎉 Timer Finished! Surprise Trivia!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "Score: ${state.score}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider()

                    if (state.isLoadingTrivia) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Fetching surprise trivia from API...")
                            }
                        }
                    } else if (state.triviaQuestion != null) {
                        val question = state.triviaQuestion

                        // Badges: Category & Difficulty
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BadgeChip(text = question.category, backgroundColor = MaterialTheme.colorScheme.secondaryContainer)
                            BadgeChip(
                                text = question.difficulty.uppercase(),
                                backgroundColor = when (question.difficulty.lowercase()) {
                                    "easy" -> Color(0xFF4CAF50)
                                    "medium" -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                }
                            )
                        }

                        // Question Text
                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive 4-choice answer buttons
                        state.shuffledAnswers.forEach { answer ->
                            val isSelected = (state.selectedAnswer == answer)
                            val isCorrectAnswer = (answer == question.correctAnswer)

                            val buttonColor = when {
                                state.selectedAnswer == null -> MaterialTheme.colorScheme.surface
                                isCorrectAnswer -> Color(0xFF4CAF50) // Always highlight correct answer green
                                isSelected -> Color(0xFFF44336) // Highlight wrong selection red
                                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            }

                            val textColor = when {
                                state.selectedAnswer == null -> MaterialTheme.colorScheme.onSurface
                                isCorrectAnswer || isSelected -> Color.White
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }

                            Button(
                                onClick = { onSelectAnswer(answer) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonColor,
                                    contentColor = textColor
                                ),
                                enabled = state.selectedAnswer == null
                            ) {
                                Text(
                                    text = answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Result Feedback & Next Question
                        if (state.selectedAnswer != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.isAnswerCorrect == true) {
                                Text(
                                    text = "🎉 Correct! Great job!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "❌ Incorrect! The right answer was: ${question.correctAnswer}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onNextQuestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text("Next Trivia Question")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(text: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
