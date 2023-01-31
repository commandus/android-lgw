#include <jni.h>
#include <string>
#include <thread>
#include <android/log.h>
#include <usb-listener.h>

#include "lora-gateway-listener.h"
#include "libloragw-helper.h"
#include "android-helper.h"
#include "log-intf.h"
#include "errlist.h"

#include "utilstring.h"

#include "gateway_usb_conf.cpp"
#include "AndroidIdentityService.h"

static void append2logfile(const char *fmt) {
    __android_log_print(ANDROID_LOG_DEBUG, "append2logfile", "%s", fmt);
    FILE *f = fopen("/storage/emulated/0/Android/data/com.commandus.lgw/files/Documents/lgw_com.log", "a+");
    if (f != NULL) {
        fprintf(f, "%s\r\n", fmt);
        fclose(f);
    }
}

static LoraGatewayListener loraGatewayListener;

static JavaVM *jVM;
static jclass loggerClass = nullptr;
static jobject loggerObject = nullptr;
static jmethodID android_LGW_onInfo = nullptr;
static jmethodID android_LGW_onConnected = nullptr;
static jmethodID android_LGW_onDisconnected = nullptr;
static jmethodID android_LGW_onValue = nullptr;
static jmethodID android_LGW_onStart = nullptr;
static jmethodID android_LGW_onFinish = nullptr;
static jmethodID android_LGW_onRead = nullptr;
static jmethodID android_LGW_onWrite = nullptr;
static jmethodID android_LGW_onSetAttr = nullptr;
static jmethodID android_LGW_open = nullptr;
static jmethodID android_LGW_close = nullptr;

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    jVM = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL Java_com_commandus_lgw_LGW_setPayloadListener(
    JNIEnv *env,
    jobject lgw,
    jobject lgwListener
)
{
    if (lgwListener) {
        loggerObject = (jobject) env->NewGlobalRef(lgwListener);
        loggerClass = env->GetObjectClass(loggerObject);

        android_LGW_onInfo = env->GetMethodID(loggerClass, "onInfo", "(Ljava/lang/String;)V");

        android_LGW_onConnected = env->GetMethodID(loggerClass, "onConnected", "(Z)V");
        android_LGW_onDisconnected = env->GetMethodID(loggerClass, "onDisconnected", "()V");
        android_LGW_onValue = env->GetMethodID(loggerClass, "onValue",
                                               "(Lcom/commandus/lgw/Payload;)V");
        android_LGW_onStart = env->GetMethodID(loggerClass, "onStarted",
                                               "(Ljava/lang/String;Ljava/lang/String;I)V");
        android_LGW_onFinish = env->GetMethodID(loggerClass, "onFinished", "(Ljava/lang/String;)V");

        android_LGW_onRead = env->GetMethodID(loggerClass, "onRead", "(I)[B");
        android_LGW_onWrite = env->GetMethodID(loggerClass, "onWrite", "([B)I");
        android_LGW_onSetAttr = env->GetMethodID(loggerClass, "onSetAttr", "(Z)I");

        android_LGW_open = nullptr;
        android_LGW_close = nullptr;
    } else {
        if (loggerObject)
            env->DeleteGlobalRef(loggerObject);
        loggerObject = nullptr;
        loggerClass = nullptr;
        android_LGW_onInfo = nullptr;
        android_LGW_onConnected = nullptr;
        android_LGW_onDisconnected = nullptr;
        android_LGW_onValue = nullptr;
        android_LGW_onStart = nullptr;
        android_LGW_onFinish = nullptr;

        android_LGW_onRead = nullptr;
        android_LGW_onWrite = nullptr;
        android_LGW_onSetAttr = nullptr;

        android_LGW_open = nullptr;
        android_LGW_close = nullptr;
    }
}

extern "C" JNIEXPORT jstring JNICALL Java_com_commandus_lgw_LGW_version(
    JNIEnv* env,
    jobject /* this */)
{
    return env->NewStringUTF(loraGatewayListener.version().c_str());
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
    return 1;
}

static JNIEnv *getJavaEnv(bool &requireDetach) {
    JNIEnv *jEnv;
    int getEnvResult = jVM->GetEnv((void **) &jEnv, JNI_VERSION_1_6);
    requireDetach = getEnvResult == JNI_EDETACHED;
    if (requireDetach) {
        if (jVM->AttachCurrentThread(&jEnv, nullptr) != 0) {
            return nullptr;
        }
    }
   return jEnv;
}


extern "C" void printf_c1(
    const char *fmt
)
{
    bool requireDetach;
    JNIEnv *jEnv = getJavaEnv(requireDetach);
    if (!jEnv || !loggerObject || !android_LGW_onInfo)
        return;
    jstring js = jEnv->NewStringUTF(fmt);
    if (!js)
        return;
    jEnv->CallVoidMethod(loggerObject, android_LGW_onInfo, js);

    if (requireDetach)
        jVM->DetachCurrentThread();
}

class JavaLGWEvent: public LogIntf {
public:
    void onInfo(
        void *env,
        int level,
        int moduleCode,
        int errorCode,
        const std::string &message
    ) override {
        __android_log_print(ANDROID_LOG_DEBUG, "loraGatewayListener", "%s", message.c_str());
        printf_c1(message.c_str());
    }

    void onConnected(
        bool connected
    ) override
    {
        if (!loggerObject || !android_LGW_onConnected)
            return;
        bool requireDetach;
        JNIEnv *jEnv = getJavaEnv(requireDetach);
        if (!jEnv)
            return;
        jboolean jValue = connected;
        jEnv->CallVoidMethod(loggerObject, android_LGW_onConnected, jValue);
        if (requireDetach)
            jVM->DetachCurrentThread();
    }

    void onDisconnected() override
    {
        if (!loggerObject || !android_LGW_onDisconnected)
            return;
        bool requireDetach;
        JNIEnv *jEnv = getJavaEnv(requireDetach);
        if (!jEnv)
            return;
        jEnv->CallVoidMethod(loggerObject, android_LGW_onDisconnected);
        if (requireDetach)
            jVM->DetachCurrentThread();
    }

    void onStarted(uint64_t gatewayId, const std::string regionName, size_t regionIndex) override
    {
        if (!loggerObject || !android_LGW_onStart)
            return;
        bool requireDetach;
        JNIEnv *jEnv = getJavaEnv(requireDetach);
        if (!jEnv)
            return;
        std::stringstream ssGatewayId;
        ssGatewayId << std::hex << gatewayId;
        jstring jgatewayId = jEnv->NewStringUTF(ssGatewayId.str().c_str());
        jstring jregionName = jEnv->NewStringUTF(regionName.c_str());
        jint jRegionIndex = regionIndex;
        jEnv->CallVoidMethod(loggerObject, android_LGW_onStart,
            jgatewayId, jregionName, jRegionIndex);
        if (requireDetach)
            jVM->DetachCurrentThread();
    }

    void onFinished(
        const std::string &message
    ) override
    {
        if (!loggerObject || !android_LGW_onFinish)
            return;
        bool requireDetach;
        JNIEnv *jEnv = getJavaEnv(requireDetach);
        if (!jEnv)
            return;
        jstring jMessage = jEnv->NewStringUTF(message.c_str());
        if (!jMessage)
            return;
        jEnv->CallVoidMethod(loggerObject, android_LGW_onFinish, jMessage);
        if (requireDetach)
            jVM->DetachCurrentThread();
    }

    void onValue(
        Payload &value
    ) override
    {
        if (!loggerObject || !android_LGW_onValue)
            return;
        bool requireDetach;
        JNIEnv *jEnv = getJavaEnv(requireDetach);
        if (!jEnv)
            return;
        jobject jPayload;
        // TODO
        jEnv->CallVoidMethod(loggerObject, android_LGW_onValue, jPayload);
        if (requireDetach)
            jVM->DetachCurrentThread();
    }
};

static LibLoragwHelper libLoragwHelper;

class GatewayConfigMem : public GatewaySettings {
public:
    MemGatewaySettingsStorage storage;
    sx1261_config_t *sx1261() override { return &storage.sx1261; };
    sx130x_config_t *sx130x() override { return &storage.sx130x; };
    gateway_t *gateway() override { return &storage.gateway; };
    struct lgw_conf_debug_s *debug() override { return &storage.debug; };
    std::string *serverAddress() override { return &storage.serverAddr; };
    std::string *gpsTTYPath() override { return &storage.gpsTtyPath; };
    void set(MemGatewaySettingsStorage &value) { storage = value; };
};

class AndroidLoragwOpenClose : public LibLoragwOpenClose {
public:
    int openDevice(const char *fileName, int mode) override
    {
        return 1;
    };

    int closeDevice(int fd) override
    {
        return 0;
    };
};

static void run(
    uint64_t gatewayIdentifier,
    size_t regionIdx,
    int verbosity
)
{
    JavaLGWEvent javaCb;
    libLoragwHelper.bind(&javaCb, new AndroidLoragwOpenClose());

    javaCb.onStarted(gatewayIdentifier,
                     memSetupMemGatewaySettingsStorage[regionIdx].name, regionIdx);

    if (!libLoragwHelper.onOpenClose) {
        javaCb.onFinished("No open/close");
        return;
    }

    IdentityService *identityService = new AndroidIdentityService();
    if (!identityService) {
        std::stringstream ss;
        ss << ERR_MESSAGE << ERR_CODE_FAIL_IDENTITY_SERVICE << ": " << ERR_FAIL_IDENTITY_SERVICE
           << memSetupMemGatewaySettingsStorage[regionIdx].name
           << " (settings #" << regionIdx << ")";
        javaCb.onFinished(ss.str());
        return;
    }

    GatewayConfigMem gwSettings;
    // set regional settings
    memSetupMemGatewaySettingsStorage[regionIdx].setup(gwSettings.storage);

    PacketListener *listener = new USBListener();
    if (!listener) {
        std::stringstream ss;
        ss << ERR_MESSAGE << ERR_CODE_INSUFFICIENT_MEMORY << ": " << ERR_INSUFFICIENT_MEMORY << std::endl;
        javaCb.onFinished(ss.str());
        return;
    }

    listener->setLogger(verbosity, &javaCb);
    if (!listener->onLog) {
        std::stringstream ss;
        ss << ERR_MESSAGE << ERR_CODE_INSUFFICIENT_MEMORY << ": " << ERR_INSUFFICIENT_MEMORY << std::endl;
        javaCb.onFinished(ss.str());
        return;
    }

    std::stringstream ss;
    ss << "Listening, Region "
       << memSetupMemGatewaySettingsStorage[regionIdx].name << std::endl;
    listener->onLog->onInfo(listener, LOG_INFO, LOG_MAIN_FUNC, 0, ss.str());

    listener->onLog->onInfo(listener, LOG_INFO, LOG_MAIN_FUNC, 0, "Start listen");
    int flags = FLAG_GATEWAY_LISTENER_NO_SEND | FLAG_GATEWAY_LISTENER_NO_BEACON;
    int r = listener->listen(&gwSettings, flags);
    listener->onLog->onInfo(listener, LOG_INFO, LOG_MAIN_FUNC, 0, "Stop listen");

    if (r && listener->onLog) {
        std::stringstream ss;
        ss << ERR_MESSAGE << r << ": " << strerror_lorawan_ns(r) << std::endl;
        listener->onLog->onInfo(listener, LOG_ERR, LOG_MAIN_FUNC, r, ss.str());
    }
    delete libLoragwHelper.onOpenClose;
    libLoragwHelper.onOpenClose = nullptr;

    if (listener) {
        delete listener;
        listener = nullptr;
    }
    if (identityService) {
        delete identityService;
        identityService = nullptr;
    }
    javaCb.onFinished(std::basic_string<char, std::char_traits<char>, std::allocator<char>>());
}

/**
 * @param env
 * @param jStr
 * @return string
 * @see https://stackoverflow.com/questions/41820039/jstringjni-to-stdstringc-with-utf8-characters
 */
static std::string jstring2string(
    JNIEnv *env,
    jstring jStr
) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, nullptr);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

static std::thread *gwThread = nullptr;

extern "C" JNIEXPORT jint JNICALL Java_com_commandus_lgw_LGW_start(
    JNIEnv* env,
    jobject /* this */,
    jint regionIdx,
    jstring gwIdString,
    jint verbosity
)
{
    std::string id = jstring2string(env, gwIdString);
    uint64_t gwId = std::stoull(id.c_str(), 0, 16);
    gwThread = new std::thread(run, gwId, regionIdx, verbosity);
    return 0;
}

extern "C" JNIEXPORT void JNICALL Java_com_commandus_lgw_LGW_stop(
    JNIEnv* env,
    jobject /* this */
)
{
    JavaLGWEvent javaLog;
    if (!gwThread)
        javaLog.onInfo(nullptr, LOG_ERR, LOG_USB_ANDROID, ERR_CODE_FAIL_IDENTITY_SERVICE, "Already stopped" );
    if (!loggerObject || !android_LGW_onInfo)
        return;
    if (!env)
        return;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_commandus_lgw_LGW_regionNames(
    JNIEnv *env,
    jobject thiz
) {
    bool requireDetach;
    JNIEnv *jEnv = getJavaEnv(requireDetach);
    if (!jEnv)
        return nullptr;
    jsize sz = sizeof(memSetupMemGatewaySettingsStorage) / sizeof(setupMemGatewaySettingsStorage);
    if (!sz)
        return nullptr;
    jobjectArray result = jEnv->NewObjectArray(sz, jEnv->FindClass("java/lang/String"), nullptr);
    if (result == nullptr) {
        if (requireDetach)
            jVM->DetachCurrentThread();
        return nullptr;
    }
    for (int i = 0; i < sz; i++) {
        jstring js = jEnv->NewStringUTF(memSetupMemGatewaySettingsStorage[i].name.c_str());
        jEnv->SetObjectArrayElement(result, i, js);
    }

    if (requireDetach)
        jVM->DetachCurrentThread();
    return result;
}

extern "C" ssize_t read_c(
    int fd,
    void *buf,
    size_t count
)
{
    if (!loggerObject || !android_LGW_onRead)
        return -1;

    bool requireDetach;
    JNIEnv *jEnv = getJavaEnv(requireDetach);
    if (!jEnv || !loggerObject)
        return -1;

    jint jCount = count;
    jbyteArray a = static_cast<jbyteArray>(jEnv->CallObjectMethod(loggerObject, android_LGW_onRead, jCount));
    if (!a)
        return -1;

    jbyte *ae = jEnv->GetByteArrayElements(a, 0);
    jsize len = jEnv->GetArrayLength(a);

    for (int i = 0; i < len; ++i ) {
        *((char *) buf + i) = ae[i];
    }
    if (requireDetach)
        jVM->DetachCurrentThread();
    return len;
}

extern "C" ssize_t write_c(
    int fd,
    const void *buf,
    size_t count
)
{
    if (!loggerObject || !android_LGW_onWrite)
        return -1;

    bool requireDetach;
    JNIEnv *jEnv = getJavaEnv(requireDetach);
    if (!jEnv || !loggerObject)
        return -1;
    jbyteArray jb = jEnv->NewByteArray(count);
    if (!jb)
        return -1;
    jEnv->SetByteArrayRegion(jb, 0, count, (jbyte*) buf);

    int r = jEnv->CallIntMethod(loggerObject, android_LGW_onWrite, jb);

    if (requireDetach)
        jVM->DetachCurrentThread();

    return r;
}

extern "C" int tcgetattr_c(
    int fd,
    struct termios *retval
)
{
    if (retval) {
        retval->c_iflag = 0;    // input modes
        retval->c_oflag = 0;    // output modes
        retval->c_cflag = 0;    // control modes
        retval->c_lflag = 0;    // local modes
        retval->c_line = 0;
        for (int i = 0; i < NCCS; i++) {
            retval->c_cc[i] = 0;    // special characters
        }
    }
    return 0;
}

extern "C" int tcsetattr_c(
    int fd,
    int optional_actions,
    const struct termios *termios_p
)
{
    if (!termios_p)
        return 0;
    if (!loggerObject || !android_LGW_onWrite)
        return -1;

    bool requireDetach;
    JNIEnv *jEnv = getJavaEnv(requireDetach);
    if (!jEnv || !loggerObject)
        return -1;

    // blocking mode
    jboolean blocking = termios_p->c_cc[VMIN] != 0;
    int r = jEnv->CallIntMethod(loggerObject, android_LGW_onSetAttr, blocking);

    if (requireDetach)
        jVM->DetachCurrentThread();

    return r;
}
