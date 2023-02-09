#ifndef ANDROIDIDENTITYSERVICE_H
#define ANDROIDIDENTITYSERVICE_H

#include <map>
#include <string>
#include <mutex>
#include <vector>

#include "identity-service.h"
#include "log-intf.h"

class AndroidIdentityService: public IdentityService {
private:
    LogIntf *androidCb;
public:
    AndroidIdentityService();
    ~AndroidIdentityService();
    int get(DeviceId &retval, DEVADDR &devaddr) override;
    int getNetworkIdentity(NetworkIdentity &retval, const DEVEUI &eui) override;
    // List entries
    void list(std::vector<NetworkIdentity> &retval, size_t offset, size_t size) override;
    // Entries count
    size_t size() override;
    // No implementation required
    void put(DEVADDR &devaddr, DEVICEID &id) override;
    // No implementation required
    void rm(DEVADDR &addr) override;

    /**
     * Set Android callbacks
     * @param option not used
     * @param data LogIntf*
     * @return 0 always
     */
    int init(const std::string &option, void *data) override;
    // No implementation required
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
    // Always false
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
