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
    append2logfile("AndroidIdentityService");
}

AndroidIdentityService::~AndroidIdentityService()
{
    append2logfile("~AndroidIdentityService");
}

int AndroidIdentityService::get(DeviceId &retval, DEVADDR &devaddr)
{
    append2logfile("get");
    std::string s = DEVADDR2string(devaddr);
append2logfile(s.c_str());
    if (androidCb) {
        return androidCb->identityGet(retval, devaddr);
    }
    return 0;
}

int AndroidIdentityService::getNetworkIdentity(NetworkIdentity &retval, const DEVEUI &eui)
{
    append2logfile("getNetworkIdentity");
    std::string s = DEVEUI2string(eui);
    append2logfile(s.c_str());
    if (androidCb) {
        return androidCb->identityGetNetworkIdentity(retval, eui);
    }
    return 0;
}

void AndroidIdentityService::list(std::vector<NetworkIdentity> &retval, size_t offset, size_t size)
{
    append2logfile("list");
    // No implementation required
}

// Entries count
size_t AndroidIdentityService::size()
{
    append2logfile("size");
    if (androidCb) {
        return androidCb->identitySize();
    }
    return 0;
}

void AndroidIdentityService::put(DEVADDR &devaddr, DEVICEID &id)
{
    append2logfile("put");
    // No implementation required
}

void AndroidIdentityService::rm(DEVADDR &addr)
{
    append2logfile("rm");
    // No implementation required
}

int AndroidIdentityService::init(const std::string &option, void *data)
{
    append2logfile("init");
    androidCb = (LogIntf *) data;
    return 0;
}

void AndroidIdentityService::flush()
{
    append2logfile("flush");
    // No implementation required
}

void AndroidIdentityService::done()
{
    append2logfile("done");
}

int AndroidIdentityService::parseIdentifiers(
    std::vector<TDEVEUI> &retval,
    const std::vector<std::string> &list,
    bool useRegex
)
{
    append2logfile("parseIdentifiers");
    // No implementation required
    return 0;
}

int AndroidIdentityService::parseNames(
    std::vector<TDEVEUI> &retval,
    const std::vector<std::string> &list,
    bool useRegex
)
{
    append2logfile("parseNames");
    // No implementation required
    return 0;
}

bool AndroidIdentityService::canControlService(
    const DEVADDR &addr
)
{
    append2logfile("canControlService");
    // Always false
    return false;
}

/**
  * Return next network address if available
  * @return 0- success, ERR_ADDR_SPACE_FULL- no address available
  */
int AndroidIdentityService::next(NetworkIdentity &retVal)
{
    append2logfile("next");
    // No implementation required
    return 0;
}
