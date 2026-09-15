package com.example.cpen321application.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen321application.network.TriviaResponse
import com.example.cpen321application.repository.ApiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimerTriviaState(
    val minutesInput: String = "0",
    val secondsInput: String = "5",
    val remainingSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val isTimerFinished: Boolean = false,
    val triviaQuestion: TriviaResponse? = null,
    val shuffledAnswers: List<String> = emptyList(),
    val selectedAnswer: String? = null,
    val isAnswerCorrect: Boolean? = null,
    val score: Int = 0,
    val isLoadingTrivia: Boolean = false,
    val error: String? = null
)

class Button3ViewModel(private val apiRepository: ApiRepository) : ViewModel() {
    private val _state = MutableStateFlow(TimerTriviaState())
    val state: StateFlow<TimerTriviaState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun setMinutesInput(minutes: String) {
        if (minutes.isEmpty() || minutes.all { it.isDigit() }) {
            _state.value = _state.value.copy(minutesInput = minutes, error = null)
        }
    }

    fun setSecondsInput(seconds: String) {
        if (seconds.isEmpty() || seconds.all { it.isDigit() }) {
            _state.value = _state.value.copy(secondsInput = seconds, error = null)
        }
    }

    fun startTimer() {
        timerJob?.cancel()

        val mins = _state.value.minutesInput.toIntOrNull() ?: 0
        val secs = _state.value.secondsInput.toIntOrNull() ?: 0
        val totalSecs = (mins * 60) + secs

        if (totalSecs <= 0) {
            _state.value = _state.value.copy(error = "Please enter a duration greater than 0 seconds.")
            return
        }

        _state.value = _state.value.copy(
            remainingSeconds = totalSecs,
            isTimerRunning = true,
            isTimerFinished = false,
            triviaQuestion = null,
            selectedAnswer = null,
            isAnswerCorrect = null,
            error = null
        )

        timerJob = viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 && _state.value.isTimerRunning) {
                delay(1000L)
                val newRemaining = _state.value.remainingSeconds - 1
                _state.value = _state.value.copy(remainingSeconds = newRemaining)
            }

            if (_state.value.remainingSeconds == 0) {
                _state.value = _state.value.copy(
                    isTimerRunning = false,
                    isTimerFinished = true
                )
                fetchTriviaQuestion()
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _state.value = _state.value.copy(isTimerRunning = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        _state.value = _state.value.copy(
            remainingSeconds = 0,
            isTimerRunning = false,
            isTimerFinished = false,
            triviaQuestion = null,
            selectedAnswer = null,
            isAnswerCorrect = null,
            error = null
        )
    }

    fun fetchTriviaQuestion() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoadingTrivia = true,
                selectedAnswer = null,
                isAnswerCorrect = null,
                error = null
            )

            val result = apiRepository.getTriviaQuestion()
            val question = result.getOrNull()

            if (question != null) {
                val answers = (question.incorrectAnswers + question.correctAnswer).shuffled()
                _state.value = _state.value.copy(
                    triviaQuestion = question,
                    shuffledAnswers = answers,
                    isLoadingTrivia = false
                )
            } else {
                _state.value = _state.value.copy(
                    isLoadingTrivia = false,
                    error = "Failed to load trivia question: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun selectAnswer(answer: String) {
        val question = _state.value.triviaQuestion ?: return
        if (_state.value.selectedAnswer != null) return // Prevent changing answer after selection

        val isCorrect = (answer == question.correctAnswer)
        val newScore = if (isCorrect) _state.value.score + 1 else _state.value.score

        _state.value = _state.value.copy(
            selectedAnswer = answer,
            isAnswerCorrect = isCorrect,
            score = newScore
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
