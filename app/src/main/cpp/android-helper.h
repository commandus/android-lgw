#include <sys/types.h>

#ifdef __cplusplus
extern "C" {
#endif


int open_c(const char *file, int flags);

int close_c(int fd);

void printf_c(const char *fmt);

ssize_t read_c(int fd, void *buf, size_t count);

ssize_t write_c(int fd, const void *buf, size_t nbytes);

#ifdef __cplusplus
}
#endif
