#!/bin/bash
#!/bin/sh
DST=.
RAK_COMMON_FOR_GATEWAY_ROOT=~/git/1/rak_common_for_gateway
RAK_2287=$RAK_COMMON_FOR_GATEWAY_ROOT/lora/rak2287/sx1302_hal
LIBLORAGW_SRC_DIR=$RAK_2287/libloragw/src
LIBLORAGW_INC_DIR=$RAK_2287/libloragw/inc
LIBTINYMT32_SRC_DIR=$RAK_2287/libtools/src
LIBTINYMT32_INC_DIR=$RAK_2287/libtools/inc
JITQUEUE_SRC_DIR=$RAK_2287/packet_forwarder/src
JITQUEUE_INC_DIR=$RAK_2287/packet_forwarder/inc
mkdir -p $DST/inc

cp $LIBLORAGW_INC_DIR/config.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_aux.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_debug.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_i2c.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_reg.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_sx1250.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_sx1302.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_usb.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx1250_spi.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx125x_spi.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx1261_spi.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_ad5338r.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_cal.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_gps.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_lbt.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_spi.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_sx125x.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_sx1302_rx.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx1250_com.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx1250_usb.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx1261_com.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx1261_usb.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_agc_params.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_com.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_hal.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_mcu.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_stts751.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_sx1261.h $DST/inc
cp $LIBLORAGW_INC_DIR/loragw_sx1302_timestamp.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx1250_defs.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx125x_com.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx1261_defs.h $DST/inc
cp $LIBLORAGW_INC_DIR/sx1261_defs.h $DST/inc

cp $LIBLORAGW_SRC_DIR/sx1261_pram.var $DST/libloragw
cp $LIBLORAGW_SRC_DIR/arb_fw.var $DST/libloragw
cp $LIBLORAGW_SRC_DIR/agc_fw_sx1250.var $DST/libloragw
cp $LIBLORAGW_SRC_DIR/agc_fw_sx1257.var $DST/libloragw

cp $LIBLORAGW_SRC_DIR/cal_fw.var $DST/libloragw
cp $LIBTINYMT32_INC_DIR/tinymt32.h $DST/inc
cp $JITQUEUE_INC_DIR/trace.h $DST/inc
cp $JITQUEUE_INC_DIR/jitqueue.h $DST/inc


cp $LIBLORAGW_SRC_DIR/loragw_spi.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_usb.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_com.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_mcu.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_i2c.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/sx125x_spi.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/sx125x_com.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/sx1250_spi.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/sx1250_usb.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/sx1250_com.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/sx1261_spi.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/sx1261_usb.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/sx1261_com.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_aux.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_reg.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_sx1250.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_sx1261.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_sx125x.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_sx1302.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_cal.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_debug.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_hal.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_lbt.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_stts751.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_gps.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_sx1302_timestamp.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_sx1302_rx.c $DST/libloragw
cp $LIBLORAGW_SRC_DIR/loragw_ad5338r.c $DST/libloragw


cp $LIBTINYMT32_SRC_DIR/tinymt32.c $DST/libtinymt32
cp $JITQUEUE_SRC_DIR/jitqueue.c $DST/jitqueue

declare -a search_replace

search_replaces[0]='fprintf\(stdout,|printf_c('
search_replaces[1]='fprintf\(stderr,|printf_c('
search_replaces[2]=' open\(|open_c('
search_replaces[3]=' close\(|close_c('

for search_replace in "${search_replaces[@]}"
do
    IFS="|" read -r -a arr <<< "${search_replace}"
    search="${arr[0]}"
    replace="${arr[1]}"

    for file in jitqueue/* libloragw/* libtinymt32/*
    do
    if [[ -f $file ]]; then
        cat $file | awk -v var1="$search" -v var2="$replace" '{ gsub(var1, var2); print; }'> file.tmp
        rm -rf $file  
        mv file.tmp $file
    fi
    done
done

exit 0

