#include <fstream>
#include "AndroidIdentityService.h"
#include "errlist.h"

int AndroidIdentityService::load()
{
    clear();
    FILE* fp = fopen(path.c_str(), "rb");
    int r = 0;
    if (!fp)
        return ERR_CODE_INVALID_JSON;
    fclose(fp);
    return r;
}

int AndroidIdentityService::save()
{
    std::fstream os;
    os.open(path.c_str(), std::ios::out);
    for (std::map<DEVADDRINT, DEVICEID>::const_iterator it = storage.begin(); it != storage.end(); it++) {
        os
           << DEVADDRINT2string(it->first) << "\t"
           << (int) (it->second.activation) << "\t"
           << DEVEUI2string(it->second.devEUI) << "\t"
           << KEY2string(it->second.nwkSKey) << "\t"
           << KEY2string(it->second.appSKey) << "\t"
           << deviceclass2string(it->second.deviceclass) << "\t"
           << LORAWAN_VERSION2string(it->second.version) << "\t"
           << DEVEUI2string(it->second.appEUI) << "\t"
           << KEY2string(it->second.appKey) << "\t"
           << KEY2string(it->second.nwkKey) << "\t"
           << DEVNONCE2string(it->second.devNonce) << "\t"
           << JOINNONCE2string(it->second.joinNonce) << "\t"
           << DEVICENAME2string(it->second.name) << "\t";
        os << "\n";
    }
    int r = os.bad() ? ERR_CODE_OPEN_DEVICE : 0;
    os.close();
    return r;
}

void AndroidIdentityService::clear()
{
}

/**
  * Return next network address if available
  * @return 0- success, ERR_ADDR_SPACE_FULL- no address available
  */
int AndroidIdentityService::nextBruteForce(NetworkIdentity &retval)
{
    return 0;
}

AndroidIdentityService::AndroidIdentityService()
{
}

AndroidIdentityService::~AndroidIdentityService()
{
}

int AndroidIdentityService::get(DeviceId &retval, DEVADDR &devaddr)
{
    return 0;
}

int AndroidIdentityService::getNetworkIdentity(NetworkIdentity &retval, const DEVEUI &eui)
{
    return 0;
}

void AndroidIdentityService::list(std::vector<NetworkIdentity> &retval, size_t offset, size_t size)
{
}

// Entries count
size_t AndroidIdentityService::size()
{

}

void AndroidIdentityService::put(DEVADDR &devaddr, DEVICEID &id)
{
}

void AndroidIdentityService::rm(DEVADDR &addr)
{
}

int AndroidIdentityService::init(const std::string &option, void *data)
{
    path = option;
    return 0;
}

void AndroidIdentityService::flush()
{
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
    return 0;
}

int AndroidIdentityService::parseNames(
    std::vector<TDEVEUI> &retval,
    const std::vector<std::string> &list,
    bool useRegex
)
{
    return 0;
}

bool AndroidIdentityService::canControlService(
    const DEVADDR &addr
)
{
    return true;
}

/**
  * Return next network address if available
  * @return 0- success, ERR_ADDR_SPACE_FULL- no address available
  */
int AndroidIdentityService::next(NetworkIdentity &retVal)
{
    return 0;
}
