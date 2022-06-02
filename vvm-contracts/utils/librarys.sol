// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

/**
 * @dev A library for working with mutable byte buffers in Solidity.
 *
 * Byte buffers are mutable and expandable, and provide a variety of primitives
 * for writing to them. At any time you can fetch a bytes object containing the
 * current contents of the buffer. The bytes object should not be stored between
 * operations, as it may change due to resizing of the buffer.
 */
library Buffer {
    /**
     * @dev Represents a mutable buffer. Buffers have a current value (buf) and
     *      a capacity. The capacity may be longer than the current value, in
     *      which case it can be extended without the need to allocate more memory.
     */
    struct buffer {
        bytes buf;
        uint256 capacity;
    }

    /**
     * @dev Initializes a buffer with an initial capacity.
     * @param buf The buffer to initialize.
     * @param capacity The number of bytes of space to allocate the buffer.
     * @return The buffer, for chaining.
     */
    function init(buffer memory buf, uint256 capacity)
        internal
        pure
        returns (buffer memory)
    {
        if (capacity % 32 != 0) {
            capacity += 32 - (capacity % 32);
        }
        // Allocate space for the buffer data
        buf.capacity = capacity;
        assembly {
            let ptr := mload(0x40)
            mstore(buf, ptr)
            mstore(ptr, 0)
            mstore(0x40, add(32, add(ptr, capacity)))
        }
        return buf;
    }

    /**
     * @dev Initializes a new buffer from an existing bytes object.
     *      Changes to the buffer may mutate the original value.
     * @param b The bytes object to initialize the buffer with.
     * @return A new buffer.
     */
    function fromBytes(bytes memory b) internal pure returns (buffer memory) {
        buffer memory buf;
        buf.buf = b;
        buf.capacity = b.length;
        return buf;
    }

    function resize(buffer memory buf, uint256 capacity) private pure {
        bytes memory oldbuf = buf.buf;
        init(buf, capacity);
        append(buf, oldbuf);
    }

    function max(uint256 a, uint256 b) private pure returns (uint256) {
        if (a > b) {
            return a;
        }
        return b;
    }

    /**
     * @dev Sets buffer length to 0.
     * @param buf The buffer to truncate.
     * @return The original buffer, for chaining..
     */
    function truncate(buffer memory buf) internal pure returns (buffer memory) {
        assembly {
            let bufptr := mload(buf)
            mstore(bufptr, 0)
        }
        return buf;
    }

    /**
     * @dev Writes a byte string to a buffer. Resizes if doing so would exceed
     *      the capacity of the buffer.
     * @param buf The buffer to append to.
     * @param off The start offset to write to.
     * @param data The data to append.
     * @param len The number of bytes to copy.
     * @return The original buffer, for chaining.
     */
    function write(
        buffer memory buf,
        uint256 off,
        bytes memory data,
        uint256 len
    ) internal pure returns (buffer memory) {
        require(len <= data.length);

        if (off + len > buf.capacity) {
            resize(buf, max(buf.capacity, len + off) * 2);
        }

        uint256 dest;
        uint256 src;
        assembly {
            // Memory address of the buffer data
            let bufptr := mload(buf)
            // Length of existing buffer data
            let buflen := mload(bufptr)
            // Start address = buffer address + offset + sizeof(buffer length)
            dest := add(add(bufptr, 32), off)
            // Update buffer length if we're extending it
            if gt(add(len, off), buflen) {
                mstore(bufptr, add(len, off))
            }
            src := add(data, 32)
        }

        // Copy word-length chunks while possible
        for (; len >= 32; len -= 32) {
            assembly {
                mstore(dest, mload(src))
            }
            dest += 32;
            src += 32;
        }

        // Copy remaining bytes
        uint256 mask = 256**(32 - len) - 1;
        assembly {
            let srcpart := and(mload(src), not(mask))
            let destpart := and(mload(dest), mask)
            mstore(dest, or(destpart, srcpart))
        }

        return buf;
    }

    /**
     * @dev Appends a byte string to a buffer. Resizes if doing so would exceed
     *      the capacity of the buffer.
     * @param buf The buffer to append to.
     * @param data The data to append.
     * @param len The number of bytes to copy.
     * @return The original buffer, for chaining.
     */
    function append(
        buffer memory buf,
        bytes memory data,
        uint256 len
    ) internal pure returns (buffer memory) {
        return write(buf, buf.buf.length, data, len);
    }

    /**
     * @dev Appends a byte string to a buffer. Resizes if doing so would exceed
     *      the capacity of the buffer.
     * @param buf The buffer to append to.
     * @param data The data to append.
     * @return The original buffer, for chaining.
     */
    function append(buffer memory buf, bytes memory data)
        internal
        pure
        returns (buffer memory)
    {
        return write(buf, buf.buf.length, data, data.length);
    }

    /**
     * @dev Writes a byte to the buffer. Resizes if doing so would exceed the
     *      capacity of the buffer.
     * @param buf The buffer to append to.
     * @param off The offset to write the byte at.
     * @param data The data to append.
     * @return The original buffer, for chaining.
     */
    function writeUint8(
        buffer memory buf,
        uint256 off,
        uint8 data
    ) internal pure returns (buffer memory) {
        if (off >= buf.capacity) {
            resize(buf, buf.capacity * 2);
        }

        assembly {
            // Memory address of the buffer data
            let bufptr := mload(buf)
            // Length of existing buffer data
            let buflen := mload(bufptr)
            // Address = buffer address + sizeof(buffer length) + off
            let dest := add(add(bufptr, off), 32)
            mstore8(dest, data)
            // Update buffer length if we extended it
            if eq(off, buflen) {
                mstore(bufptr, add(buflen, 1))
            }
        }
        return buf;
    }

    /**
     * @dev Appends a byte to the buffer. Resizes if doing so would exceed the
     *      capacity of the buffer.
     * @param buf The buffer to append to.
     * @param data The data to append.
     * @return The original buffer, for chaining.
     */
    function appendUint8(buffer memory buf, uint8 data)
        internal
        pure
        returns (buffer memory)
    {
        return writeUint8(buf, buf.buf.length, data);
    }

    /**
     * @dev Writes up to 32 bytes to the buffer. Resizes if doing so would
     *      exceed the capacity of the buffer.
     * @param buf The buffer to append to.
     * @param off The offset to write at.
     * @param data The data to append.
     * @param len The number of bytes to write (left-aligned).
     * @return The original buffer, for chaining.
     */
    function write(
        buffer memory buf,
        uint256 off,
        bytes32 data,
        uint256 len
    ) private pure returns (buffer memory) {
        if (len + off > buf.capacity) {
            resize(buf, (len + off) * 2);
        }

        uint256 mask = 256**len - 1;
        // Right-align data
        data = data >> (8 * (32 - len));
        assembly {
            // Memory address of the buffer data
            let bufptr := mload(buf)
            // Address = buffer address + sizeof(buffer length) + off + len
            let dest := add(add(bufptr, off), len)
            mstore(dest, or(and(mload(dest), not(mask)), data))
            // Update buffer length if we extended it
            if gt(add(off, len), mload(bufptr)) {
                mstore(bufptr, add(off, len))
            }
        }
        return buf;
    }

    /**
     * @dev Writes a bytes20 to the buffer. Resizes if doing so would exceed the
     *      capacity of the buffer.
     * @param buf The buffer to append to.
     * @param off The offset to write at.
     * @param data The data to append.
     * @return The original buffer, for chaining.
     */
    function writeBytes20(
        buffer memory buf,
        uint256 off,
        bytes20 data
    ) internal pure returns (buffer memory) {
        return write(buf, off, bytes32(data), 20);
    }

    /**
     * @dev Appends a bytes20 to the buffer. Resizes if doing so would exceed
     *      the capacity of the buffer.
     * @param buf The buffer to append to.
     * @param data The data to append.
     * @return The original buffer, for chhaining.
     */
    function appendBytes20(buffer memory buf, bytes20 data)
        internal
        pure
        returns (buffer memory)
    {
        return write(buf, buf.buf.length, bytes32(data), 20);
    }

    /**
     * @dev Appends a bytes32 to the buffer. Resizes if doing so would exceed
     *      the capacity of the buffer.
     * @param buf The buffer to append to.
     * @param data The data to append.
     * @return The original buffer, for chaining.
     */
    function appendBytes32(buffer memory buf, bytes32 data)
        internal
        pure
        returns (buffer memory)
    {
        return write(buf, buf.buf.length, data, 32);
    }

    /**
     * @dev Writes an integer to the buffer. Resizes if doing so would exceed
     *      the capacity of the buffer.
     * @param buf The buffer to append to.
     * @param off The offset to write at.
     * @param data The data to append.
     * @param len The number of bytes to write (right-aligned).
     * @return The original buffer, for chaining.
     */
    function writeInt(
        buffer memory buf,
        uint256 off,
        uint256 data,
        uint256 len
    ) private pure returns (buffer memory) {
        if (len + off > buf.capacity) {
            resize(buf, (len + off) * 2);
        }

        uint256 mask = 256**len - 1;
        assembly {
            // Memory address of the buffer data
            let bufptr := mload(buf)
            // Address = buffer address + off + sizeof(buffer length) + len
            let dest := add(add(bufptr, off), len)
            mstore(dest, or(and(mload(dest), not(mask)), data))
            // Update buffer length if we extended it
            if gt(add(off, len), mload(bufptr)) {
                mstore(bufptr, add(off, len))
            }
        }
        return buf;
    }

    /**
     * @dev Appends a byte to the end of the buffer. Resizes if doing so would
     * exceed the capacity of the buffer.
     * @param buf The buffer to append to.
     * @param data The data to append.
     * @return The original buffer.
     */
    function appendInt(
        buffer memory buf,
        uint256 data,
        uint256 len
    ) internal pure returns (buffer memory) {
        return writeInt(buf, buf.buf.length, data, len);
    }
}

library CBOR {
    using Buffer for Buffer.buffer;

    uint8 private constant MAJOR_TYPE_INT = 0;
    uint8 private constant MAJOR_TYPE_NEGATIVE_INT = 1;
    uint8 private constant MAJOR_TYPE_BYTES = 2;
    uint8 private constant MAJOR_TYPE_STRING = 3;
    uint8 private constant MAJOR_TYPE_ARRAY = 4;
    uint8 private constant MAJOR_TYPE_MAP = 5;
    uint8 private constant MAJOR_TYPE_CONTENT_FREE = 7;

    function encodeType(
        Buffer.buffer memory buf,
        uint8 major,
        uint256 value
    ) private pure {
        if (value <= 23) {
            buf.appendUint8(uint8((major << 5) | value));
        } else if (value <= 0xFF) {
            buf.appendUint8(uint8((major << 5) | 24));
            buf.appendInt(value, 1);
        } else if (value <= 0xFFFF) {
            buf.appendUint8(uint8((major << 5) | 25));
            buf.appendInt(value, 2);
        } else if (value <= 0xFFFFFFFF) {
            buf.appendUint8(uint8((major << 5) | 26));
            buf.appendInt(value, 4);
        } else if (value <= 0xFFFFFFFFFFFFFFFF) {
            buf.appendUint8(uint8((major << 5) | 27));
            buf.appendInt(value, 8);
        }
    }

    function encodeIndefiniteLengthType(Buffer.buffer memory buf, uint8 major)
        private
        pure
    {
        buf.appendUint8(uint8((major << 5) | 31));
    }

    function encodeUInt(Buffer.buffer memory buf, uint256 value) internal pure {
        encodeType(buf, MAJOR_TYPE_INT, value);
    }

    function encodeInt(Buffer.buffer memory buf, int256 value) internal pure {
        if (value >= 0) {
            encodeType(buf, MAJOR_TYPE_INT, uint256(value));
        } else {
            encodeType(buf, MAJOR_TYPE_NEGATIVE_INT, uint256(-1 - value));
        }
    }

    function encodeBytes(Buffer.buffer memory buf, bytes memory value) internal pure {
        encodeType(buf, MAJOR_TYPE_BYTES, value.length);
        buf.append(value);
    }

    function encodeString(Buffer.buffer memory buf, string memory value)
        internal
        pure
    {
        encodeType(buf, MAJOR_TYPE_STRING, bytes(value).length);
        buf.append(bytes(value));
    }

    function startArray(Buffer.buffer memory buf) internal pure {
        encodeIndefiniteLengthType(buf, MAJOR_TYPE_ARRAY);
    }

    function startMap(Buffer.buffer memory buf) internal pure {
        encodeIndefiniteLengthType(buf, MAJOR_TYPE_MAP);
    }

    function endSequence(Buffer.buffer memory buf) internal pure {
        encodeIndefiniteLengthType(buf, MAJOR_TYPE_CONTENT_FREE);
    }
}

/**
 * @title Library for common Victorlink functions
 * @dev Uses imported CBOR library for encoding to buffer
 */
library Victorlink {
    uint256 internal constant defaultBufferSize = 256; // solhint-disable-line const-name-snakecase

    using Buffer for Buffer.buffer;
    using CBOR for Buffer.buffer;

    struct Request {
        bytes32 id;
        address callbackAddress;
        bytes4 callbackFunctionId;
        uint256 nonce;
        Buffer.buffer buf;
    }

    /**
     * @notice Initializes a Victorlink request
     * @dev Sets the ID, callback address, and callback function signature on the request
     * @param self The uninitialized request
     * @param _id The Job Specification ID
     * @param _callbackAddress The callback address
     * @param _callbackFunction The callback function signature
     * @return The initialized request
     */
    function initialize(
        Request memory self,
        bytes32 _id,
        address _callbackAddress,
        bytes4 _callbackFunction
    ) internal pure returns (Victorlink.Request memory) {
        Buffer.init(self.buf, defaultBufferSize);
        self.id = _id;
        self.callbackAddress = _callbackAddress;
        self.callbackFunctionId = _callbackFunction;
        return self;
    }

    /**
     * @notice Sets the data for the buffer without encoding CBOR on-chain
     * @dev CBOR can be closed with curly-brackets {} or they can be left off
     * @param self The initialized request
     * @param _data The CBOR data
     */
    function setBuffer(Request memory self, bytes memory _data) internal pure {
        Buffer.init(self.buf, _data.length);
        Buffer.append(self.buf, _data);
    }

    /**
     * @notice Adds a string value to the request with a given key name
     * @param self The initialized request
     * @param _key The name of the key
     * @param _value The string value to add
     */
    function add(
        Request memory self,
        string memory _key,
        string memory _value
    ) internal pure {
        self.buf.encodeString(_key);
        self.buf.encodeString(_value);
    }

    /**
     * @notice Adds a bytes value to the request with a given key name
     * @param self The initialized request
     * @param _key The name of the key
     * @param _value The bytes value to add
     */
    function addBytes(
        Request memory self,
        string memory _key,
        bytes memory _value
    ) internal pure {
        self.buf.encodeString(_key);
        self.buf.encodeBytes(_value);
    }

    /**
     * @notice Adds a int256 value to the request with a given key name
     * @param self The initialized request
     * @param _key The name of the key
     * @param _value The int256 value to add
     */
    function addInt(
        Request memory self,
        string memory _key,
        int256 _value
    ) internal pure {
        self.buf.encodeString(_key);
        self.buf.encodeInt(_value);
    }

    /**
     * @notice Adds a uint256 value to the request with a given key name
     * @param self The initialized request
     * @param _key The name of the key
     * @param _value The uint256 value to add
     */
    function addUint(
        Request memory self,
        string memory _key,
        uint256 _value
    ) internal pure {
        self.buf.encodeString(_key);
        self.buf.encodeUInt(_value);
    }

    /**
     * @notice Adds an array of strings to the request with a given key name
     * @param self The initialized request
     * @param _key The name of the key
     * @param _values The array of string values to add
     */
    function addStringArray(
        Request memory self,
        string memory _key,
        string[] memory _values
    ) internal pure {
        self.buf.encodeString(_key);
        self.buf.startArray();
        for (uint256 i = 0; i < _values.length; i++) {
            self.buf.encodeString(_values[i]);
        }
        self.buf.endSequence();
    }
}