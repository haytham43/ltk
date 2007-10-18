/*
 * Copyright 2007 ETH Zurich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.llrp.ltk.types;

import org.apache.log4j.Logger;

import org.jdom.Document;

import org.llrp.ltk.exceptions.IllegalBitListException;
import org.llrp.ltk.exceptions.LLRPException;
import org.llrp.ltk.exceptions.WrongParameterException;


/**
 * representing a message in LLRP. * The binary encoding Is always: Reserved(3
 * Bits) | Version (3 Bits) | Message Type (10 Bits) | Message Length (32 Bits) |
 * Parameters
 *
 * call empty constructor to create new message. Use constructor taking
 * LLRPBitList or Byte[] to create message from binary encoded message. Use
 * constructor taking JDOM document to create message from XML encoding
 *
 */
public abstract class LLRPMessage {
    public static Logger logging = Logger.getLogger(LLRPMessage.class);
    public final int reservedLength = 3;
    public final int versionLength = 3;
    public final int typeNumberLength = 10;
    public final int minHeaderLength = 80;
    protected BitList reserved = new BitList(reservedLength);
    protected BitList version;
    protected UnsignedInteger messageID = new UnsignedInteger();
    protected UnsignedInteger messageLength = new UnsignedInteger();

    /**
     * encode this message to binary formate.
     *
     * @return Byte[] which can directly be sent over a stream
     */
    public final Byte[] encodeBinary() {
        LLRPBitList result = new LLRPBitList();
        result.append(reserved.encodeBinary());
        result.append(version.encodeBinary());
        // type number only last 10 bits of first two bytes. Bit 0-5 used for
        // reserved and version
        result.append(getTypeNum().encodeBinary()
                          .subList(reservedLength + versionLength,
                typeNumberLength));
        result.append(messageLength.encodeBinary());
        result.append(messageID.encodeBinary());
        // call the message specific encode function
        result.append(encodeBinarySpecific());
        // finalizeEncode sets the length correctly. It must be called after
        // encoding as length can only be set after message is encoded
        finalizeEncode(result);

        return result.toByteArray();
    }

    /**
     * encoding function - has to be implemented by each message.
     *
     * @return LLRPBitList
     */
    protected abstract LLRPBitList encodeBinarySpecific();

    /**
     * create message from Byte[]. Will also be called from Constructor taking a Byte[] Argument
     *
     * @param bits
     *            representing message
     *
     * @throws LLRPException
     *             if bitstring is not well formatted or has any other error
     * @throws IllegalBitListException
     */
    public final void decodeBinary(Byte[] byteArray) {
        LLRPBitList bits = new LLRPBitList(byteArray);

        // message must have at least 80 bits for header
        if (bits.length() < minHeaderLength) {
            logging.error("Bit String too short, must be at least 80, is " +
                bits.length());
            throw new IllegalBitListException("Bit String too short");
        }

        Short messageType = new SignedShort(bits.subList(reservedLength +
                    versionLength,
                    SignedShort.length() - (reservedLength + versionLength))).toShort();

        // this should never occur. Implies an error in Message Decoder,
        // since it is the one responsible for calling the appropriate
        // class constructors according to message type
        if (!messageType.equals(getTypeNum().toShort())) {
            logging.error("incorrect type. Message of Type " +
                getTypeNum().toShort() + " expected, but message indicates " +
                messageType);
            throw new LLRPException("incorrect type. Message of Type " +
                getTypeNum().toShort() + " expected, but message indicates " +
                messageType);
        }

        // skip first three bits as they are reserved
        int position = reservedLength;
        version = new BitList(versionLength);

        // version is a BitList of length versionLength
        for (int i = 0; i < versionLength; i++) {
            // read bits starting at position (position==reservedLength)
            if (bits.get(position + i)) {
                version.set(i);
            } else {
                version.clear(i);
            }
        }

        // skip message type - length starts at position 16 - it is a
        // signedShort
        position = SignedShort.length();
        messageLength = new UnsignedInteger(bits.subList(position,
                    UnsignedInteger.length()));
        position += UnsignedInteger.length();
        messageID = new UnsignedInteger(bits.subList(position,
                    UnsignedInteger.length()));
        position += UnsignedInteger.length();
        // call decodeSpecific which is implemented by the class subtyping this
        // one.
        // pass only data bits, not header
        decodeBinarySpecific(bits.subList(position, bits.length() - position));
    }

    // /**
    /**
     * Message ID to distinguish messages of same type.
     *
     * @return UnsignedInteger
     */
    public UnsignedInteger getMessageID() {
        return messageID;
    }

    /**
     * number of bytes of encoded message.
     *
     * @return UnsignedInteger
     */
    public UnsignedInteger getMessageLength() {
        return messageLength;
    }

    /**
     * type number uniquely identifies message.
     *
     * @return SignedShort
     */
    public abstract SignedShort getTypeNum();

    /**
     * version of llrp.
     *
     * @return BitList
     */
    public BitList getVersion() {
        return version;
    }

    /**
     * setMessageID.
     *
     * @param messageID
     */
    public void setMessageID(UnsignedInteger messageID) {
        this.messageID = messageID;
    }

    /**
     * create BitList easiest is to use variadic argument - for example
     * BitList(0,1,1) for value 3
     *
     * @param version
     *            as bit array
     *
     * @throws WrongParameterException
     */
    public void setVersion(BitList version) {
        if (version.length() != versionLength) {
            // LLRPMessage.logger.warn("wrong length of version - must be bit
            // array of length 3");
            throw new LLRPException("wrong length of version");
        }

        this.version = version;
    }

    /**
     * Message as Bitstring.
     *
     * @return String
     */
    public String toString() {
        return encodeBinary().toString();
    }

    /**
     * to be implemented by specific message.
     *
     * @param bits
     *            without header
     *
     * @throws LLRPException
     */
    protected abstract void decodeBinarySpecific(LLRPBitList bits);

    /**
     * finalizeEncode sets the length of the message.
     *
     * @param result
     */
    private void finalizeEncode(LLRPBitList result) {
        // get length of BitList
        int lengthBits = result.length();

        // calculte number of bytes
        // BitList must already be a multiple of 8
        int lengthBytes = lengthBits / 8;
        LLRPBitList binLength = new UnsignedInteger(lengthBytes).encodeBinary();

        for (int i = 0; i < binLength.length(); i++) {
            // replace bits starting from bit 16 up to bit 48
            if (binLength.get(i)) {
                result.set(versionLength + reservedLength + typeNumberLength +
                    i);
            } else {
                result.clear(versionLength + reservedLength + typeNumberLength +
                    i);
            }
        }
    }

    /**
     * create xml representation of this parameter.
     *
     * @return Dom Document
     */
    public abstract Document encodeXML();

    /**
     * create objects from xml.
     *
     * @param dom
     *            document
     */
    public abstract void decodeXML(Document xml);
}
