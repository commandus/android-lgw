#include <jni.h>
#include <string>

#include "android-helper.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_commandus_lgw_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
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

}
