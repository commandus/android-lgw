#include <sys/types.h>
#include <termios.h>

#ifdef __cplusplus
extern "C" {
#endif

int open_c(const char *file, int flags);

int close_c(int fd);

#define printf_c(fmt, args...) { char line[4096]; snprintf(line, sizeof(line), fmt, args); printf_c1(line); }
void printf_c1(const char *s);

ssize_t read_c(int fd, void *buf, size_t count);

ssize_t write_c(int fd, const void *buf, size_t nbytes);

int tcgetattr_c(int fd, struct termios *retval);

int tcsetattr_c(int fd, int optional_actions, const struct termios *termios_p);

#ifdef __cplusplus
}
#endif
