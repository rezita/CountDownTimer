package io.github.kotlin.timer

import com.github.rezita.CountDownTimer
import com.github.rezita.TimerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class CountDownTimerTest {

    @Test
    fun timer_init_state_idle() {
        val timer = CountDownTimer(3000L, 1000L)
        assertEquals(TimerState.Idle, timer.state.value)
    }

    @Test
    fun timer_init_with_invalid_values_throws_exception() {
        assertFailsWith<IllegalArgumentException> {
            CountDownTimer(0L, 1000L)
        }
        assertFailsWith<IllegalArgumentException> {
            CountDownTimer(1000L, 0L)
        }
        assertFailsWith<IllegalArgumentException> {
            CountDownTimer(-100L, 1000L)
        }
    }

    @Test
    fun timer_counts_down_correctly() = runTest {
        val timer = CountDownTimer(3000L, 1000L)
        val states = mutableListOf<TimerState>()

        val job = launch {
            timer.state.collect { states.add(it) }
        }
        timer.start(this)
        advanceTimeBy(3000)
        runCurrent()

        assertEquals(
            listOf(
                TimerState.Running(3000),
                TimerState.Running(2000),
                TimerState.Running(1000),
                TimerState.Finished
            ),
            states
        )
        job.cancel()
    }

    @Test
    fun timer_pause_stops_countdown() = runTest {
        val timer = CountDownTimer(3000L, 1000L)
        val states = mutableListOf<TimerState>()

        val job = launch {
            timer.state.collect { states.add(it) }
        }
        timer.start(this)
        advanceTimeBy(1000) // 2000 remaining
        runCurrent()
        timer.pause()
        runCurrent()

        assertEquals(TimerState.Paused(2000), states.last())
        
        advanceTimeBy(2000)
        runCurrent()
        assertEquals(TimerState.Paused(2000), states.last(), "Timer should remain paused")
        
        job.cancel()
    }

    @Test
    fun timer_resume_continues_countdown() = runTest {
        val timer = CountDownTimer(3000L, 1000L)
        val states = mutableListOf<TimerState>()

        val job = launch {
            timer.state.collect { states.add(it) }
        }
        timer.start(this)
        advanceTimeBy(1000) // 2000 remaining
        runCurrent()
        timer.pause()
        runCurrent()
        
        timer.resume(this)
        runCurrent()
        assertEquals(TimerState.Running(2000), states.last())
        
        advanceTimeBy(1000) // 1000 remaining
        runCurrent()
        assertEquals(TimerState.Running(1000), states.last())
        
        job.cancel()
    }

    @Test
    fun timer_stop_resets_to_idle() = runTest {
        val timer = CountDownTimer(3000L, 1000L)
        timer.start(this)
        advanceTimeBy(1000)
        runCurrent()
        
        timer.stop()
        runCurrent()
        
        assertEquals(TimerState.Idle, timer.state.value)
    }

    @Test
    fun timer_restart_resets_and_starts_again() = runTest {
        val timer = CountDownTimer(3000L, 1000L)
        val states = mutableListOf<TimerState>()
        val job = launch { timer.state.collect { states.add(it) } }

        timer.start(this)
        advanceTimeBy(1000) // 2000 remaining
        runCurrent()
        
        timer.restart(this)
        runCurrent()
        
        assertEquals(TimerState.Running(3000L), states.last())
        
        advanceTimeBy(3000)
        runCurrent()
        assertEquals(TimerState.Finished, states.last())
        job.cancel()
    }

    @Test
    fun timer_start_while_running_is_ignored() = runTest {
        val timer = CountDownTimer(3000L, 1000L)
        val states = mutableListOf<TimerState>()
        val job = launch { timer.state.collect { states.add(it) } }

        timer.start(this)
        advanceTimeBy(1000)
        runCurrent()

        val sizeBefore = states.size
        timer.start(this)
        runCurrent()

        assertEquals(sizeBefore, states.size, "Subsequent start calls while running should be ignored")
        job.cancel()
    }

    @Test
    fun timer_resume_after_stop_is_ignored() = runTest {
        val timer = CountDownTimer(3000L, 1000L)
        timer.start(this)
        advanceTimeBy(1000)
        runCurrent()
        timer.pause()
        runCurrent()
        timer.stop()
        runCurrent()
        
        timer.resume(this)
        runCurrent()
        
        assertEquals(TimerState.Idle, timer.state.value, "Resume after stop should do nothing")
    }

    @Test
    fun timer_multiple_pause_resume_cycles() = runTest {
        val timer = CountDownTimer(3000L, 1000L)
        val states = mutableListOf<TimerState>()
        val job = launch { timer.state.collect { states.add(it) } }

        timer.start(this)
        advanceTimeBy(1000) // 2000
        runCurrent()
        timer.pause()
        runCurrent()
        timer.resume(this)
        runCurrent()
        advanceTimeBy(1000) // 1000
        runCurrent()
        timer.pause()
        runCurrent()
        timer.resume(this)
        runCurrent()
        advanceTimeBy(1000) // 0 -> Finished
        runCurrent()

        assertEquals(TimerState.Finished, states.last())
        job.cancel()
    }

    @Test
    fun timer_does_not_emit_negative_values() = runTest {
        val timer = CountDownTimer(1500L, 1000L)
        val states = mutableListOf<TimerState>()
        val job = launch { timer.state.collect { states.add(it) } }

        timer.start(this)
        advanceTimeBy(2000)
        runCurrent()

        val negativeStates = states.filterIsInstance<TimerState.Running>().filter { it.remainingMs < 0 }
        assertTrue(negativeStates.isEmpty(), "Timer should not emit negative remaining time: $negativeStates")
        assertEquals(TimerState.Finished, states.last())
        job.cancel()
    }
}
