#include <jni.h>
#include <string>
#include <thread>
#include <ctime>
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

// @see https://developer.android.com/training/articles/perf-jni
/*
static void append2logfile(const char *fmt) {
    FILE *f = fopen("/storage/emulated/0/Android/data/com.commandus.lgw/files/Documents/lgw.log", "a+");
    if (f != nullptr) {
        fprintf(f, "%s\r\n", fmt);
        fclose(f);
    }
}
*/

/**
 * Java string to std::string
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

    jclass stringClass = env->GetObjectClass(jStr);
    jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    // jbyteArray
    auto stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    // size_t
    auto length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, nullptr);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

static JavaVM *jVM;
static jclass loggerCls = nullptr;
static jclass payloadCls = nullptr;

static jobject loggerObject = nullptr;
static jmethodID android_LGW_onInfo = nullptr;
static jmethodID android_LGW_onConnected = nullptr;
static jmethodID android_LGW_onDisconnected = nullptr;
static jmethodID android_LGW_onValue = nullptr;
static jmethodID android_LGW_onReceive = nullptr;
static jmethodID android_LGW_onStart = nullptr;
static jmethodID android_LGW_onFinished = nullptr;
static jmethodID android_LGW_onRead = nullptr;
static jmethodID android_LGW_onWrite = nullptr;
static jmethodID android_LGW_onSetAttr = nullptr;
static jmethodID payloadCnstr = nullptr;
// identity callbacks
static jmethodID android_LGW_onIdentitySize = nullptr;
static jmethodID android_LGW_onIdentityGet = nullptr;
static jmethodID android_LGW_onIdentityGetNetworkIdentity = nullptr;

static void dbgPrintOutAddresses()
{
    __android_log_print(ANDROID_LOG_DEBUG, "algw",
                        "loggerObject %x loggerCls %x payloadCls %x",
                        loggerObject, loggerCls, payloadCls);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    jVM = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL Java_com_commandus_lgw_LorawanGatewayRak2287_setPayloadListener(
    JNIEnv *env,
    jobject lgw,
    jobject lgwListener
)
{
    if (lgwListener) {
        loggerObject = (jobject) env->NewGlobalRef(lgwListener);

        jclass lloggerCls = env->GetObjectClass(loggerObject);
        loggerCls = reinterpret_cast<jclass>(env->NewGlobalRef(lloggerCls));

        jclass lpayloadCls = env->FindClass("com/commandus/lgw/Payload");
        payloadCls = reinterpret_cast<jclass>(env->NewGlobalRef(lpayloadCls));

        android_LGW_onInfo = env->GetMethodID(loggerCls, "onInfo", "(ILjava/lang/String;)V");
        android_LGW_onConnected = env->GetMethodID(loggerCls, "onUsbConnected", "(Z)V");
        android_LGW_onDisconnected = env->GetMethodID(loggerCls, "onUsbDisconnected", "()V");
        android_LGW_onReceive = env->GetMethodID(loggerCls, "onReceive",
                                                 "(Lcom/commandus/lgw/Payload;)V");
        android_LGW_onValue = env->GetMethodID(loggerCls, "onValue",
                                               "(Lcom/commandus/lgw/Payload;)V");
        android_LGW_onStart = env->GetMethodID(loggerCls, "onStarted",
                                               "(Ljava/lang/String;Ljava/lang/String;I)V");
        android_LGW_onFinished = env->GetMethodID(loggerCls, "onFinished", "(Ljava/lang/String;)V");

        android_LGW_onRead = env->GetMethodID(loggerCls, "onRead", "(I)[B");
        android_LGW_onWrite = env->GetMethodID(loggerCls, "onWrite", "([B)I");
        android_LGW_onSetAttr = env->GetMethodID(loggerCls, "onSetAttr", "(Z)I");
        android_LGW_onIdentitySize = env->GetMethodID(loggerCls, "onIdentitySize", "()I");
        android_LGW_onIdentityGet = env->GetMethodID(loggerCls, "onIdentityGet", "(Ljava/lang/String;)Lcom/commandus/lgw/LoraDeviceAddress;");
        android_LGW_onIdentityGetNetworkIdentity = env->GetMethodID(loggerCls, "onGetNetworkIdentity", "(Ljava/lang/String;)Lcom/commandus/lgw/LoraDeviceAddress;");

        payloadCnstr = env->GetMethodID(payloadCls, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIF)V");

        // it can help on crash
        dbgPrintOutAddresses();
    } else {
        if (loggerObject) {
            env->DeleteGlobalRef(loggerObject);
            loggerObject = nullptr;
        }
        if (payloadCls) {
            env->DeleteGlobalRef(payloadCls);
            payloadCls = nullptr;
        }
        loggerCls = nullptr;
        android_LGW_onInfo = nullptr;
        android_LGW_onConnected = nullptr;
        android_LGW_onDisconnected = nullptr;
        android_LGW_onReceive = nullptr;
        android_LGW_onValue = nullptr;
        android_LGW_onStart = nullptr;
        android_LGW_onFinished = nullptr;

        android_LGW_onRead = nullptr;
        android_LGW_onWrite = nullptr;
        android_LGW_onSetAttr = nullptr;

        android_LGW_onIdentitySize = nullptr;
        android_LGW_onIdentityGet = nullptr;
        android_LGW_onIdentityGetNetworkIdentity = nullptr;

        payloadCnstr = nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_commandus_lgw_LorawanGatewayFake_setPayloadListener(
        JNIEnv *env,
        jobject lgw,
        jobject lgwListener
)
{
    return Java_com_commandus_lgw_LorawanGatewayRak2287_setPayloadListener(env, lgw, lgwListener);
}

extern "C" JNIEXPORT jstring JNICALL Java_com_commandus_lgw_LorawanGatewayRak2287_version(
    JNIEnv* env,
    jobject /* this */)
{
    LoraGatewayListener lgw;
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
    if (!js) {
        if (requireDetach)
            jVM->DetachCurrentThread();
        return;
    }

    jint jSeverity = 0;
    jEnv->CallVoidMethod(loggerObject, android_LGW_onInfo, jSeverity, js);

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
        jstring jGatewayId = jEnv->NewStringUTF(ssGatewayId.str().c_str());
        jstring jRegionName = jEnv->NewStringUTF(regionName.c_str());
        jint jRegionIndex = (jint) regionIndex;
        if (jGatewayId && jRegionName)
            jEnv->CallVoidMethod(loggerObject, android_LGW_onStart,
                                 jGatewayId, jRegionName, jRegionIndex);
        if (requireDetach)
            jVM->DetachCurrentThread();
    }

    void onFinished(
        const std::string &message
    ) override
    {
        if (!loggerObject || !android_LGW_onFinished)
            return;
        bool requireDetach;
        JNIEnv *jEnv = getJavaEnv(requireDetach);
        if (!jEnv)
            return;
        jstring jMessage = jEnv->NewStringUTF(message.c_str());
        if (jMessage)
            jEnv->CallVoidMethod(loggerObject, android_LGW_onFinished, jMessage);
        if (requireDetach)
            jVM->DetachCurrentThread();
    }

    void onReceive(
        Payload &value
    ) override
    {
        if (!loggerObject || !android_LGW_onReceive)
            return;
        bool requireDetach;
        JNIEnv *jEnv = getJavaEnv(requireDetach);
        if (!jEnv)
            return;
        if (payloadCls) {
            if (payloadCnstr) {
                jstring jEui = jEnv->NewStringUTF(value.eui.c_str());
                jstring jName = jEnv->NewStringUTF(value.devName.c_str());
                jstring jHexPayload = jEnv->NewStringUTF(hexString(value.payload).c_str());
                jobject jPayload = jEnv->NewObject(payloadCls, payloadCnstr,
                    jEui, jName, jHexPayload, value.frequency, value.rssi, value.lsnr);
                jEnv->CallVoidMethod(loggerObject, android_LGW_onReceive, jPayload);
            }
        }
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
        if (payloadCls) {
            jmethodID cnstr = jEnv->GetMethodID(payloadCls, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIF)V");
            if (cnstr) {
                jstring jEui = jEnv->NewStringUTF(value.eui.c_str());
                jstring jName = jEnv->NewStringUTF(value.devName.c_str());
                jstring jHexPayload = jEnv->NewStringUTF(hexString(value.payload).c_str());
                jPayload = jEnv->NewObject(payloadCls, cnstr,
                    jEui, jName, jHexPayload, value.frequency, value.rssi, value.lsnr);
                jEnv->CallVoidMethod(loggerObject, android_LGW_onValue, jPayload);
            }
        }
        if (requireDetach)
            jVM->DetachCurrentThread();
    }

    /**
     * Get device EUI, name, keys by tha address
     * @param retVal return valus
     * @param devaddr device address
     * @return 0- success, ERR_CODE_DEVICE_ADDRESS_NOTFOUND- device is not registered
     */
    int identityGet(DeviceId &retVal, DEVADDR &devaddr) override
    {
        if (!loggerObject || !android_LGW_onIdentityGet)
            return 0;
        bool requireDetach;
        JNIEnv *jEnv = getJavaEnv(requireDetach);
        if (!jEnv)
            return 0;
        std::string a = DEVADDR2string(devaddr);
        jstring ja = jEnv->NewStringUTF(a.c_str());
        jobject r = jEnv->CallObjectMethod(loggerObject, android_LGW_onIdentityGet, ja);
        if (!r) {
            jEnv->DeleteLocalRef(ja);
            if (requireDetach)
                jVM->DetachCurrentThread();
            return ERR_CODE_DEVICE_ADDRESS_NOTFOUND;
        }

        jclass jLoraAddressCls = jEnv->GetObjectClass(r);
        // String addr not used
        // jfieldID jfAddr = jEnv->GetFieldID(jLoraAddressCls,"addr", "Ljava/lang/String;");
        // jstring jsAddr = (jstring) jEnv->GetObjectField(r, jfAddr);

        // DevEUI devEui;
        jfieldID jfDevEui = jEnv->GetFieldID(jLoraAddressCls,"devEui", "Lcom/commandus/lgw/DevEUI;");
        jobject joDevEui = jEnv->GetObjectField(r, jfDevEui);
        jclass jDevEUICls = jEnv->GetObjectClass(joDevEui);
        jmethodID jmDevEuiToString = jEnv->GetMethodID(jDevEUICls, "toString", "()Ljava/lang/String;");
        // jstring
        auto jsDevEui = (jstring) jEnv->CallObjectMethod(joDevEui, jmDevEuiToString);
        std::string s = jstring2string(jEnv, jsDevEui);

        retVal.setEUIString(s);

        jEnv->DeleteLocalRef(joDevEui);
        jEnv->DeleteLocalRef(jsDevEui);
        // KEY128 nwkSKey
        jfieldID jfNwkSKey = jEnv->GetFieldID(jLoraAddressCls,"nwkSKey", "Lcom/commandus/lgw/KEY128;");
        jobject joNwkSKey = jEnv->GetObjectField(r, jfNwkSKey);
        jclass jNwkSKeyCls = jEnv->GetObjectClass(joNwkSKey);
        jmethodID jmNwkSKeyToString = jEnv->GetMethodID(jNwkSKeyCls, "toString", "()Ljava/lang/String;");
        // jstring
        auto jsNwkSKey = (jstring)jEnv->CallObjectMethod(joNwkSKey, jmNwkSKeyToString);
        retVal.setNwkSKeyString(jstring2string(jEnv, jsNwkSKey));
        jEnv->DeleteLocalRef(joNwkSKey);
        jEnv->DeleteLocalRef(jsNwkSKey);
        // KEY128 appSKey
        jfieldID jfAppSKey = jEnv->GetFieldID(jLoraAddressCls,"appSKey", "Lcom/commandus/lgw/KEY128;");
        jobject joAppSKey = jEnv->GetObjectField(r, jfAppSKey);
        jclass jAppSKeyCls = jEnv->GetObjectClass(joAppSKey);
        jmethodID jmAppSKeyToString = jEnv->GetMethodID(jAppSKeyCls, "toString", "()Ljava/lang/String;");
        // jstring
        auto jsAppSKey = (jstring) jEnv->CallObjectMethod(joAppSKey, jmAppSKeyToString);
        retVal.setAppSKeyString(jstring2string(jEnv, jsAppSKey));
        jEnv->DeleteLocalRef(joAppSKey);
        jEnv->DeleteLocalRef(jsAppSKey);
        // String name
        jfieldID jfName = jEnv->GetFieldID(jLoraAddressCls,"name", "Ljava/lang/String;");
        // jstring
        auto jsName = (jstring) jEnv->GetObjectField(r, jfName);
        retVal.setName(jstring2string(jEnv, jsName));
        jEnv->DeleteLocalRef(jsName);

        if (requireDetach)
            jVM->DetachCurrentThread();
        return 0;
    }

    /**
     * Return device address and identity by the EUI
     * @param retVal device address and identities
     * @param eui device EUI
     * @return 0- success, ERR_CODE_DEVICE_EUI_NOT_FOUND- error
     */
    int identityGetNetworkIdentity(NetworkIdentity &retVal, const DEVEUI &eui) override
    {
        if (!loggerObject || !android_LGW_onIdentityGetNetworkIdentity)
            return 0;
        bool requireDetach;
        JNIEnv *jEnv = getJavaEnv(requireDetach);
        if (!jEnv)
            return 0;
        std::string sDevEui = DEVEUI2string(eui);
        jstring jDevEui = jEnv->NewStringUTF(sDevEui.c_str());
        jobject r = jEnv->CallObjectMethod(loggerObject, android_LGW_onIdentityGetNetworkIdentity, jDevEui);
        if (!r) {
            if (requireDetach)
                jVM->DetachCurrentThread();
            return ERR_CODE_DEVICE_EUI_NOT_FOUND;
        }
        jclass jLoraAddressCls = jEnv->GetObjectClass(r);

        // String addr
        jfieldID jfAddr = jEnv->GetFieldID(jLoraAddressCls,"addr", "Ljava/lang/String;");
        // jstring
        auto jsAddr = (jstring) jEnv->GetObjectField(r, jfAddr);
        string2DEVADDR(retVal.devaddr, jstring2string(jEnv, jsAddr));
        jEnv->DeleteLocalRef(jsAddr);

        // DevEUI devEui;
        jfieldID jfDevEui = jEnv->GetFieldID(jLoraAddressCls,"devEui", "Lcom/commandus/lgw/DevEUI;");
        jobject joDevEui = jEnv->GetObjectField(r, jfDevEui);
        jclass jDevEUICls = jEnv->GetObjectClass(joDevEui);
        jmethodID jmDevEuiToString = jEnv->GetMethodID(jDevEUICls, "toString", "()Ljava/lang/String;");
        // jstring
        auto jsDevEui = (jstring) jEnv->CallObjectMethod(joDevEui, jmDevEuiToString);
        string2DEVEUI(retVal.devEUI, jstring2string(jEnv, jsDevEui));
        jEnv->DeleteLocalRef(joDevEui);
        jEnv->DeleteLocalRef(jsDevEui);

        // KEY128 nwkSKey
        jfieldID jfNwkSKey = jEnv->GetFieldID(jLoraAddressCls,"nwkSKey", "Lcom/commandus/lgw/KEY128;");
        jobject joNwkSKey = jEnv->GetObjectField(r, jfNwkSKey);
        jclass jNwkSKeyCls = jEnv->GetObjectClass(joNwkSKey);
        jmethodID jmNwkSKeyToString = jEnv->GetMethodID(jNwkSKeyCls, "toString", "()Ljava/lang/String;");
        // jstring
        auto jsNwkSKey = (jstring) jEnv->CallObjectMethod(joNwkSKey, jmNwkSKeyToString);
        string2KEY(retVal.nwkSKey, jstring2string(jEnv, jsNwkSKey));
        jEnv->DeleteLocalRef(joNwkSKey);
        jEnv->DeleteLocalRef(jsNwkSKey);

        // KEY128 appSKey
        jfieldID jfAppSKey = jEnv->GetFieldID(jLoraAddressCls,"appSKey", "Lcom/commandus/lgw/KEY128;");
        jobject joAppSKey = jEnv->GetObjectField(r, jfAppSKey);
        jclass jAppSKeyCls = jEnv->GetObjectClass(joAppSKey);
        jmethodID jmAppSKeyToString = jEnv->GetMethodID(jAppSKeyCls, "toString", "()Ljava/lang/String;");
        // jstring
        auto jsAppSKey = (jstring) jEnv->CallObjectMethod(joAppSKey, jmAppSKeyToString);
        string2KEY(retVal.appSKey, jstring2string(jEnv, jsAppSKey));

        jEnv->DeleteLocalRef(joAppSKey);
        jEnv->DeleteLocalRef(jsAppSKey);

        // String name
        jfieldID jfName = jEnv->GetFieldID(jLoraAddressCls,"name", "Ljava/lang/String;");
        // jstring
        auto jsName = (jstring) jEnv->GetObjectField(r, jfName);
        string2DEVICENAME(retVal.name, jstring2string(jEnv, jsName).c_str());
        jEnv->DeleteLocalRef(jsName);

        if (requireDetach)
            jVM->DetachCurrentThread();
        return 0;
    }

    // Entries count
    size_t identitySize() override
    {
        if (!loggerObject || !android_LGW_onIdentitySize)
            return 0;
        bool requireDetach;
        JNIEnv *jEnv = getJavaEnv(requireDetach);
        if (!jEnv)
            return 0;
        jint r = jEnv->CallIntMethod(loggerObject, android_LGW_onIdentitySize);

        if (requireDetach)
            jVM->DetachCurrentThread();
        return r;
    }
};

static const char* long_gateway_thread_names[] = {
        "gwMaim",
        "upstream",
        "downstream",
        "jit",
        "gps",
        "gpsCheckTime",
        "spectralScan"
};

class JaveThreadStartFinish : public ThreadStartFinish {
public:
    void onThreadStart(ENUM_GATEWAY_THREAD thread) override
    {
    }

    void onThreadFinish(ENUM_GATEWAY_THREAD thread) override
    {
        // detach Java thread
        jVM->DetachCurrentThread();
    }
};

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

/**
 * Handle uplink messages interface
 */
class AndroidLoraPacketHandler : public LoraPacketHandler {
private:
    JavaLGWEvent *javaLGWEvent;
public:
    explicit AndroidLoraPacketHandler(JavaLGWEvent *javaLGWEvent) {
        this->javaLGWEvent = javaLGWEvent;
    }

    int ack(
        int socket,
        const sockaddr_in* gwAddress,
        const SEMTECH_PREFIX_GW &dataprefix
    ) override
    {
        return 0;
    };

    // Return 0, retval = EUI and keys
    int put(
        const struct timeval &time,
        SemtechUDPPacket &packet
    ) override
    {
        if (!javaLGWEvent)
            return ERR_CODE_NO_CONFIG;
        Payload p;
        p.received = time.tv_sec;
        p.eui = packet.getDeviceEUI();
        p.devName = DEVICENAME2string(packet.devId.name);
        if (!packet.metadata.empty()) {
            rfmMetaData &m = packet.metadata[0];
            p.frequency = (int) m.freq;
            p.rssi = m.rssi;
            p.lsnr = m.lsnr;
        } else {
            p.frequency = 0;
            p.rssi = 0;
            p.lsnr = 0;
        }
        p.payload = packet.payload;
        javaLGWEvent->onValue(p);
        return 0;
    }

    int putUnidentified(
        const struct timeval &time,
        SemtechUDPPacket &packet
    ) override
    {
        if (!javaLGWEvent)
            return ERR_CODE_NO_CONFIG;
        Payload p;
        p.received = time.tv_sec;
        p.eui = "";
        p.devName = "";
        if (!packet.metadata.empty()) {
            rfmMetaData &m = packet.metadata[0];
            p.frequency = (int) m.freq;
            p.rssi = m.rssi;
            p.lsnr = m.lsnr;
        } else {
            p.frequency = 0;
            p.rssi = 0;
            p.lsnr = 0;
        }
        javaLGWEvent->onReceive(p);
        return 0;
    }

    // Reserve FPort number for network service purposes
    void reserveFPort(
        uint8_t value
    ) override
    {
    }

    int join(
        const struct timeval &time,
        int socket,
        const sockaddr_in *socketAddress,
        SemtechUDPPacket &packet
    ) override
    {
        return 0;
    }
};

static LibLoragwHelper libLoragwHelper;

class AndroidGatewayHandler {
public:
    // Read database hosted in the Android (content provider)
    IdentityService *identityService;
    // open/close Android USB serial port helper
    LibLoragwOpenClose *libLoragwOpenClose;
    // RAK2287 USB serial port listener
    USBListener *listener;
    // Send packet to the Android
    AndroidLoraPacketHandler *packetHandler;
    // Java callback
    JavaLGWEvent javaLGWEvent;

    AndroidGatewayHandler() {
        identityService = new AndroidIdentityService();
        libLoragwOpenClose = new AndroidLoragwOpenClose();
        libLoragwHelper.onOpenClose = libLoragwHelper.onOpenClose;
        libLoragwHelper.bind(&javaLGWEvent, libLoragwOpenClose);
        identityService->init("", &javaLGWEvent);

        listener = new USBListener();
        listener->setLogger(7, &javaLGWEvent);
        packetHandler = new AndroidLoraPacketHandler(&javaLGWEvent);
        listener->setHandler(packetHandler);
        listener->setIdentityService(identityService);
        listener->listener.stopRequest = false;

        listener->listener.setOnStop(
            [this] (const LoraGatewayListener *lsnr,
                bool gracefullyStopped
            ) {
                if (!gracefullyStopped) {
                    // wait until all threads done
                    while(!lsnr->isStopped()) {
                        javaLGWEvent.onInfo(nullptr, LOG_INFO, LOG_MAIN_FUNC, 0, "Stopping..");
                        sleep(1);
                    }
                }
            }
        );
    }

    virtual ~AndroidGatewayHandler() {
        if (libLoragwHelper.onOpenClose) {
            delete libLoragwHelper.onOpenClose;
            libLoragwHelper.onOpenClose = nullptr;
        }
        if (listener) {
            delete listener;
            listener = nullptr;
        }
        if (identityService) {
            delete identityService;
            identityService = nullptr;
        }
        if (packetHandler) {
            delete packetHandler;
            packetHandler = nullptr;
        }
    }
};

static JaveThreadStartFinish javeThreadStartFinish;
static AndroidGatewayHandler* listenerHandler = nullptr;

static void run(
    uint64_t gatewayIdentifier,
    size_t regionIdx,
    int verbosity
)
{
    listenerHandler = new AndroidGatewayHandler();

    // check 1
    if (!libLoragwHelper.onOpenClose) {
        listenerHandler->javaLGWEvent.onFinished("No open/close");
        delete listenerHandler;
        jVM->DetachCurrentThread();
        return;
    }

    // check 2
    if (!listenerHandler->identityService) {
        std::stringstream ss;
        ss << ERR_MESSAGE << ERR_CODE_FAIL_IDENTITY_SERVICE << ": " << ERR_FAIL_IDENTITY_SERVICE
           << memSetupMemGatewaySettingsStorage[regionIdx].name
           << " (settings #" << regionIdx << ")";
        listenerHandler->javaLGWEvent.onFinished(ss.str());
        delete listenerHandler;
        jVM->DetachCurrentThread();
        return;
    }

    // check 3
    if (!listenerHandler->listener || !listenerHandler->listener->onLog) {
        std::stringstream ss;
        ss << ERR_MESSAGE << ERR_CODE_INSUFFICIENT_MEMORY << ": " << ERR_INSUFFICIENT_MEMORY << std::endl;
        listenerHandler->javaLGWEvent.onFinished(ss.str());
        delete listenerHandler;
        jVM->DetachCurrentThread();
        return;
    }

    {
        std::stringstream ss;
        ss << "Devices: " << listenerHandler->identityService->size();
        listenerHandler->javaLGWEvent.onInfo(listenerHandler->listener, LOG_INFO, LOG_MAIN_FUNC, 0, ss.str());
    }

    // set regional settings
    GatewayConfigMem gwSettings;
    memSetupMemGatewaySettingsStorage[regionIdx].setup(gwSettings.storage);

    // read only, deny send message and deny send beacon
    int flags = FLAG_GATEWAY_LISTENER_NO_SEND | FLAG_GATEWAY_LISTENER_NO_BEACON;
    int r = listenerHandler->listener->listen(
            memSetupMemGatewaySettingsStorage[regionIdx].name, regionIdx, &gwSettings, flags,
            &javeThreadStartFinish);
    if (r) {
        std::stringstream ss2;
        ss2 << ERR_MESSAGE << r << ": " << strerror_lorawan_ns(r) << std::endl;
        listenerHandler->javaLGWEvent.onInfo(nullptr, LOG_ERR, LOG_MAIN_FUNC, r, ss2.str());
    } else
        listenerHandler->javaLGWEvent.onFinished("Successfully finished");
    delete listenerHandler;
    listenerHandler = nullptr;
    jVM->DetachCurrentThread();
}

static std::thread *gwThread = nullptr;

extern "C" JNIEXPORT jint JNICALL Java_com_commandus_lgw_LorawanGatewayRak2287_start(
    JNIEnv* env,
    jobject /* this */,
    jint regionIdx,
    jstring gwIdString,
    jint verbosity
)
{
    std::string id = jstring2string(env, gwIdString);
    uint64_t gwId = std::stoull(id, nullptr, 16);
    gwThread = new std::thread(run, gwId, regionIdx, verbosity);
    gwThread->detach();
    return 0;
}

extern "C" JNIEXPORT void JNICALL Java_com_commandus_lgw_LorawanGatewayRak2287_stop(
    JNIEnv* env,
    jobject /* this */
)
{
    if (!listenerHandler)
        return;
    if (!listenerHandler->listener)
        return;
    int r = listenerHandler->listener->listener.stop(0);
    if (r) {
        std::stringstream ss3;
        ss3 << "Stop error " << r;
        listenerHandler->javaLGWEvent.onInfo(nullptr, LOG_ERR, LOG_MAIN_FUNC, r, ss3.str());
    }
}

/**
 * Get list of regional settings names
 */
extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_commandus_lgw_LorawanGatewayRak2287_regionNames(
    JNIEnv *env,
    jobject thiz
) {
    bool requireDetach;
    JNIEnv *jEnv = getJavaEnv(requireDetach);
    if (!jEnv)
        return nullptr;
    jsize sz = sizeof(memSetupMemGatewaySettingsStorage) / sizeof(setupMemGatewaySettingsStorage);
    if (!sz) {
        if (requireDetach)
            jVM->DetachCurrentThread();
        return nullptr;
    }
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

    jint jCount = (jint) count;
    // jbyteArray
    auto a = (jbyteArray) jEnv->CallObjectMethod(loggerObject, android_LGW_onRead, jCount);
    if (!a) {
        if (requireDetach)
            jVM->DetachCurrentThread();
        return -1;
    }

    jbyte *ae = jEnv->GetByteArrayElements(a, nullptr);
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
    jbyteArray jb = jEnv->NewByteArray((jsize) count);
    if (!jb) {
        if (requireDetach)
            jVM->DetachCurrentThread();
        return -1;
    }
    jEnv->SetByteArrayRegion(jb, 0, (jsize)count, (jbyte*) buf);

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
        for (unsigned char & i : retval->c_cc) {
            i = 0;    // special characters
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
    if (!jEnv) {
        return -1;
    }

    // blocking mode
    jboolean blocking = termios_p->c_cc[VMIN] != 0;
    int r = jEnv->CallIntMethod(loggerObject, android_LGW_onSetAttr, blocking);

    if (requireDetach)
        jVM->DetachCurrentThread();

    return r;
}

static void runFake(
    uint64_t gatewayIdentifier,
    size_t regionIdx,
    int verbosity
) {
    listenerHandler = new AndroidGatewayHandler();

    listenerHandler->listener->listener.stopRequest = false;

    libLoragwHelper.bind(&listenerHandler->javaLGWEvent, listenerHandler->libLoragwOpenClose);
    listenerHandler->identityService->init("", &listenerHandler->javaLGWEvent);

    listenerHandler->javaLGWEvent.onStarted(gatewayIdentifier,
                     memSetupMemGatewaySettingsStorage[regionIdx].name, regionIdx);

    if (!libLoragwHelper.onOpenClose) {
        listenerHandler->javaLGWEvent.onFinished("No open/close");
        delete listenerHandler;
        listenerHandler = nullptr;
        jVM->DetachCurrentThread();
        return;
    }

    if (!listenerHandler->identityService) {
        std::stringstream ss;
        ss << ERR_MESSAGE << ERR_CODE_FAIL_IDENTITY_SERVICE << ": " << ERR_FAIL_IDENTITY_SERVICE
           << memSetupMemGatewaySettingsStorage[regionIdx].name
           << " (settings #" << regionIdx << ")";
        listenerHandler->javaLGWEvent.onFinished(ss.str());
        delete listenerHandler;
        listenerHandler = nullptr;
        jVM->DetachCurrentThread();
        return;
    }

    {
        std::stringstream ss;
        ss << "Devices: " << listenerHandler->identityService->size();
        listenerHandler->javaLGWEvent.onInfo(listenerHandler->listener, LOG_INFO, LOG_MAIN_FUNC, 0, ss.str());
    }

    GatewayConfigMem gwSettings;
    // set regional settings
    memSetupMemGatewaySettingsStorage[regionIdx].setup(gwSettings.storage);

    if (!listenerHandler->listener || !listenerHandler->listener->onLog) {
        std::stringstream ss;
        ss << ERR_MESSAGE << ERR_CODE_INSUFFICIENT_MEMORY << ": " << ERR_INSUFFICIENT_MEMORY << std::endl;
        listenerHandler->javaLGWEvent.onFinished(ss.str());
        delete listenerHandler;
        listenerHandler = nullptr;
        jVM->DetachCurrentThread();
        return;
    }

    std::stringstream ss;
    ss << "Listening, region "
       << memSetupMemGatewaySettingsStorage[regionIdx].name << std::endl;

    listenerHandler->javaLGWEvent.onInfo(listenerHandler->listener, LOG_INFO, LOG_MAIN_FUNC, 0, ss.str());

    int r = 0;

    size_t cnt = 0;
    while (!listenerHandler->listener->listener.stopRequest) {
        if ((cnt % 60) == 0) {
            Payload p;
            p.received = time(nullptr);
            p.eui = "4242";
            p.eui = "42424242";
            p.devName = "fake-dev";
            p.frequency = 888888;
            p.rssi = 1;
            p.lsnr = 2;
            p.payload = "fake";
            listenerHandler->javaLGWEvent.onValue(p);
        }
        sleep(1);
        cnt++;
    }
    if (r) {
        std::stringstream ss4;
        ss4 << ERR_MESSAGE << r << ": " << strerror_lorawan_ns(r) << std::endl;
        listenerHandler->javaLGWEvent.onInfo(listenerHandler->listener, LOG_ERR,
        LOG_MAIN_FUNC, r, ss4.str());
    } else
        listenerHandler->javaLGWEvent.onFinished("Successfully finished");
    delete listenerHandler;
    listenerHandler = nullptr;
    jVM->DetachCurrentThread();
}

extern "C" JNIEXPORT jint JNICALL Java_com_commandus_lgw_LorawanGatewayFake_start(
    JNIEnv* env,
    jobject /* this */,
    jint regionIdx,
    jstring gwIdString,
    jint verbosity
)
{
    std::string id = jstring2string(env, gwIdString);
    uint64_t gwId = std::stoull(id, nullptr, 16);
    gwThread = new std::thread(runFake, gwId, regionIdx, verbosity);
    gwThread->detach();
    return 0;
}

extern "C" JNIEXPORT void JNICALL Java_com_commandus_lgw_LorawanGatewayFake_stop(
    JNIEnv* env,
    jobject /* this */
)
{
    if (listenerHandler && listenerHandler->listener)
        listenerHandler->listener->listener.stopRequest = true;
}
