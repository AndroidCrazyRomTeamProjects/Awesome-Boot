## 2024-05-19 - Concurrent Coroutines
**Learning:** `async { }` and `.await()` can be used to run multiple network requests concurrently in Kotlin coroutines, rather than running them sequentially, significantly speeding up load times.

**Action:** Whenever multiple independent requests are made in a coroutine block, always use `async` to parallelize them instead of running them sequentially.

## 2024-05-19 - Concurrent File I/O
**Learning:** File I/O operations block and can be slow, especially when reading large animation files sequentially. `async { }` and `.await()` can be used to read multiple files concurrently, similarly to how multiple network requests can be fetched, reducing overall load times.

**Action:** Whenever multiple independent files are being read in a coroutine block, always use `async` to run the operations concurrently.
