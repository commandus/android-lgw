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
INC_DIR=$DST/packet_forwarder
mkdir -p $INC_DIR

LORAWAN_NETWORK_SERVER_DIR=~/src/lorawan-network-server
DST_ULGW=$DST/ulgw
mkdir -p $DST_ULGW

cp $LORAWAN_NETWORK_SERVER_DIR/device-history-service-abstract.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/gateway-lora.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/identity-service.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/lora-encrypt.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/lora-radio.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/log-intf.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/packet-listener.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/usb-listener.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/utilstring.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/dev-addr.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/errlist.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/gateway-settings.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/lora-gateway-listener.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/lorawan-mac.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/platform.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/utildate.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/utilthread.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/device-history-item.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/gateway-list.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/gateway-stat.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/lora-packet-handler-abstract.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/net-id.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/udp-socket.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/utillora.h $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/libloragw-helper.h $DST_ULGW

cp $LORAWAN_NETWORK_SERVER_DIR/errlist.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/gateway-stat.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/lorawan-mac.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/udp-socket.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/utillora.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/dev-addr.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/gateway-list.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/gateway_usb_conf.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/lora-encrypt.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/net-id.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/usb-listener.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/utilstring.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/device-history-item.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/gateway-settings.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/identity-service.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/lora-gateway-listener.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/packet-listener.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/utildate.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/utilthread.cpp $DST_ULGW
cp $LORAWAN_NETWORK_SERVER_DIR/libloragw-helper.cpp $DST_ULGW

cp $LIBLORAGW_INC_DIR/config.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_aux.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_debug.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_i2c.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_reg.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_sx1250.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_sx1302.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_usb.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx1250_spi.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx125x_spi.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx1261_spi.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_ad5338r.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_cal.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_gps.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_lbt.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_spi.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_sx125x.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_sx1302_rx.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx1250_com.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx1250_usb.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx1261_com.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx1261_usb.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_agc_params.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_com.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_hal.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_mcu.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_stts751.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_sx1261.h $INC_DIR
cp $LIBLORAGW_INC_DIR/loragw_sx1302_timestamp.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx1250_defs.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx125x_com.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx1261_defs.h $INC_DIR
cp $LIBLORAGW_INC_DIR/sx1261_defs.h $INC_DIR

cp $LIBLORAGW_SRC_DIR/sx1261_pram.var $DST/libloragw
cp $LIBLORAGW_SRC_DIR/arb_fw.var $DST/libloragw
cp $LIBLORAGW_SRC_DIR/agc_fw_sx1250.var $DST/libloragw
cp $LIBLORAGW_SRC_DIR/agc_fw_sx1257.var $DST/libloragw

cp $LIBLORAGW_SRC_DIR/cal_fw.var $DST/libloragw
cp $LIBTINYMT32_INC_DIR/tinymt32.h $INC_DIR
cp $JITQUEUE_INC_DIR/trace.h $INC_DIR
cp $JITQUEUE_INC_DIR/jitqueue.h $INC_DIR


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

search_replaces[0]='fprintf\(stdout, str\)|printf_c("%s", str)'
search_replaces[1]='fprintf\(stderr, args\)|printf_c("%s", args)'
search_replaces[2]='fprintf\(stdout,|printf_c('
search_replaces[3]='fprintf\(stderr,|printf_c('
search_replaces[4]=' open\(|open_c('
search_replaces[5]=' close\(|close_c('
search_replaces[6]='tcgetattr\(|tcgetattr_c('
search_replaces[7]='tcsetattr\(|tcsetattr_c('
# search_replaces[8]='printf|printf_c'

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

# Replace write() to write_c(), read() to read_c()
patch $DST/libloragw/loragw_mcu.c -i loragw_mcu.c.patch

# Replace read() to read_c()
patch $DST/libloragw/loragw_usb.c -i loragw_usb.c.patch

# Make C++ friendly
patch $INC_DIR/loragw_hal.h -i loragw_hal.h.patch
exit 0

