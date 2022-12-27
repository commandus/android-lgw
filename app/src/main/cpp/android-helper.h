// #define open(file, flags, ...) open_c(file, flags)
// #define close(fd) close_c(fd)
// #define printf(args...) printf_c(args)
// #define fprintf(fd, fmt, args...) printf_c(fmt, args)

extern "C" {

int open_c(const char *file, int flags);

int close_c(int fd);

}