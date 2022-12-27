#ifdef __cplusplus
extern "C" {
#endif

int open_c(const char *file, int flags);

int close_c(int fd);

void printf_c(const char *fmt, ...);

#ifdef __cplusplus
}
#endif
