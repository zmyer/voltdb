/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef MEMORY_CHUNK_ALLOCATOR_HPP_
#define MEMORY_CHUNK_ALLOCATOR_HPP_

#include <sys/mman.h>
#include <iostream>
#include "common/FatalException.hpp"

namespace voltdb {

/*
 * Find next higher power of two
 * From http://en.wikipedia.org/wiki/Power_of_two
 */
template <class T>
inline T nexthigher(T k) {
    if (k == 0)
            return 1;
    k--;
    for (int i=1; i<sizeof(T)*CHAR_BIT; i<<=1)
            k = k | k >> i;
    return k+1;
}

static size_t computeMemoryChunkSize(size_t allocationSize) {
#ifdef USE_HUGEPAGES
    const size_t rounded_to_2MB = (allocationSize >> 21) << 21;
    if (allocationSize == 0) {
        throwFatalException("BSDBG: Unexpected attempt to allocate block of size 0");
    } else if (rounded_to_2MB == allocationSize) {
        return allocationSize;
    } else {
        return rounded_to_2MB + 1 << 21;
    }
#else
#ifdef USE_MMAP
    return nexthigher(allocationSize);
#else
    return allocationSize;
#endif
#endif
}

static char* allocateMemoryChunk(size_t allocationSize) {
#ifdef USE_HUGEPAGES
    if (allocationSize & 0x1FFFFF){
        std::cout << strerror( errno ) << std::endl;
        std::cout << "Attempted to allocate " << allocationSize << " bytes of memory" << std::endl;
        throwFatalException("BSDBG: Could not mmap - memory is not 2MB aligned");
    }
    return static_cast<char*>(::mmap( 0, allocationSize, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANON, -1, 0 ));
#else
#ifdef USE_MMAP
    return static_cast<char*>(::mmap( 0, allocationSize, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANON | MAP_HUGETLB | MAP_HUGE_2MB, -1, 0 ));
#else
    return new char[allocationSize];
#endif
#endif
}

static void deallocateMemoryChunk(char* chunk, uint64_t size) {
#if defined(USE_HUGEPAGES) || defined(USE_MMAP)
    if (::munmap(chunk, size) != 0) {
        std::cout << strerror( errno ) << std::endl;
        throwFatalException("BSDBG: Failed munmap of normal memory");
    }
#else
    delete [] chunk;
#endif
}

}

#endif /* #ifndef MEMORY_CHUNK_ALLOCATOR_HPP_ */
