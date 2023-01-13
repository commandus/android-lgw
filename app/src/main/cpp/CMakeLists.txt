cmake_minimum_required(VERSION 3.18.1)

project("loragw")

set(LIBLORAGW_SRC_DIR libloragw)
set(LIBTINYMT32_SRC_DIR libtinymt32)
set(JITQUEUE_SRC_ROOT jitqueue)

set(LIBLORAGW_SRC
    ${LIBLORAGW_SRC_DIR}/loragw_spi.c
    ${LIBLORAGW_SRC_DIR}/loragw_usb.c
    ${LIBLORAGW_SRC_DIR}/loragw_com.c
    ${LIBLORAGW_SRC_DIR}/loragw_mcu.c
    ${LIBLORAGW_SRC_DIR}/loragw_i2c.c
    ${LIBLORAGW_SRC_DIR}/sx125x_spi.c
    ${LIBLORAGW_SRC_DIR}/sx125x_com.c
    ${LIBLORAGW_SRC_DIR}/sx1250_spi.c
    ${LIBLORAGW_SRC_DIR}/sx1250_usb.c
    ${LIBLORAGW_SRC_DIR}/sx1250_com.c
    ${LIBLORAGW_SRC_DIR}/sx1261_spi.c
    ${LIBLORAGW_SRC_DIR}/sx1261_usb.c
    ${LIBLORAGW_SRC_DIR}/sx1261_com.c
    ${LIBLORAGW_SRC_DIR}/loragw_aux.c
    ${LIBLORAGW_SRC_DIR}/loragw_reg.c
    ${LIBLORAGW_SRC_DIR}/loragw_sx1250.c
    ${LIBLORAGW_SRC_DIR}/loragw_sx1261.c
    ${LIBLORAGW_SRC_DIR}/loragw_sx125x.c
    ${LIBLORAGW_SRC_DIR}/loragw_sx1302.c
    ${LIBLORAGW_SRC_DIR}/loragw_cal.c
    ${LIBLORAGW_SRC_DIR}/loragw_debug.c
    ${LIBLORAGW_SRC_DIR}/loragw_hal.c
    ${LIBLORAGW_SRC_DIR}/loragw_lbt.c
    ${LIBLORAGW_SRC_DIR}/loragw_stts751.c
    ${LIBLORAGW_SRC_DIR}/loragw_gps.c
    ${LIBLORAGW_SRC_DIR}/loragw_sx1302_timestamp.c
    ${LIBLORAGW_SRC_DIR}/loragw_sx1302_rx.c
    ${LIBLORAGW_SRC_DIR}/loragw_ad5338r.c

    ${LIBTINYMT32_SRC_DIR}/tinymt32.c
    ${JITQUEUE_SRC_ROOT}/jitqueue.c

    qsort.c
    android-helper.cpp

    ulgw/usb-listener.cpp
    ulgw/packet-listener.cpp
    ulgw/udp-socket.cpp
    ulgw/utillora.cpp
    ulgw/net-id.cpp
    ulgw/dev-addr.cpp
    ulgw/gateway-stat.cpp
    ulgw/identity-service.cpp
    ulgw/utildate.cpp
    ulgw/device-history-item.cpp
    ulgw/gateway-list.cpp
    ulgw/utilstring.cpp
    ulgw/errlist.cpp
    ulgw/lora-gateway-listener.cpp
    ulgw/gateway-settings.cpp
    ulgw/lora-encrypt.cpp
    ulgw/lorawan-mac.cpp
    ulgw/utilthread.cpp
    ulgw/libloragw-helper.cpp

    ulgw/AndroidIdentityService.cpp

    base64/base64.cpp
    system/crypto/aes.c
    system/crypto/cmac.c
    strptime.cpp
)

add_library(loragw SHARED ${LIBLORAGW_SRC})
set_property(TARGET loragw PROPERTY C_STANDARD 99)
target_include_directories(loragw PRIVATE . packet_forwarder ulgw rapidjson base64)
target_compile_options(loragw PRIVATE -include subst.h)

find_library(log-lib log)

target_link_libraries(loragw ${log-lib})
