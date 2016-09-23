/* This file is part of VoltDB.
 * Copyright (C) 2008-2016 VoltDB Inc.
 *
 * This file contains original code and/or modifications of original code.
 * Any modifications made by VoltDB Inc. are licensed under the following
 * terms and conditions:
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
/* Copyright (C) 2008 by H-Store Project
 * Brown University
 * Massachusetts Institute of Technology
 * Yale University
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

#ifndef HSTORESERIALIZEIO_H
#define HSTORESERIALIZEIO_H

#include "common/bytearray.h"
#include "common/debuglog.h"
#include "common/types.h"

#include <fstream>

namespace voltdb {


#ifdef __DARWIN_OSSwapInt64 // for darwin/macosx

#undef htonll
#undef ntohll
#define htonll(x) __DARWIN_OSSwapInt64(x)
#define ntohll(x) __DARWIN_OSSwapInt64(x)

#else // unix in general

#undef htons
#undef ntohs
#define htons(x) static_cast<uint16_t>((((x) >> 8) & 0xff) | (((x) & 0xff) << 8))
#define ntohs(x) static_cast<uint16_t>((((x) >> 8) & 0xff) | (((x) & 0xff) << 8))

#ifdef __bswap_64 // recent linux

#undef htonll
#undef ntohll
#define htonll(x) static_cast<uint64_t>(__bswap_constant_64(x))
#define ntohll(x) static_cast<uint64_t>(__bswap_constant_64(x))

#else // unix in general again

#undef htonll
#undef ntohll
#define htonll(x) (((int64_t)(ntohl((int32_t)((x << 32) >> 32))) << 32) | (uint32_t)ntohl(((int32_t)(x >> 32))))
#define ntohll(x) (((int64_t)(ntohl((int32_t)((x << 32) >> 32))) << 32) | (uint32_t)ntohl(((int32_t)(x >> 32))))

#endif // __bswap_64

#endif // unix or mac



/** Abstract class for reading from memory buffers. */
template <Endianess E> class SerializeInput {
protected:
    /** Does no initialization. Subclasses must call initialize. */
    SerializeInput() : current_(NULL), end_(NULL) {}

    void initialize(const void* data, size_t length) {
        current_ = reinterpret_cast<const char*>(data);
        end_ = current_ + length;
    }

public:
    virtual ~SerializeInput() {};

    // functions for deserialization
    inline char readChar() {
        return readPrimitive<char>();
    }

    inline int8_t readByte() {
        return readPrimitive<int8_t>();
    }

    inline int16_t readShort() {
        int16_t value = readPrimitive<int16_t>();
        if (E == BYTE_ORDER_BIG_ENDIAN) {
            return ntohs(value);
        } else {
            return value;
        }
    }

    inline int32_t readInt() {
        int32_t value = readPrimitive<int32_t>();
        if (E == BYTE_ORDER_BIG_ENDIAN) {
            return ntohl(value);
        } else {
            return value;
        }
    }

    inline bool readBool() {
        return readByte();
    }

    inline char readEnumInSingleByte() {
        return readByte();
    }

    inline int64_t readLong() {
        int64_t value = readPrimitive<int64_t>();
        if (E == BYTE_ORDER_BIG_ENDIAN) {
            return ntohll(value);
        } else {
            return value;
        }
    }

    inline float readFloat() {
        int32_t value = readPrimitive<int32_t>();
        if (E == BYTE_ORDER_BIG_ENDIAN) {
            value = ntohl(value);
        }
        float retval;
        memcpy(&retval, &value, sizeof(retval));
        return retval;
    }

    inline double readDouble() {
        int64_t value = readPrimitive<int64_t>();
        if (E == BYTE_ORDER_BIG_ENDIAN) {
            value = ntohll(value);
        }
        double retval;
        memcpy(&retval, &value, sizeof(retval));
        return retval;
    }

    /** Returns a pointer to the internal data buffer, advancing the read position by length. */
    const char* getRawPointer(size_t length) {
        const char* result = current_;
        current_ += length;
        // TODO: Make this a non-optional check?
        assert(current_ <= end_);
        return result;
    }

    const char* getRawPointer() {
        return current_;
    }

    /** Copy a string from the buffer. */
    inline std::string readTextString() {
        int32_t stringLength = readInt();
        assert(stringLength >= 0);
        return std::string(reinterpret_cast<const char*>(getRawPointer(stringLength)),
                stringLength);
    };

    /** Copy a ByteArray from the buffer. */
    inline ByteArray readBinaryString() {
        int32_t stringLength = readInt();
        assert(stringLength >= 0);
        return ByteArray(reinterpret_cast<const char*>(getRawPointer(stringLength)),
                stringLength);
    };

    /** Copy the next length bytes from the buffer to destination. */
    inline void readBytes(void* destination, size_t length) {
        ::memcpy(destination, getRawPointer(length), length);
    };

    /** Write the buffer as hex bytes for debugging */
    std::string fullBufferStringRep();

    /** Move the read position back by bytes. Warning: this method is
    currently unverified and could result in reading before the
    beginning of the buffer. */
    // TODO(evanj): Change the implementation to validate this?
    void unread(size_t bytes) {
        current_ -= bytes;
    }

    bool hasRemaining() {
        return current_ < end_;
    }

private:
    template <typename T>
    T readPrimitive() {
        T value;
        ::memcpy(&value, current_, sizeof(value));
        current_ += sizeof(value);
        return value;
    }

    // Current read position.
    const char* current_;
    // End of the buffer. Valid byte range: current_ <= validPointer < end_.
    const char* end_;

    // No implicit copies
    SerializeInput(const SerializeInput&);
    SerializeInput& operator=(const SerializeInput&);
};

/** Implementation of SerializeInput that references an existing buffer. */
template <Endianess E> class ReferenceSerializeInput : public SerializeInput<E> {
public:
    ReferenceSerializeInput(const void* data, size_t length) {
        this->initialize(data, length);
    }

    // Destructor does nothing: nothing to clean up!
    virtual ~ReferenceSerializeInput() {}
};

/** Implementation of SerializeInput that makes a copy of the buffer. */
template <Endianess E> class CopySerializeInput : public SerializeInput<E> {
public:
    CopySerializeInput(const void* data, size_t length)
      : bytes_(reinterpret_cast<const char*>(data), static_cast<int>(length))
    {
        SerializeInput<E>::initialize(bytes_.data(), static_cast<int>(length));
    }

    // Destructor frees the ByteArray.
    virtual ~CopySerializeInput() {}

private:
    ByteArray bytes_;
};

#ifndef SERIALIZE_IO_DECLARATIONS
#define SERIALIZE_IO_DECLARATIONS
typedef SerializeInput<BYTE_ORDER_BIG_ENDIAN> SerializeInputBE;
typedef SerializeInput<BYTE_ORDER_LITTLE_ENDIAN> SerializeInputLE;

typedef ReferenceSerializeInput<BYTE_ORDER_BIG_ENDIAN> ReferenceSerializeInputBE;
typedef ReferenceSerializeInput<BYTE_ORDER_LITTLE_ENDIAN> ReferenceSerializeInputLE;

typedef CopySerializeInput<BYTE_ORDER_BIG_ENDIAN> CopySerializeInputBE;
typedef CopySerializeInput<BYTE_ORDER_LITTLE_ENDIAN> CopySerializeInputLE;
#endif


/** Abstract class for writing to memory buffers. Subclasses may optionally support resizing. */
class ByteBufferOutput {
protected:
    ByteBufferOutput() : buffer_(NULL), position_(0), capacity_(0) {}

    /** Set the buffer to buffer with capacity and position. */
    void initialize(void* buffer, size_t capacity, size_t position = 0) {
        position_ = position;
        buffer_ = reinterpret_cast<char*>(buffer);
        assert(position_ <= capacity);
        capacity_ = capacity;
    }
    void setPosition(size_t position) {
        this->position_ = position;
    }

public:
    /** Returns a pointer to the beginning of the buffer, for reading the serialized data. */
    const char* data() const { return buffer_; }

    /** Returns the number of bytes written in to the buffer. */
    size_t size() const { return position_; }

    inline void writeBytes(const void *value, size_t length) {
        assureExpand(length);
        memcpy(buffer_ + position_, value, length);
        position_ += length;
    }

    inline void writeZeros(size_t length) {
        assureExpand(length);
        memset(buffer_ + position_, 0, length);
        position_ += length;
    }

    /** Reserves length bytes of space for writing. Returns the offset to the bytes. */
    size_t reserveBytes(size_t length) {
        assureExpand(length);
        size_t offset = position_;
        position_ += length;
        return offset;
    }

    /** Copies length bytes from value to this buffer, starting at
    offset. Offset should have been obtained from reserveBytes. This
    does not affect the current write position.  * @return offset +
    length */
    inline size_t writeBytesAt(size_t offset, const void *value, size_t length) {
        assert(offset + length <= position_);
        memcpy(buffer_ + offset, value, length);
        return offset + length;
    }

    std::size_t position() const {
        return position_;
    }

    size_t remaining() const {
        return capacity_ - position_;
    }

protected:
    /** Called when trying to write past the end of the buffer.
     * Subclasses can optionally resize the buffer by calling initialize.
     * If this function returns and size() < minimum_desired,
     * the program will crash.
     * @param minimum_desired the minimum length
     * the resized buffer needs to have. **/
    virtual void expand(size_t minimum_desired) = 0;

private:
    inline void assureExpand(size_t next_write) {
        size_t minimum_desired = position_ + next_write;
        if (minimum_desired > capacity_) {
            expand(minimum_desired);
        }
        assert(capacity_ >= minimum_desired);
    }

    // No implicit copies
    ByteBufferOutput(const ByteBufferOutput&);
    ByteBufferOutput& operator=(const ByteBufferOutput&);

    // Beginning of the buffer.
    char* buffer_;
    // Current write position in the buffer.
    size_t position_;
    // Total bytes this buffer can contain.
    size_t capacity_;
};

/** Implementation of SerializeOutput that references an existing buffer. */
class ReferenceByteSerializer : public ByteBufferOutput {
public:
    ReferenceByteSerializer() : ByteBufferOutput() { }

    /** Make the protected method public for this class */
    void initialize(void* buffer, size_t capacity, size_t position) {
        ByteBufferOutput::initialize(buffer, capacity, position);
    }

protected:
    /** Reference output can't resize the buffer: Frowny-Face. */
    virtual void expand(size_t minimum_desired);
};

/*
 * A serialize output class that falls back to allocating a 50 meg buffer
 * if the regular allocation runs out of space. The topend is notified when this occurs.
 */
class FallbackByteSerializer : public ReferenceByteSerializer {
public:
    FallbackByteSerializer()
      : fallbackBuffer_(NULL)
    { }

    /** Set the buffer to buffer with capacity and sets the position. */
    void initialize(void* buffer, size_t capacity, size_t position) {
        if (fallbackBuffer_ != NULL) {
            char *temp = fallbackBuffer_;
            fallbackBuffer_ = NULL;
            delete[] temp;
        }
        ReferenceByteSerializer::initialize(buffer, capacity, position);
    }

    // Destructor frees the fallback buffer if it is allocated
    ~FallbackByteSerializer() {
        delete[] fallbackBuffer_;
    }

    /** Expand once to a fallback size, and if that doesn't work abort */
    void expand(size_t minimum_desired);

private:
    char *fallbackBuffer_;
};

/** Implementation of SerializeOutput that makes a copy of the buffer. */
class ExpandableByteBufferOutput : public ByteBufferOutput {
public:
    // Start with something sizeable so we avoid a ton of initial
    // allocations.
    static const int INITIAL_SIZE = 8388608;

    ExpandableByteBufferOutput() : bytes_(INITIAL_SIZE) {
        initialize(bytes_.data(), INITIAL_SIZE);
    }

    // Destructor frees the ByteArray.
    virtual ~ExpandableByteBufferOutput() {}

    void reset() {
        setPosition(0);
    }

    size_t remaining() const {
        return bytes_.length() - static_cast<int>(position());
    }

protected:
    /** Resize this buffer to contain twice the amount desired. */
    virtual void expand(size_t minimum_desired) {
        size_t next_capacity = (bytes_.length() + minimum_desired) * 2;
        assert(next_capacity < static_cast<size_t>(std::numeric_limits<int>::max()));
        bytes_.copyAndExpand(static_cast<int>(next_capacity));
        initialize(bytes_.data(), next_capacity);
    }

private:
    ByteArray bytes_;
};


/** Class for writing to disk. */
class ByteFileOutput {
public:
    ByteFileOutput() { }

    ~ByteFileOutput() {};

    void initialize(std::string outputFileName) {
        m_ofile.open(outputFileName.c_str(),
                     std::ofstream::binary | std::ofstream::trunc);
    }

    void close() {
        m_ofile.flush();
        m_ofile.close();
    }

    /** Returns the number of bytes written. */
    size_t size() { return m_ofile.tellp(); }

    std::size_t position() { return m_ofile.tellp(); }

    inline void writeBytes(const void *value, size_t length) {
        m_ofile.write((const char*)value,length);
    }

    /** Copies length bytes from value to this buffer, starting at
     *  offset. Offset should have been obtained from reserveBytes. This
     *  does not affect the current write position. 
     * @return offset + length
     */
    inline size_t writeBytesAt(size_t offset, const void *value, size_t length) {
        size_t position = m_ofile.tellp();
        assert(offset + length <= position);
        m_ofile.seekp(offset);
        m_ofile.write((const char*)value, length);
        m_ofile.seekp(position);
        return offset + length;
    }

    /** Reserves length bytes of space for writing. Returns the offset to the bytes. */
    size_t reserveBytes(size_t length) {
        size_t offset = m_ofile.tellp();
        m_ofile.seekp(offset + length);
        return offset;
    }

private:
    // No implicit copies
    ByteFileOutput(const ByteFileOutput&);
    ByteFileOutput& operator=(const ByteFileOutput&);

    std::ofstream m_ofile;
};

/** Template class for serializing output. */
template <class BS> class SerializeOutput {
public:
    SerializeOutput(BS& serializer)
      : m_byteSerializer(serializer)
    { };

    /** Returns the number of bytes written. */
    size_t size() { return m_byteSerializer.size(); }

    std::size_t position() {
        return m_byteSerializer.position();
    }

    /** Reserves length bytes of space for writing. Returns the offset to the bytes. */
    size_t reserveBytes(size_t length) {
        return m_byteSerializer.reserveBytes(length);
    }

    // functions for serialization
    inline void writeChar(char value) {
        writePrimitive(value);
    }

    inline void writeByte(int8_t value) {
        writePrimitive(value);
    }

    inline void writeShort(int16_t value) {
        writePrimitive(static_cast<uint16_t>(htons(value)));
    }

    inline void writeInt(int32_t value) {
        writePrimitive(htonl(value));
    }

    inline void writeBool(bool value) {
        writeByte(value ? int8_t(1) : int8_t(0));
    };

    inline void writeLong(int64_t value) {
        writePrimitive(htonll(value));
    }

    inline void writeFloat(float value) {
        int32_t data;
        memcpy(&data, &value, sizeof(data));
        writePrimitive(htonl(data));
    }

    inline void writeDouble(double value) {
        int64_t data;
        memcpy(&data, &value, sizeof(data));
        writePrimitive(htonll(data));
    }

    inline void writeEnumInSingleByte(int value) {
        assert(std::numeric_limits<int8_t>::min() <= value &&
                value <= std::numeric_limits<int8_t>::max());
        writeByte(static_cast<int8_t>(value));
    }

    // this explicitly accepts char* and length (or ByteArray)
    // as std::string's implicit construction is unsafe!
    void writeBinaryString(const void* value, size_t length) {
        int32_t stringLength = static_cast<int32_t>(length);
        writePrimitive(htonl(stringLength));
        writeBytes(value, length);
    }

    inline void writeBinaryString(const ByteArray &value) {
        writeBinaryString(value.data(), value.length());
    }

    inline void writeTextString(const std::string &value) {
        writeBinaryString(value.data(), value.size());
    }

    inline void writeBytes(const void *value, size_t length) {
        m_byteSerializer.writeBytes(value,length);
    }

    inline size_t writeIntAt(size_t position, int32_t value) {
        return writePrimitiveAt(position, htonl(value));
    }

    inline size_t writeBoolAt(size_t position, bool value) {
        return writePrimitiveAt(position, value ? int8_t(1) : int8_t(0));
    }

    inline void writeZeros(size_t length) {
        m_byteSerializer.writeZeros(length);
    }

    static bool isLittleEndian() {
        static const uint16_t s = 0x0001;
        uint8_t byte;
        memcpy(&byte, &s, 1);
        return byte != 0;
    }

    inline size_t writeBytesAt(size_t offset, const void *value, size_t length) {
        return m_byteSerializer.writeBytesAt(offset, value, length);
    }

private:
    template <typename T>
    void writePrimitive(T value) {
        m_byteSerializer.writeBytes(&value, sizeof(value));
    }

    template <typename T>
    size_t writePrimitiveAt(size_t position, T value) {
        return writeBytesAt(position, &value, sizeof(value));
    }

    BS& m_byteSerializer;
};

template <class BS> class SelfContainedSerializeOutput : public SerializeOutput<BS> {
public:
    SelfContainedSerializeOutput()
      : SerializeOutput<BS>(m_byteSerializer)
      , m_byteSerializer()
    { }

protected:
     BS m_byteSerializer;
};

class ReferenceSerializeOutput : public SelfContainedSerializeOutput<ReferenceByteSerializer> {
public:
    ReferenceSerializeOutput()
      : SelfContainedSerializeOutput<ReferenceByteSerializer>()
    { }

    ReferenceSerializeOutput(void* buffer, size_t capacity)
      : SelfContainedSerializeOutput<ReferenceByteSerializer>()
    {
        m_byteSerializer.initialize(buffer, capacity, 0);
    }

    void initializeWithPosition(void* buffer, size_t capacity, size_t position) {
        m_byteSerializer.initialize(buffer, capacity, position);
    }

    size_t remaining() const { return m_byteSerializer.remaining(); }
    const char* data() const { return m_byteSerializer.data(); }
};

 
class FallbackSerializeOutput : public SelfContainedSerializeOutput<FallbackByteSerializer> {
public:
    FallbackSerializeOutput()
      : SelfContainedSerializeOutput<FallbackByteSerializer>()
    { }

    void initializeWithPosition(void* buffer, size_t capacity, size_t position) {
        m_byteSerializer.initialize(buffer, capacity, position);
    }

    size_t size() const {
        return m_byteSerializer.size();
    }
    const char* data() const { return m_byteSerializer.data(); }
};

class FileSerializeOutput : public SelfContainedSerializeOutput<ByteFileOutput> {
public:
    FileSerializeOutput(std::string outFileName)
      : SelfContainedSerializeOutput<ByteFileOutput>()
    {
        m_byteSerializer.initialize(outFileName);
    }

    const char* data() const { return NULL; }
};

class TestableSerializeOutput : public SelfContainedSerializeOutput<ExpandableByteBufferOutput> {
public:
    TestableSerializeOutput()
      : SelfContainedSerializeOutput<ExpandableByteBufferOutput>()
    { }

     //    TestableSerializeOutput(void* data, size_t length)
     // : voltdb::SerializeOutput<voltdb::ExpandableByteBufferOutput>(&m_byteSerializer)
     //{ }

     //void initializeWithPosition(void* buffer, size_t capacity, size_t position) {
     //    m_byteSerializer.initializeWithPosition(buffer, capacity, position);
     //}

     //size_t remaining() const { return m_byteSerializer.remaining(); }
    const char* data() const { return m_byteSerializer.data(); }
};

} // namespace voltdb
#endif
