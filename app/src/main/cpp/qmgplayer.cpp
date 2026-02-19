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
typedef void** (*QmageDecCreateAniInfo_t)(uint8_t*, int, int);
typedef void   (*QmageDecDestroyAniInfo_t)(void**);
typedef int    (*QmageDecodeAniFrame_t)(void**, uint8_t*);
typedef int    (*QmageDecodeFrame_t)(uint8_t*, int, uint8_t*);
typedef int    (*QmageDecGetLastErr_t)();

static void* qmgHandle = nullptr;

static QmageDecCreateAniInfo_t  QmageDecCreateAniInfo = nullptr;
static QmageDecDestroyAniInfo_t QmageDecDestroyAniInfo = nullptr;
static QmageDecodeAniFrame_t    QmageDecodeAniFrame = nullptr;
static QmageDecodeFrame_t       QmageDecodeFrame = nullptr;
static QmageDecGetLastErr_t     QmageDecGetLastErr = nullptr;

static bool loadQmgLibrary() {
    if (qmgHandle) return true;

    qmgHandle = dlopen("libQmageDecoder.so", RTLD_NOW);
    if (!qmgHandle) {
        LOGE("dlopen failed: %s", dlerror());
        return false;
    }

    QmageDecCreateAniInfo =
        (QmageDecCreateAniInfo_t)dlsym(qmgHandle, "QmageDecCreateAniInfo");
    QmageDecDestroyAniInfo =
        (QmageDecDestroyAniInfo_t)dlsym(qmgHandle, "QmageDecDestroyAniInfo");
    QmageDecodeAniFrame =
        (QmageDecodeAniFrame_t)dlsym(qmgHandle, "QmageDecodeAniFrame");
    QmageDecodeFrame =
        (QmageDecodeFrame_t)dlsym(qmgHandle, "QmageDecodeFrame");
    QmageDecGetLastErr =
        (QmageDecGetLastErr_t)dlsym(qmgHandle, "QmageDecGetLastErr");

    if (!QmageDecCreateAniInfo || !QmageDecodeAniFrame) {
        LOGE("dlsym failed for one or more functions");
        return false;
    }
    LOGI("Successfully loaded libQmageDecoder.so and all functions");
    return true;
}


JNIEXPORT jlong JNICALL
Java_org_crazyromteam_qmgstore_qmg_LibQmg_CreateAniInfo(
        JNIEnv* env,
        jobject,
        jbyteArray qmgData,
        jint flags) {
    LOGI("CreateAniInfo called");
    if (!loadQmgLibrary()) {
        return 0;
    }

    jsize len = env->GetArrayLength(qmgData);
    LOGI("qmgData length: %d", len);
    uint8_t* buffer = (uint8_t*)malloc(len + 0x10000);
    memset(buffer + len, 0, 0x10000);

    env->GetByteArrayRegion(qmgData, 0, len, (jbyte*)buffer);

    void** ani = QmageDecCreateAniInfo(buffer, flags, len);
    if (!ani) {
        LOGE("QmageDecCreateAniInfo failed, returned null");
        free(buffer);
        return 0;
    }

    LOGI("QmageDecCreateAniInfo succeeded, pointer: %p", ani);
    // Store buffer pointer inside aniInfo[0] if needed, otherwise manage separately
    return (jlong)ani;
}

JNIEXPORT void JNICALL
Java_org_crazyromteam_qmgstore_qmg_LibQmg_DestroyAniInfo(
        JNIEnv*,
        jobject,
        jlong aniPtr) {
    LOGI("DestroyAniInfo called for pointer: %p", (void**)aniPtr);
    if (!loadQmgLibrary()) return;

    if (!aniPtr) return;
    QmageDecDestroyAniInfo((void**)aniPtr);
    LOGI("DestroyAniInfo completed");
}

JNIEXPORT jint JNICALL
Java_org_crazyromteam_qmgstore_qmg_LibQmg_DecodeAniFrame(
        JNIEnv* env,
        jobject,
        jlong aniPtr,
        jbyteArray outBuf) {
    if (!loadQmgLibrary() || !aniPtr) {
        LOGE("DecodeAniFrame called with null pointer or library not loaded");
        return -1; // Return an error code
    }

    uint8_t* out = (uint8_t*)env->GetByteArrayElements(outBuf, nullptr);
    int ret = QmageDecodeAniFrame((void**)aniPtr, out);
    LOGI("DecodeAniFrame returned: %d. First 4 bytes of outBuf: %02x %02x %02x %02x", ret, out[0], out[1], out[2], out[3]);
    env->ReleaseByteArrayElements(outBuf, (jbyte*)out, 0);
    return ret;
}

JNIEXPORT jint JNICALL
Java_org_crazyromteam_qmgstore_qmg_LibQmg_DecodeFrame(
        JNIEnv* env,
        jobject,
        jbyteArray inBuf,
        jint inLen,
        jbyteArray outBuf) {
    if (!loadQmgLibrary()) return -1;

    uint8_t* in  = (uint8_t*)env->GetByteArrayElements(inBuf, nullptr);
    uint8_t* out = (uint8_t*)env->GetByteArrayElements(outBuf, nullptr);

    int ret = QmageDecodeFrame(in, inLen, out);

    env->ReleaseByteArrayElements(inBuf, (jbyte*)in, JNI_ABORT);
    env->ReleaseByteArrayElements(outBuf, (jbyte*)out, 0);

    return ret;
}

JNIEXPORT jint JNICALL
Java_org_crazyromteam_qmgstore_qmg_LibQmg_GetLastErr(
        JNIEnv*,
        jobject) {
    if (!loadQmgLibrary()) return -1;
    return QmageDecGetLastErr();
}

}
