# CountDownTimer KMP

A lightweight, Coroutine-based CountDownTimer for Kotlin Multiplatform (Android, iOS, JVM, and Linux).

## Installation

Add the dependency to your `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.rezita:countdowntimer:1.0.0")
        }
    }
}
```

## Usage

```kotlin
val timer = CountDownTimer(
    durationMs = 30000L, 
    intervalMs = 1000L
)

// Collect state
scope.launch {
    timer.state.collect { state ->
        when (state) {
            is TimerState.Idle -> println("Idle")
            is TimerState.Running -> println("Remaining: ${state.remainingMs}ms")
            is TimerState.Paused -> println("Paused at: ${state.remainingMs}ms")
            is TimerState.Finished -> println("Finished!")
        }
    }
}

// Control the timer
timer.start(scope)
// timer.pause()
// timer.resume(scope)
// timer.stop()
//timer.restart(scope)
```

## License
Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
