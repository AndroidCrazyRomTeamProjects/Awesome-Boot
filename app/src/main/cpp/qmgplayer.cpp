#include <jni.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <dlfcn.h>
#include <android/log.h>

#define LOG_TAG "QMG_PLAYER"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

// QMG decoder functions from libQmageDecoder.so
// Using void* for opaque context handles as the exact structure depends on the library version.
typedef void* (*QmageDecCreateAniInfo_t)(void*, int, int);
typedef void  (*QmageDecDestroyAniInfo_t)(void*);
typedef int   (*QmageDecodeAniFrame_t)(void*, void*);

static void* qmgHandle = nullptr;
static QmageDecCreateAniInfo_t  QmageDecCreateAniInfo = nullptr;
static QmageDecDestroyAniInfo_t QmageDecDestroyAniInfo = nullptr;
static QmageDecodeAniFrame_t    QmageDecodeAniFrame = nullptr;

static bool loadQmgLibrary() {
    if (qmgHandle) return true;

    qmgHandle = dlopen("libQmageDecoder.so", RTLD_NOW);
    if (!qmgHandle) {
        LOGE("dlopen failed for libQmageDecoder.so: %s", dlerror());
        return false;
    }

    QmageDecCreateAniInfo = (QmageDecCreateAniInfo_t)dlsym(qmgHandle, "QmageDecCreateAniInfo");
    QmageDecDestroyAniInfo = (QmageDecDestroyAniInfo_t)dlsym(qmgHandle, "QmageDecDestroyAniInfo");
    QmageDecodeAniFrame = (QmageDecodeAniFrame_t)dlsym(qmgHandle, "QmageDecodeAniFrame");

    if (!QmageDecCreateAniInfo || !QmageDecodeAniFrame || !QmageDecDestroyAniInfo) {
        LOGE("dlsym failed for one or more functions in libQmageDecoder.so");
        return false;
    }
    LOGI("Successfully loaded libQmageDecoder.so");
    return true;
}

// Wrapper structure to safely manage both the decoder's handle and our source buffer.
// This prevents us from corrupting the internal state of the library's context.
struct QmgContext {
    void* ani;
    void* buffer;
    uint8_t* tempBuffer;
    int tempBufferSize;
};

JNIEXPORT jlong JNICALL
Java_org_crazyromteam_qmgstore_qmg_LibQmg_CreateAniInfo(
        JNIEnv* env,
        jobject,
        jbyteArray qmgData,
        jint flags) {
    if (!loadQmgLibrary() || !qmgData) return 0;

    jsize len = env->GetArrayLength(qmgData);
    LOGI("CreateAniInfo: len=%d, flags=%d", (int)len, (int)flags);

    // Allocate buffer with padding for safety.
    uint8_t* buffer = (uint8_t*)malloc(len + 0x20000);
    if (!buffer) return 0;
    memset(buffer, 0, len + 0x20000);
    env->GetByteArrayRegion(qmgData, 0, len, (jbyte*)buffer);

    // Signature is typically (buffer, flags, size).
    // Flags=1 for memory-based decoding.
    void* ani = QmageDecCreateAniInfo(buffer, (int)flags, (int)len);

    if (!ani) {
        LOGE("QmageDecCreateAniInfo returned NULL");
        free(buffer);
        return 0;
    }

    QmgContext* ctx = (QmgContext*)malloc(sizeof(QmgContext));
    ctx->ani = ani;
    ctx->buffer = buffer;
    ctx->tempBuffer = nullptr;
    ctx->tempBufferSize = 0;

    LOGI("CreateAniInfo success: ctx=%p, ani=%p", ctx, ani);
    return (jlong)ctx;
}

JNIEXPORT void JNICALL
Java_org_crazyromteam_qmgstore_qmg_LibQmg_DestroyAniInfo(
        JNIEnv*,
        jobject,
        jlong ctxPtr) {
    if (!ctxPtr) return;
    QmgContext* ctx = (QmgContext*)ctxPtr;

    LOGI("DestroyAniInfo start: ctx=%p, ani=%p", ctx, ctx->ani);

    if (ctx->ani) {
        // Destroy the library's context first.
        QmageDecDestroyAniInfo(ctx->ani);
    }

    if (ctx->buffer) {
        // Free our source buffer after the library is done with it.
        free(ctx->buffer);
    }

    if (ctx->tempBuffer) {
        free(ctx->tempBuffer);
    }

    free(ctx);
    LOGI("DestroyAniInfo completed");
}

JNIEXPORT jint JNICALL
Java_org_crazyromteam_qmgstore_qmg_LibQmg_DecodeAniFrame(
        JNIEnv* env,
        jobject,
        jlong ctxPtr,
        jbyteArray outBuf) {
    if (!ctxPtr || !outBuf || !loadQmgLibrary()) return -1;

    QmgContext* ctx = (QmgContext*)ctxPtr;
    uint8_t* out = (uint8_t*)env->GetByteArrayElements(outBuf, nullptr);

    int ret = QmageDecodeAniFrame(ctx->ani, out);

    env->ReleaseByteArrayElements(outBuf, (jbyte*)out, 0);
    return ret;
}

JNIEXPORT jint JNICALL
Java_org_crazyromteam_qmgstore_qmg_LibQmg_DecodeAniFrameNative(
        JNIEnv* env,
        jobject,
        jlong ctxPtr,
        jbyteArray outBuf,
        jint width,
        jint height,
        jint colorFormat) {
    if (!ctxPtr || !outBuf || !loadQmgLibrary()) return -1;

    QmgContext* ctx = (QmgContext*)ctxPtr;
    int pixelCount = width * height;

    // For ARGB8888 or RGBA8888 the max size we would ever need from QmageDecoder is pixelCount * 4
    int requiredSize = pixelCount * 4;

    if (!ctx->tempBuffer || ctx->tempBufferSize < requiredSize) {
        if (ctx->tempBuffer) free(ctx->tempBuffer);
        ctx->tempBuffer = (uint8_t*)malloc(requiredSize);
        ctx->tempBufferSize = requiredSize;
        if (!ctx->tempBuffer) return -1;
    }

    // Decode directly into our temporary buffer
    int ret = QmageDecodeAniFrame(ctx->ani, ctx->tempBuffer);
    if (ret < 0) return ret; // Decoding failed

    uint8_t* out = (uint8_t*)env->GetByteArrayElements(outBuf, nullptr);
    if (!out) return -1;

    uint8_t* srcBuf = ctx->tempBuffer;

    // Apply color conversion (mapping from Color.kt enum)
    // RGB565(0), RGB888(1), BGR888(2), RGB5658(3), RGB8565(4), ARGB8888(5), RGBA8888(6), BGRA8888(7)
    switch (colorFormat) {
        case 0: { // RGB565 -> ARGB8888
            int src = 0;
            int dst = 0;
            for (int i = 0; i < pixelCount; ++i) {
                int v0 = srcBuf[src] & 0xFF;
                int v1 = srcBuf[src + 1] & 0xFF;
                int value = (v1 << 8) | v0;
                int r = (value >> 11) & 0x1F;
                int g = (value >> 5) & 0x3F;
                int b = value & 0x1F;

                out[dst] = (uint8_t)((r << 3) | (r >> 2));
                out[dst + 1] = (uint8_t)((g << 2) | (g >> 4));
                out[dst + 2] = (uint8_t)((b << 3) | (b >> 2));
                out[dst + 3] = 0xFF; // Alpha 255

                dst += 4;
                src += 2;
            }
            break;
        }
        case 1: { // RGB888 -> ARGB8888
            int src = 0;
            int dst = 0;
            for (int i = 0; i < pixelCount; ++i) {
                out[dst] = srcBuf[src];
                out[dst + 1] = srcBuf[src + 1];
                out[dst + 2] = srcBuf[src + 2];
                out[dst + 3] = 0xFF;
                dst += 4;
                src += 3;
            }
            break;
        }
        case 2: { // BGR888 -> ARGB8888
            int src = 0;
            int dst = 0;
            for (int i = 0; i < pixelCount; ++i) {
                out[dst] = srcBuf[src + 2]; // R
                out[dst + 1] = srcBuf[src + 1]; // G
                out[dst + 2] = srcBuf[src]; // B
                out[dst + 3] = 0xFF;
                dst += 4;
                src += 3;
            }
            break;
        }
        case 3: { // RGB5658 (Alpha + RGB565) -> ARGB8888
            int src = 0;
            int dst = 0;
            // Kotlin says alphaFirst = false for RGB5658 (meaning Alpha is last)
            for (int i = 0; i < pixelCount; ++i) {
                int v0 = srcBuf[src] & 0xFF;
                int v1 = srcBuf[src + 1] & 0xFF;
                int rgbValue = (v1 << 8) | v0;
                uint8_t alpha = srcBuf[src + 2];

                int r = (rgbValue >> 11) & 0x1F;
                int g = (rgbValue >> 5) & 0x3F;
                int b = rgbValue & 0x1F;

                out[dst] = (uint8_t)((r << 3) | (r >> 2));
                out[dst + 1] = (uint8_t)((g << 2) | (g >> 4));
                out[dst + 2] = (uint8_t)((b << 3) | (b >> 2));
                out[dst + 3] = alpha;

                dst += 4;
                src += 3;
            }
            break;
        }
        case 4: { // RGB8565 (Alpha + RGB565 diff order) -> ARGB8888
            int src = 0;
            int dst = 0;
            // Kotlin says alphaFirst = true for RGB8565 (meaning Alpha is first)
            for (int i = 0; i < pixelCount; ++i) {
                uint8_t alpha = srcBuf[src];
                int v1 = srcBuf[src + 1] & 0xFF;
                int v2 = srcBuf[src + 2] & 0xFF;
                int rgbValue = (v2 << 8) | v1;

                int r = (rgbValue >> 11) & 0x1F;
                int g = (rgbValue >> 5) & 0x3F;
                int b = rgbValue & 0x1F;

                out[dst] = (uint8_t)((r << 3) | (r >> 2));
                out[dst + 1] = (uint8_t)((g << 2) | (g >> 4));
                out[dst + 2] = (uint8_t)((b << 3) | (b >> 2));
                out[dst + 3] = alpha;

                dst += 4;
                src += 3;
            }
            break;
        }
        case 5: { // ARGB8888 -> ARGB8888 (Kotlin handles this by rearranging A to end or just copies? Kotlin has A R G B -> R G B A)
            // Wait, Kotlin Color.ARGB8888:
            // out[dst] = outBuf[src + 1] // R
            // out[dst + 1] = outBuf[src + 2] // G
            // out[dst + 2] = outBuf[src + 3] // B
            // out[dst + 3] = outBuf[src + 0] // A
            int src = 0;
            int dst = 0;
            for (int i = 0; i < pixelCount; ++i) {
                out[dst] = srcBuf[src + 1];
                out[dst + 1] = srcBuf[src + 2];
                out[dst + 2] = srcBuf[src + 3];
                out[dst + 3] = srcBuf[src];
                src += 4;
                dst += 4;
            }
            break;
        }
        case 6: { // RGBA8888 -> ARGB8888 (Kotlin RGBA8888: System.arraycopy)
            memcpy(out, srcBuf, pixelCount * 4);
            break;
        }
        case 7: { // BGRA8888 -> ARGB8888
            // Kotlin Color.BGRA8888:
            // out[dst] = outBuf[src + 2] // R
            // out[dst + 1] = outBuf[src + 1] // G
            // out[dst + 2] = outBuf[src] // B
            // out[dst + 3] = outBuf[src + 3] // A
            int src = 0;
            int dst = 0;
            for (int i = 0; i < pixelCount; ++i) {
                out[dst] = srcBuf[src + 2];
                out[dst + 1] = srcBuf[src + 1];
                out[dst + 2] = srcBuf[src];
                out[dst + 3] = srcBuf[src + 3];
                src += 4;
                dst += 4;
            }
            break;
        }
    }

    env->ReleaseByteArrayElements(outBuf, (jbyte*)out, 0);
    return ret;
}

}
