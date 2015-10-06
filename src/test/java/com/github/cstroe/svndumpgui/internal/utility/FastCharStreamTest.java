package com.github.cstroe.svndumpgui.internal.utility;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FastCharStreamTest {

    private FastCharStream charStream;

    @Test
    public void parse_tokens() throws IOException {

        final InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("FastCharStreamTokens.txt");

        charStream = new FastCharStream(new InputStreamReader(inputStream));

        int number;
        READ();
        assertThat(charStream.getStreamPosition(), is(4l));

        COLON();
        assertThat(charStream.getStreamPosition(), is(5l));

        SPACE();
        assertThat(charStream.getStreamPosition(), is(6l));

        number = NUMBER();
        assertThat(charStream.getStreamPosition(), is(8l));
        assertThat(number, is(16));

        NEWLINE();
        assertThat(charStream.getStreamPosition(), is(9l));

        assertThat(readData(number), is(equalTo("abcdefghijklmnop")));
        assertThat(charStream.getStreamPosition(), is(25l));

        NEWLINE();
        assertThat(charStream.getStreamPosition(), is(26l));

        READ();
        assertThat(charStream.getStreamPosition(), is(30l));

        COLON();
        assertThat(charStream.getStreamPosition(), is(31l));

        SPACE();
        assertThat(charStream.getStreamPosition(), is(32l));

        number = NUMBER();
        assertThat(charStream.getStreamPosition(), is(33l));
        assertThat(number, is(3));

        NEWLINE();
        assertThat(charStream.getStreamPosition(), is(34l));

        assertThat(readData(number), is(equalTo("abc")));
        assertThat(charStream.getStreamPosition(), is(37l));

        NEWLINE();
        assertThat(charStream.getStreamPosition(), is(38l));

        READ();
        assertThat(charStream.getStreamPosition(), is(42l));

        COLON();
        assertThat(charStream.getStreamPosition(), is(43l));

        SPACE();
        assertThat(charStream.getStreamPosition(), is(44l));

        number = NUMBER();
        assertThat(number, is(5));
        assertThat(charStream.getStreamPosition(), is(45l));

        NEWLINE();
        assertThat(charStream.getStreamPosition(), is(46l));


        assertThat(readData(number), is(equalTo("abcde")));
        assertThat(charStream.getStreamPosition(), is(51l));

        assertThat(charStream.buffer.length, is(FastCharStream.INITAL_BUFFER_LENGTH));
    }

    private void READ() throws IOException {
        charStream.BeginToken();
        charStream.readChar();
        charStream.readChar();
        charStream.readChar();

        assertThat(charStream.GetImage(), is(equalTo("READ")));
    }

    private void COLON() throws IOException {
        charStream.BeginToken();
        assertThat(charStream.GetImage(), is(equalTo(":")));
    }

    private void SPACE() throws IOException {
        charStream.BeginToken();
        assertThat(charStream.GetImage(), is(equalTo(" ")));
    }

    private int NUMBER() throws IOException {
        boolean tokenBegun = false;
        while(true) {
            char currentChar;
            if(tokenBegun) {
                currentChar = charStream.readChar();
            } else {
                currentChar = charStream.BeginToken();
                tokenBegun = true;
            }
            if(currentChar < '0' || currentChar > '9') {
                break;
            }
        }
        charStream.backup(1);

        return Integer.parseInt(charStream.GetImage());
    }

    private void NEWLINE() throws IOException {
        charStream.BeginToken();
        assertThat(charStream.GetImage(), is(equalTo("\n")));
    }

    private String readData(int length) throws IOException {
        charStream.BeginToken();
        for(int i = 0; i < length - 1; i++) {
            charStream.readChar();
        }
        return charStream.GetImage();
    }

    @Test
    public void parse_tokens_and_small_data() throws IOException {
        final InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("FastCharStreamTokens.txt");

        charStream = new FastCharStream(new InputStreamReader(inputStream));

        int number;
        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(16));

        assertThat(new String(charStream.readChars(number)), is(equalTo("abcdefghijklmnop")));
        assertThat(charStream.getStreamPosition(), is(25l));

        NEWLINE();
        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(3));

        assertThat(new String(charStream.readChars(number)), is(equalTo("abc")));
        assertThat(charStream.getStreamPosition(), is(37l));
        NEWLINE();

        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(5));

        assertThat(new String(charStream.readChars(number)), is(equalTo("abcde")));
        assertThat(charStream.getStreamPosition(), is(51l));

        assertThat(charStream.buffer.length, is(FastCharStream.INITAL_BUFFER_LENGTH));
    }

    @Test
    public void parse_tokens_and_big_data() throws IOException, NoSuchAlgorithmException {
        StringBuilder builder = new StringBuilder();

        byte[] md5sum_8192;
        {
            builder.append("READ: 8192\n");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            for (int i = 0; i < 8192; i++) {
                char nextChar = (char)('a' + i % 8);
                md5.update((byte) nextChar);
                builder.append(nextChar);
            }
            builder.append("\n");
            md5sum_8192 = md5.digest();
        }
        byte[] md5sum_10000;
        {
            builder.append("READ: 10000\n");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            for (int i = 0; i < 10000; i++) {
                char nextChar = (char)('a' + i % 8);
                md5.update((byte)nextChar);
                builder.append(nextChar);
            }
            builder.append("\n");
            md5sum_10000 = md5.digest();
        }

        builder.append("READ: 4\n");
        for(int i = 0; i < 4; i++) {
            char nextChar = (char)('a' + i % 8);
            builder.append(nextChar);
        }
        builder.append("\n");


        charStream = new FastCharStream(new StringReader(builder.toString()));

        int number;

        {
            READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
            assertThat(number, is(8192));
            assertThat(charStream.getStreamPosition(), is(11l));

            char[] firstBuffer = charStream.readChars(number);
            assertThat(charStream.getStreamPosition(), is(11l + 8192l));

            assertThat(firstBuffer.length, is(8192));
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5sum_firstBuffer = md5.digest(convert(firstBuffer));
            assertTrue(Arrays.equals(md5sum_8192, md5sum_firstBuffer));
        } {
            NEWLINE();
            READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
            assertThat(number, is(10000));
            assertThat(charStream.getStreamPosition(), is(8216l));

            char[] secondBuffer = charStream.readChars(number);
            assertThat(charStream.getStreamPosition(), is(18216l));

            assertThat(secondBuffer.length, is(10000));
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5sum_secondbuffer = md5.digest(convert(secondBuffer));
            assertTrue(Arrays.equals(md5sum_10000, md5sum_secondbuffer));
            NEWLINE();
        }

        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(4));
        assertThat(charStream.getStreamPosition(), is(18225l));

        assertThat(new String(charStream.readChars(number)), is(equalTo("abcd")));
        assertThat(charStream.getStreamPosition(), is(18229l));

        assertThat(charStream.buffer.length, is(FastCharStream.INITAL_BUFFER_LENGTH));
    }

    private byte[] convert(char[] array) {
        return new String(array).getBytes(StandardCharsets.US_ASCII);
    }

    @Test
    public void parse_tokens_and_two_consecutive_read_chars() throws IOException {
        StringBuilder builder = new StringBuilder();

        char[] firstHalf = new char[8192/2];
        char[] secondHalf = new char[8192/2];
        {
            builder.append("READ: 8192\n");
            for (int i = 0; i < 8192; i++) {
                char nextChar = (char)('a' + i % 8);
                if( i < 8192/2) {
                    firstHalf[i] = nextChar;
                } else {
                    secondHalf[i - (8192/2)] = nextChar;
                }
                builder.append(nextChar);
            }
            builder.append("\n");
        }

        charStream = new FastCharStream(new StringReader(builder.toString()));

        int number;
        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();

        assertThat(number, is(8192));

        char[] readChars = charStream.readChars(8192/2);
        assertTrue(Arrays.equals(firstHalf, readChars));

        readChars = charStream.readChars(8192/2);
        assertTrue(Arrays.equals(secondHalf, readChars));

        NEWLINE();
    }
}