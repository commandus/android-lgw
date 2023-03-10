#ifdef _MSC_VER
#define _CRT_SECURE_NO_WARNINGS
#endif

#include <string.h>
#include "errlist.h"

#define ERR_COUNT 173

// used by strerror_lorawan_ns()
static const char *errList[ERR_COUNT] = {
	ERR_COMMAND_LINE,
	ERR_OPEN_DEVICE,
	ERR_CLOSE_DEVICE,
	ERR_BAD_STATUS,
	ERR_INVALID_PAR_LOG_FILE,
	ERR_INVALID_SERVICE,
	ERR_INVALID_GATEWAY_ID,
	ERR_INVALID_DEVICE_EUI,
	ERR_INVALID_BUFFER_SIZE,
	ERR_GRPC_NETWORK_SERVER_FAIL,
	ERR_INVALID_RFM_HEADER,
	ERR_INVALID_ADDRESS,
	ERR_INVALID_FAMILY,
	ERR_SOCKET_CREATE,
	ERR_SOCKET_BIND,
	ERR_SOCKET_OPEN,
	ERR_SOCKET_CLOSE,
	ERR_SOCKET_READ,
	ERR_SOCKET_WRITE,
	ERR_SOCKET_NO_ONE,
    ERR_SOCKET_CONNECT,
    ERR_SOCKET_ADDRESS,
    ERR_SELECT,
	ERR_INVALID_PACKET,
	ERR_INVALID_JSON,
	ERR_DEVICE_ADDRESS_NOTFOUND,
	ERR_FAIL_IDENTITY_SERVICE,
	ERR_LMDB_TXN_BEGIN,
	ERR_LMDB_TXN_COMMIT,
	ERR_LMDB_OPEN,
	ERR_LMDB_CLOSE,
	ERR_LMDB_PUT,
	ERR_LMDB_PUT_PROBE,
	ERR_LMDB_GET,
	ERR_WRONG_PARAM,
	ERR_INSUFFICIENT_MEMORY,
	ERR_NO_CONFIG,
	ERR_SEND_ACK,
	ERR_NO_GATEWAY_STAT,
	ERR_INVALID_PROTOCOL_VERSION,
	ERR_PACKET_TOO_SHORT,
	ERR_PARAM_NO_INTERFACE,
	ERR_MAC_TOO_SHORT,
	ERR_MAC_INVALID,
	ERR_MAC_UNKNOWN_EXTENSION,
    ERR_PARAM_INVALID,
	ERR_INSUFFICIENT_PARAMS,
	ERR_NO_MAC_NO_PAYLOAD,
	ERR_INVALID_REGEX,
	ERR_NO_DATABASE,
	ERR_LOAD_PROTO,
	ERR_LOAD_DATABASE_CONFIG,
	ERR_DB_SELECT,
	ERR_DB_DATABASE_NOT_FOUND,
	ERR_DB_DATABASE_OPEN,
	ERR_DB_DATABASE_CLOSE,
	ERR_DB_CREATE,
	ERR_DB_INSERT,
	ERR_DB_START_TRANSACTION,
	ERR_DB_COMMIT_TRANSACTION,
	ERR_DB_EXEC,
	ERR_PING,
	ERR_PULLOUT,
	ERR_INVALID_STAT,
	ERR_NO_PAYLOAD,
	ERR_NO_MESSAGE_TYPE,
	ERR_QUEUE_EMPTY,
	ERR_RM_FILE,
	ERR_INVALID_BASE64,
	ERR_MISSED_DEVICE,
	ERR_MISSED_GATEWAY,
	ERR_INVALID_FPORT,
	ERR_INVALID_MIC,
	ERR_SEGMENTATION_FAULT,
    ERR_ABRT,
	ERR_BEST_GATEWAY_NOT_FOUND,
	ERR_REPLY_MAC,
	ERR_NO_MAC,
	ERR_NO_DEVICE_STAT,
	ERR_INIT_DEVICE_STAT,
	ERR_INIT_IDENTITY,
	ERR_INIT_QUEUE,
	ERR_HANGUP_DETECTED,
	ERR_NO_FCNT_DOWN,
	ERR_CONTROL_NOT_AUTHORIZED,
	ERR_GATEWAY_NOT_FOUND,
	ERR_CONTROL_DEVICE_NOT_FOUND,
	ERR_INVALID_CONTROL_PACKET,
    ERR_DUPLICATED_PACKET,
    ERR_INIT_GW_STAT,
    ERR_DEVICE_NAME_NOT_FOUND,
    ERR_DEVICE_EUI_NOT_FOUND,
    ERR_JOIN_EUI_NOT_MATCHED,
    ERR_GATEWAY_NO_YET_PULL_DATA,
    ERR_REGION_BAND_EMPTY,
    ERR_INIT_REGION_BANDS,
    ERR_INIT_REGION_NO_DEFAULT,
    ERR_NO_REGION_BAND,
    ERR_REGION_BAND_NO_DEFAULT,
    ERR_IS_JOIN,
    ERR_BAD_JOIN_REQUEST,
    ERR_NETID_OR_NETTYPE_MISSED,
	ERR_NETTYPE_OUT_OF_RANGE,
	ERR_NETID_OUT_OF_RANGE,
	ERR_TYPE_OUT_OF_RANGE,
	ERR_NWK_OUT_OF_RANGE,
	ERR_ADDR_OUT_OF_RANGE,
    ERR_ADDR_SPACE_FULL,
    ERR_INIT_LOGGER_HUFFMAN_PARSER,
	ERR_WS_START_FAILED,
	ERR_NO_DEFAULT_WS_DATABASE,
	ERR_INIT_LOGGER_HUFFMAN_DB,
    ERR_NO_PACKET_PARSER,
    ERR_LOAD_WS_PASSWD_NOT_FOUND,

    ERR_LORA_GATEWAY_CONFIGURE_BOARD_FAILED,
    ERR_LORA_GATEWAY_CONFIGURE_TIME_STAMP,
    ERR_LORA_GATEWAY_CONFIGURE_SX1261_RADIO,
    ERR_LORA_GATEWAY_CONFIGURE_TX_GAIN_LUT,
    ERR_LORA_GATEWAY_CONFIGURE_INVALID_RADIO,
    ERR_LORA_GATEWAY_CONFIGURE_DEMODULATION,
    ERR_LORA_GATEWAY_CONFIGURE_MULTI_SF_CHANNEL,
    ERR_LORA_GATEWAY_CONFIGURE_STD_CHANNEL,
    ERR_LORA_GATEWAY_CONFIGURE_FSK_CHANNEL,
    ERR_LORA_GATEWAY_CONFIGURE_DEBUG,
    ERR_LORA_GATEWAY_CONFIGURE_GPS_FAILED,

    ERR_LORA_GATEWAY_START_FAILED,
    ERR_LORA_GATEWAY_GET_EUI,
    ERR_LORA_GATEWAY_GPS_GET_TIME,
    ERR_LORA_GATEWAY_GPS_SYNC_TIME,
    ERR_LORA_GATEWAY_GPS_DISABLED,
    ERR_LORA_GATEWAY_GPS_GET_COORDS,

    ERR_LORA_GATEWAY_SPECTRAL_SCAN_START_FAILED,
    ERR_LORA_GATEWAY_SPECTRAL_SCAN_TIMEOUT,
    ERR_LORA_GATEWAY_SPECTRAL_SCAN_FAILED,
    ERR_LORA_GATEWAY_SPECTRAL_SCAN_ABORTED,
    ERR_LORA_GATEWAY_SPECTRAL_SCAN_UNEXPECTED_STATUS,
    ERR_LORA_GATEWAY_GET_TX_STATUS,
    ERR_LORA_GATEWAY_SKIP_SPECTRAL_SCAN,

    ERR_LORA_GATEWAY_STATUS_FAILED,
    ERR_LORA_GATEWAY_EMIT_ALLREADY,
    ERR_LORA_GATEWAY_SCHEDULED_ALLREADY,
    ERR_LORA_GATEWAY_SPECTRAL_SCAN_ABORT_FAILED,
    ERR_LORA_GATEWAY_SEND_FAILED,
    ERR_LORA_GATEWAY_SENT,
    ERR_LORA_GATEWAY_JIT_DEQUEUE_FAILED,
    ERR_LORA_GATEWAY_JIT_PEEK_FAILED,
    ERR_LORA_GATEWAY_JIT_ENQUEUE_FAILED,
    ERR_LORA_GATEWAY_FETCH,
    ERR_LORA_GATEWAY_UNKNOWN_STATUS,
    ERR_LORA_GATEWAY_UNKNOWN_DATARATE,
    ERR_LORA_GATEWAY_UNKNOWN_BANDWIDTH,
    ERR_LORA_GATEWAY_UNKNOWN_CODERATE,
    ERR_LORA_GATEWAY_UNKNOWN_MODULATION,
    ERR_LORA_GATEWAY_RECEIVED,
    ERR_LORA_GATEWAY_AUTOQUIT_THRESHOLD,
    ERR_LORA_GATEWAY_BEACON_FAILED,
    ERR_LORA_GATEWAY_UNKNOWN_TX_MODE,
    ERR_LORA_GATEWAY_SEND_AT_GPS_TIME,
    ERR_LORA_GATEWAY_SEND_AT_GPS_TIME_DISABLED,
    ERR_LORA_GATEWAY_SEND_AT_GPS_TIME_INVALID,
    ERR_LORA_GATEWAY_TX_CHAIN_DISABLED,
    ERR_LORA_GATEWAY_TX_UNSUPPORTED_FREQUENCY,
    ERR_LORA_GATEWAY_TX_UNSUPPORTED_POWER,
    ERR_LORA_GATEWAY_USB_NOT_FOUND,
    ERR_LORA_GATEWAY_SHUTDOWN_TIMEOUT,
    ERR_LORA_GATEWAY_STOP_FAILED,
    ERR_INIT_PLUGINS_FAILED,
    ERR_LOAD_PLUGINS_FAILED,
    ERR_PLUGIN_MQTT_CONNECT,
    ERR_PLUGIN_MQTT_DISCONNECT,
    ERR_PLUGIN_MQTT_SEND,
    ERR_UNIDENTIFIED_MESSAGE,
    ERR_LORA_GATEWAY_SPECTRAL_SCAN_RESULT
};

const char *strerror_lorawan_ns
(
	int errcode
)
{
	if ((errcode <= -500) && (errcode >= -500 - ERR_COUNT))
	{
		return errList[-(errcode + 500)];
	}
	return strerror(errcode);
}

#define LOG_LEVEL_COUNT	8

static const char *logLevelList[LOG_LEVEL_COUNT] = 
{
	"",
	"F", // "fatal",
	"C", // "critical",
	"E", // "error",
	"W", // "warning",
	"I", // "info",
	"I", // "info",
	"D", // "debug"
};

static const char *logLevelColorList[LOG_LEVEL_COUNT] = 
{
	"",
	"0;41",  // background red
	"0;41",  // background red
	"0;31",  // red
	"0;33 ", // yellow
	"0;37",  // white
	"0;37",  // bright white
	"0;32"   // green
};

const char *logLevelString
(
	int logLevel
)
{
	if (logLevel < 0)
		logLevel = 0;
	if (logLevel > LOG_LEVEL_COUNT)
		logLevel = 0;
	return logLevelList[logLevel];
}

const char *logLevelColor
(
	int logLevel
)
{
	if (logLevel < 0)
		logLevel = 0;
	if (logLevel > LOG_LEVEL_COUNT)
		logLevel = 0;
	return logLevelColorList[logLevel];
}
