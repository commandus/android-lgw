#include <fstream>
#include "AndroidIdentityService.h"
#include "errlist.h"

static void append2logfile(const char *fmt) {
    FILE *f = fopen("/storage/emulated/0/Android/data/com.commandus.lgw/files/Documents/lgw.log", "a+");
    if (f != nullptr) {
        fprintf(f, "%s\r\n", fmt);
        fclose(f);
    }
}

AndroidIdentityService::AndroidIdentityService()
    : androidCb(nullptr)
{
}

AndroidIdentityService::~AndroidIdentityService()
{
}

int AndroidIdentityService::get(DeviceId &retval, DEVADDR &devaddr)
{
    std::string s = DEVADDR2string(devaddr);
    if (androidCb) {
        return androidCb->identityGet(retval, devaddr);
    }
    return ERR_CODE_DEVICE_ADDRESS_NOTFOUND;
}

int AndroidIdentityService::getNetworkIdentity(NetworkIdentity &retval, const DEVEUI &eui)
{
    std::string s = DEVEUI2string(eui);
    if (androidCb) {
        return androidCb->identityGetNetworkIdentity(retval, eui);
    }
    return ERR_CODE_DEVICE_EUI_NOT_FOUND;
}

void AndroidIdentityService::list(std::vector<NetworkIdentity> &retval, size_t offset, size_t size)
{
    // No implementation required
}

// Entries count
size_t AndroidIdentityService::size()
{
    if (androidCb) {
        return androidCb->identitySize();
    }
    return 0;
}

void AndroidIdentityService::put(DEVADDR &devaddr, DEVICEID &id)
{
    // No implementation required
}

void AndroidIdentityService::rm(DEVADDR &addr)
{
    // No implementation required
}

int AndroidIdentityService::init(const std::string &option, void *data)
{
    androidCb = (LogIntf *) data;
    return 0;
}

void AndroidIdentityService::flush()
{
    // No implementation required
}

void AndroidIdentityService::done()
{
}

int AndroidIdentityService::parseIdentifiers(
    std::vector<TDEVEUI> &retval,
    const std::vector<std::string> &list,
    bool useRegex
)
{
    // No implementation required
    return 0;
}

int AndroidIdentityService::parseNames(
    std::vector<TDEVEUI> &retval,
    const std::vector<std::string> &list,
    bool useRegex
)
{
    // No implementation required
    return 0;
}

bool AndroidIdentityService::canControlService(
    const DEVADDR &addr
)
{
    // Always false
    return false;
}

/**
  * Return next network address if available
  * @return 0- success, ERR_ADDR_SPACE_FULL- no address available
  */
int AndroidIdentityService::next(NetworkIdentity &retVal)
{
    // No implementation required
    return 0;
}
