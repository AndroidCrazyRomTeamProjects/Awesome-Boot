## 2024-05-19 - Concurrent Coroutines
**Learning:** `async { }` and `.await()` can be used to run multiple network requests concurrently in Kotlin coroutines, rather than running them sequentially, significantly speeding up load times.

**Action:** Whenever multiple independent requests are made in a coroutine block, always use `async` to parallelize them instead of running them sequentially.

## 2024-05-19 - Concurrent File I/O
**Learning:** File I/O operations block and can be slow, especially when reading large animation files sequentially. `async { }` and `.await()` can be used to read multiple files concurrently, similarly to how multiple network requests can be fetched, reducing overall load times.

**Action:** Whenever multiple independent files are being read in a coroutine block, always use `async` to run the operations concurrently.

## 2026-03-20 - Single-pass Iteration Over Large Arrays
**Learning:** Iterating over large arrays multiple times (e.g., millions of pixels) for different operations like alpha extraction and color conversion introduces significant overhead due to duplicated loops and temporary array allocations, leading to increased latency and GC pressure.
**Action:** When performing multiple sequential transformations on elements of large arrays, combine them into a single-pass loop whenever possible to eliminate intermediate allocations and redundant O(N) iterations.
