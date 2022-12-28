#include <string>
#include <jni.h>

#include <android/log.h>

#include "lora-gateway-listener.h"
#include "android-helper.h"

static LoraGatewayListener lgw;

static JavaVM *jVM;
static jclass loggerClass = nullptr;
static jobject loggerObject = nullptr;
static jmethodID android_LGW_printf = nullptr;

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, "lgw", "JNI_OnLoad()");
    jVM = vm;
    JNIEnv *env;
    (*jVM).GetEnv((void **) &env, JNI_VERSION_1_6);
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL Java_com_commandus_lgw_LGW_setLog(
    JNIEnv *env,
    jobject lgw,
    jobject log
)
{
    (*jVM).GetEnv((void **) &env, JNI_VERSION_1_6);

    if (log) {
        loggerObject = (jobject) env->NewGlobalRef(log);
        loggerClass = env->GetObjectClass(loggerObject);
        android_LGW_printf = env->GetMethodID(loggerClass, "log", "(Ljava/lang/String;)V");
    } else {
        if (loggerObject)
            env->DeleteGlobalRef(loggerObject);
        loggerObject = nullptr;
        loggerClass = nullptr;
        android_LGW_printf = nullptr;
    }
}

extern "C" JNIEXPORT jstring JNICALL Java_com_commandus_lgw_LGW_version
(
        JNIEnv* env,
        jobject /* this */)
{
    __android_log_print(ANDROID_LOG_DEBUG, "lgw", "version()");
    return env->NewStringUTF(lgw.version().c_str());
}

extern "C" int close_c(
    int fd
)
{
    return 0;
}

extern "C" int open_c(
    const char *file,
    int flags
)
{
    return 0;
}

extern "C" void printf_c(
    const char *fmt, ...
)
{
    __android_log_print(ANDROID_LOG_DEBUG, "lgw", "printf_c");
    if (!android_LGW_printf)
        return;
    JNIEnv *env;
    jVM->GetEnv((void**) &env, JNI_VERSION_1_4);


    jstring js = env->NewStringUTF(fmt);
    return env->CallVoidMethod(loggerObject, android_LGW_printf, js);
}

extern "C" JNIEXPORT jint JNICALL Java_com_commandus_lgw_LGW_start(
    JNIEnv* env,
    jobject /* this */,
    jboolean connected,
    jint fd
)
{
    printf_c("Start");
    __android_log_print(ANDROID_LOG_DEBUG, "lgw", "start");
}

extern "C" JNIEXPORT void JNICALL Java_com_commandus_lgw_LGW_stop(
    JNIEnv* env,
    jobject /* this */
)
{
    __android_log_print(ANDROID_LOG_DEBUG, "lgw", "stop");
    printf_c("Stop");
}
