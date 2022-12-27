#include "qsort.h"
/*
 * Isaac Turner 29 April 2014 Public Domain
 * @see https://github.com/noporpoise/sort_r/blob/master/sort_r.h
 */

#define SORT_R_SWAP(a,b,tmp) ((tmp) = (a), (a) = (b), (b) = (tmp))
static inline void sort_r_swap(
    char *__restrict a,
    char *__restrict b,
    size_t w
)
{
    char tmp, *end = a+w;
    for(; a < end; a++, b++) { SORT_R_SWAP(*a, *b, tmp); }
}

static inline int sort_r_cmpswap(
    char *__restrict a,
    char *__restrict b, size_t w,
    int (*compar)(
        const void *_a,
        const void *_b,
        void *_arg
    ),
    void *arg)
{
    if(compar(a, b, arg) > 0) {
        sort_r_swap(a, b, w);
        return 1;
    }
    return 0;
}

static inline void sort_r_swap_blocks(char *ptr, size_t na, size_t nb)
{
    if(na > 0 && nb > 0) {
        if(na > nb) { sort_r_swap(ptr, ptr+na, nb); }
        else { sort_r_swap(ptr, ptr+nb, na); }
    }
}

static inline void sort_r_simple(
    void *base,
    size_t nel,
    size_t w,
    int (*compar)(const void *_a,
        const void *_b,
        void *_arg),
        void *arg
    )
{
    char *b = (char *)base, *end = b + nel*w;
    if(nel < 10) {
        char *pi, *pj;
        for(pi = b+w; pi < end; pi += w) {
            for(pj = pi; pj > b && sort_r_cmpswap(pj-w,pj,w,compar,arg); pj -= w) {}
        }
    }
    else
    {
        int cmp;
        char *pl, *ple, *pr, *pre, *pivot;
        char *last = b+w*(nel-1), *tmp;

        char *l[3];
        l[0] = b + w;
        l[1] = b+w*(nel/2);
        l[2] = last - w;

        if(compar(l[0],l[1],arg) > 0) { SORT_R_SWAP(l[0], l[1], tmp); }
        if(compar(l[1],l[2],arg) > 0) {
            SORT_R_SWAP(l[1], l[2], tmp);
            if(compar(l[0],l[1],arg) > 0) { SORT_R_SWAP(l[0], l[1], tmp); }
        }

        if(l[1] != last) { sort_r_swap(l[1], last, w); }

        pivot = last;
        ple = pl = b;
        pre = pr = last;

        while(pl < pr) {
            for(; pl < pr; pl += w) {
                cmp = compar(pl, pivot, arg);
                if(cmp > 0) { break; }
                else if(cmp == 0) {
                    if(ple < pl) { sort_r_swap(ple, pl, w); }
                    ple += w;
                }
            }
            if(pl >= pr) { break; }
            for(; pl < pr; ) {
                pr -= w; /* Move right pointer onto an unprocessed item */
                cmp = compar(pr, pivot, arg);
                if(cmp == 0) {
                    pre -= w;
                    if(pr < pre) { sort_r_swap(pr, pre, w); }
                }
                else if(cmp < 0) {
                    if(pl < pr) { sort_r_swap(pl, pr, w); }
                    pl += w;
                    break;
                }
            }
        }

        pl = pr;
        sort_r_swap_blocks(b, ple-b, pl-ple);
        sort_r_swap_blocks(pr, pre-pr, end-pre);
        sort_r_simple(b, (pl-ple)/w, w, compar, arg);
        sort_r_simple(end-(pre-pr), (pre-pr)/w, w, compar, arg);
    }
}

void qsort_r(
        void *base,
        size_t nel,
        size_t w,
        int (*compar)(
            const void *_a,
            const void *_b,
            void *_arg),
            void *arg
        )
{
    char *b = (char *)base, *end = b + nel*w;
    if(nel < 10) {
        char *pi, *pj;
        for(pi = b+w; pi < end; pi += w) {
            for(pj = pi; pj > b && sort_r_cmpswap(pj-w,pj,w,compar,arg); pj -= w) {}
        }
    }
    else
    {
        int cmp;
        char *pl, *ple, *pr, *pre, *pivot;
        char *last = b+w*(nel-1), *tmp;

        char *l[3];
        l[0] = b + w;
        l[1] = b+w*(nel/2);
        l[2] = last - w;

        if(compar(l[0],l[1],arg) > 0) { SORT_R_SWAP(l[0], l[1], tmp); }
        if(compar(l[1],l[2],arg) > 0) {
            SORT_R_SWAP(l[1], l[2], tmp);
            if(compar(l[0],l[1],arg) > 0) { SORT_R_SWAP(l[0], l[1], tmp); }
        }

        if(l[1] != last) { sort_r_swap(l[1], last, w); }

        pivot = last;
        ple = pl = b;
        pre = pr = last;

        while(pl < pr) {
            for(; pl < pr; pl += w) {
                cmp = compar(pl, pivot, arg);
                if(cmp > 0) { break; }
                else if(cmp == 0) {
                    if(ple < pl) { sort_r_swap(ple, pl, w); }
                    ple += w;
                }
            }
            if(pl >= pr) { break; }
            for(; pl < pr; ) {
                pr -= w;
                cmp = compar(pr, pivot, arg);
                if(cmp == 0) {
                    pre -= w;
                    if(pr < pre) { sort_r_swap(pr, pre, w); }
                }
                else if(cmp < 0) {
                    if(pl < pr) { sort_r_swap(pl, pr, w); }
                    pl += w;
                    break;
                }
            }
        }

        pl = pr; /* pr may have gone below pl */
        sort_r_swap_blocks(b, ple-b, pl-ple);
        sort_r_swap_blocks(pr, pre-pr, end-pre);
        sort_r_simple(b, (pl-ple)/w, w, compar, arg);
        sort_r_simple(end-(pre-pr), (pre-pr)/w, w, compar, arg);
    }
}
