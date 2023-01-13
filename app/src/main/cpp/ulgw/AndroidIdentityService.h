#ifndef ANDROIDIDENTITYSERVICE_H
#define ANDROIDIDENTITYSERVICE_H

#include <map>
#include <string>
#include <mutex>
#include <vector>

#include "identity-service.h"

class AndroidIdentityService: public IdentityService {
private:
    int load();
    int save();
    std::mutex mutexMap;
    // assigned addresses
    std::map<DEVADDRINT, DEVICEID, DEVADDRINTCompare> storage;
    // devices which has special rights
    std::map<DEVADDRINT, uint32_t, DEVADDRINTCompare> rightsMask;
    std::string path;

    void clear();
    /**
      * Return next network address if available
      * @return 0- success, ERR_ADDR_SPACE_FULL- no address available
      */
    int nextBruteForce(NetworkIdentity &retval);
public:
    // helper data
    // helps to find out free address in the space
    uint32_t maxDevNwkAddr;

    AndroidIdentityService();
    ~AndroidIdentityService();
    int get(DeviceId &retval, DEVADDR &devaddr) override;
    int getNetworkIdentity(NetworkIdentity &retval, const DEVEUI &eui) override;
    // List entries
    void list(std::vector<NetworkIdentity> &retval, size_t offset, size_t size) override;
    // Entries count
    size_t size() override;
    void put(DEVADDR &devaddr, DEVICEID &id) override;
    void rm(DEVADDR &addr) override;

    int init(const std::string &option, void *data) override;
    void flush() override;
    void done() override;
    int parseIdentifiers(
        std::vector<TDEVEUI> &retval,
        const std::vector<std::string> &list,
        bool useRegex
    ) override;
    int parseNames(
        std::vector<TDEVEUI> &retval,
        const std::vector<std::string> &list,
        bool useRegex
    ) override;
    bool canControlService(
        const DEVADDR &addr
    ) override;

    /**
      * Return next network address if available
      * @return 0- success, ERR_ADDR_SPACE_FULL- no address available
      */
    int next(NetworkIdentity &retVal) override;
};

#endif
