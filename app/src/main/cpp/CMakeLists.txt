cmake_minimum_required(VERSION 3.18.1)

project("loragw")

set(LIBLORAGW_SRC_DIR libloragw)
set(LIBTINYMT32_SRC_DIR libtinymt32)
set(JITQUEUE_SRC_ROOT jitqueue)

# set(LIBLORAGW_INC_DIRS ${LIBTINYMT32_SRC_DIR} ${LIBLORAGW_SRC_DIR} ${JITQUEUE_SRC_ROOT})

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

)

add_library(loragw SHARED ${LIBLORAGW_SRC})
set_property(TARGET loragw PROPERTY C_STANDARD 99)
target_include_directories(loragw PRIVATE . inc)
target_compile_options(loragw PRIVATE -include subst.h)

find_library(log-lib log)

target_link_libraries(loragw ${log-lib})
