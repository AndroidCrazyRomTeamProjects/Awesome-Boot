## 2024-05-19 - Concurrent Coroutines
**Learning:** `async { }` and `.await()` can be used to run multiple network requests concurrently in Kotlin coroutines, rather than running them sequentially, significantly speeding up load times.

**Action:** Whenever multiple independent requests are made in a coroutine block, always use `async` to parallelize them instead of running them sequentially.
