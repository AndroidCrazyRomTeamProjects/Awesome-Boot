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

}
