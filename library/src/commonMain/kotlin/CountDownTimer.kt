package io.github.rezita

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CountDownTimer(
    private val totalTimeInMs: Long = DEFAULT_TOTAL_TIME_IN_MS,
    val intervalInMs: Long = DEFAULT_INTERVAL_IN_MS
) {
    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state

    private var job: Job? = null

    private var remainingMs: Long = totalTimeInMs
    private var isPaused = false

    init {
        require(totalTimeInMs > 0) { "Total time must be greater than zero" }
        require(intervalInMs > 0) { "Interval must be greater than zero" }
    }

    fun start(scope: CoroutineScope) {
        if (job?.isActive == true) return

        isPaused = false

        _state.value = TimerState.Running(remainingMs)

        job = scope.launch {
            try {
                while (remainingMs > 0) {

                    if (isPaused) {
                        _state.value = TimerState.Paused(remainingMs)
                        return@launch
                    }

                    delay(intervalInMs)
                    remainingMs = (remainingMs - intervalInMs).coerceAtLeast(0)
                    
                    if (remainingMs > 0) {
                        _state.value = TimerState.Running(remainingMs)
                    }
                }

                _state.value = TimerState.Finished
                resetInternal()

            } finally {
                job = null
            }
        }
    }


    fun pause() {
        if (job == null) return

        job?.cancel()
        job = null

        isPaused = true
        _state.value = TimerState.Paused(remainingMs)
    }

    fun resume(scope: CoroutineScope) {
        if (!isPaused) return

        isPaused = false
        start(scope)
    }

    fun stop() {
        job?.cancel()
        resetInternal()
        isPaused = false
        _state.value = TimerState.Idle
    }

    fun restart(scope: CoroutineScope) {
        stop()
        start(scope)
    }

    private fun resetInternal() {
        remainingMs = totalTimeInMs
    }

    companion object {
        private const val DEFAULT_TOTAL_TIME_IN_MS = 30000L //30 sec
        private const val DEFAULT_INTERVAL_IN_MS = 5000L //5 sec
    }
}
