## 2024-03-22 - Replacing GET with HEAD for API Exists Checks
**Learning:** Using a GET request to check if a file exists (especially a media file like .qmg) fetches the entire file body. For large files, this is a massive performance bottleneck and wastes bandwidth. Replacing it with a HEAD request allows the application to check the HTTP status code (200 OK or 404 Not Found) without downloading the file body, significantly speeding up file existence checks and saving bandwidth.
**Action:** When creating methods to verify the existence of a remote resource using Retrofit, always use `@HEAD` instead of `@GET`.

## 2024-05-28 - Optimizing tight loops in image decoding
**Learning:** In very tight pixel-processing loops (e.g., converting QMG colors like RGB565 to ARGB8888), standard arithmetic operations like integer multiplication/division (`* 255 / 31`) are measurably slower than equivalent bitwise scaling operations (`(r << 3) | (r >> 2)`). Additionally, using `System.arraycopy` for copying very small arrays (e.g., 2 bytes at a time) introduces significant JNI/method call overhead, making manual indexing much faster.
**Action:** When writing high-performance pixel conversion code in Kotlin/Java, always prefer bitwise operators over mult/div for scaling values, hoist conditionals (like null-checks for alpha arrays) outside of the pixel-iteration loop, and manually assign values instead of using array copy methods when the byte size per pixel is very small.

## 2024-11-20 - Optimizing sequential API calls with concurrent execution
**Learning:** When performing multiple independent API or file-existence checks before updating the UI (e.g., checking if several QMG files exist to determine which previews to show), doing them sequentially with `await` or regular `suspend` calls forces the network latency of each to stack up. Launching them concurrently using `async { ... }` reduces total wait time to only the latency of the slowest request.
**Action:** When making multiple independent network requests within a coroutine, wrap them in `async` blocks and `await()` their results collectively rather than running them sequentially.
