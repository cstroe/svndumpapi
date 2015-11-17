/*
    Taken from Apache Lucene.
 */
package com.github.cstroe.svndumpgui.internal.utility;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.github.cstroe.svndumpgui.generated.CharStream;

import java.io.*;
import java.nio.charset.StandardCharsets;

/** An efficient implementation of JavaCC's CharStream interface.  <p>Note that
 * this does not do line-number counting, but instead keeps track of the
 * character position of the token in the input, as required by Lucene's
 * org.apache.lucene.analysis.Token API.
 * */
public final class SvnDumpCharStream implements CharStream {
    public static final int INITAL_BUFFER_LENGTH = 2048;
    byte[] buffer = null;

    int bufferLength = 0;          // end of valid chars
    int bufferPosition = 0;        // next char to read

    int tokenStart = 0;          // offset in buffer
    int bufferStart = 0;          // position in file of buffer

    long streamPosition = 0;

    InputStream inputStream;            // source of bytes

    /** Constructs from a Reader. */
    public SvnDumpCharStream(InputStream stream) {
        inputStream = stream;
    }

    @Override
    public final char readChar() throws IOException {
        if (bufferPosition >= bufferLength)
            refill();

        byte currentByte = buffer[bufferPosition];
        int expectedUtf8Length = getUTF8ExpectedLength(currentByte);
        if(expectedUtf8Length == 0) {
            throw new UnsupportedEncodingException("Svn log message is not UTF8 or ASCII encoded.");
        }

        byte[] rawBytes = new byte[expectedUtf8Length];
        System.arraycopy(buffer, bufferPosition, rawBytes, 0, expectedUtf8Length);
        char c = new String(rawBytes, StandardCharsets.UTF_8).charAt(0);

        streamPosition += expectedUtf8Length;
        bufferPosition += expectedUtf8Length;
        return c;
    }

    public byte[] readBytes(int length) throws IOException {
        byte[] localBuffer = new byte[length];

        // the number of chars already in the buffer
        int bufferedCharsLength = bufferLength - bufferPosition;

        if(bufferedCharsLength >= length) {
            System.arraycopy(buffer, bufferPosition, localBuffer, 0, length);
            bufferPosition += length;
            tokenStart = bufferPosition;
        } else {
            System.arraycopy(buffer, bufferPosition, localBuffer, 0, bufferedCharsLength);
            int totalBytesRead = bufferedCharsLength;

            // clear buffer
            tokenStart = 0;
            bufferPosition = 0;
            bufferLength = 0;

            while(totalBytesRead < length) {
                int bytesRead = inputStream.read(localBuffer, totalBytesRead, length - totalBytesRead);
                if (bytesRead == -1) {
                    throw new IOException("read past eof");
                }
                totalBytesRead += bytesRead;
            }
        }

        streamPosition += length;
        return localBuffer;
    }

    // Adapted from: http://stackoverflow.com/a/28892327
    private static int getUTF8ExpectedLength(byte firstByte) {
        int expectedLength = 0;
        if ((firstByte & 0b10000000) == 0b00000000) {
            expectedLength = 1;
        } else if ((firstByte & 0b11100000) == 0b11000000) {
            expectedLength = 2;
        } else if ((firstByte & 0b11110000) == 0b11100000) {
            expectedLength = 3;
        } else if ((firstByte & 0b11111000) == 0b11110000) {
            expectedLength = 4;
        } else if ((firstByte & 0b11111100) == 0b11111000) {
            expectedLength = 5;
        } else if ((firstByte & 0b11111110) == 0b11111100) {
            expectedLength = 6;
        }
        return expectedLength;
    }

    private void refill() throws IOException {
        int newPosition = bufferLength - tokenStart;

        if (tokenStart == 0) {        // token won't fit in buffer
            if (buffer == null) {        // first time: alloc buffer
                buffer = new byte[INITAL_BUFFER_LENGTH];
            } else if (bufferLength == buffer.length) { // grow buffer
                byte[] newBuffer = new byte[Math.max(buffer.length*2, Integer.MAX_VALUE - 5)];
                System.arraycopy(buffer, 0, newBuffer, 0, bufferLength);
                buffer = newBuffer;
            }
        } else {            // shift token to front
            System.arraycopy(buffer, tokenStart, buffer, 0, newPosition);
        }

        bufferLength = newPosition;        // update state
        bufferPosition = newPosition;
        bufferStart += tokenStart;
        tokenStart = 0;

        int charsRead =          // fill space in buffer
                inputStream.read(buffer, newPosition, buffer.length-newPosition);
        if (charsRead == -1)
            throw new IOException("read past eof");
        else
            bufferLength += charsRead;
    }

    @Override
    public final char BeginToken() throws IOException {
        tokenStart = bufferPosition;
        return readChar();
    }

    @Override
    public final void backup(int amount) {
        streamPosition -= amount;
        bufferPosition -= amount;
        assert bufferPosition > 0;
        assert streamPosition > 0;
    }

    @Override
    public final String GetImage() {
        return new String(buffer, tokenStart, bufferPosition - tokenStart);
    }

    @Override
    public final char[] GetSuffix(int len) {
        byte[] value = new byte[len];
        System.arraycopy(buffer, bufferPosition - len, value, 0, len);
        return new String(value).toCharArray();
    }

    @Override
    public final void Done() {
        try {
            inputStream.close();
        } catch (IOException e) {
            // nothing
        }
    }

    @Override
    public final int getColumn() {
        return (int)streamPosition;
    }
    @Override
    public final int getLine() {
        return (int)streamPosition;
    }
    @Override
    public final int getEndColumn() {
        return (int)streamPosition;
    }
    @Override
    public final int getEndLine() {
        return (int)streamPosition;
    }
    @Override
    public final int getBeginColumn() {
        return (int)streamPosition;
    }
    @Override
    public final int getBeginLine() {
        return (int)streamPosition;
    }

    @Override
    public void setTabSize(int i) {

    }

    @Override
    public int getTabSize() {
        return 0;
    }

    @Override
    public boolean getTrackLineColumn() {
        return false;
    }

    @Override
    public void setTrackLineColumn(boolean trackLineColumn) {

    }

    public long getStreamPosition() {
        return streamPosition;
    }
}