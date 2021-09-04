package com.github.cstroe.svndumpgui.internal.utility;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;

public class SvnDumpCharStreamTest {

    private SvnDumpCharStream charStream;

    @Test
    public void parse_tokens() throws IOException {

        final InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("FastCharStreamTokens.txt");

        charStream = new SvnDumpCharStream(inputStream);

        int number;
        READ();
        assertThat(charStream.getStreamPosition(), is(4L));

        COLON();
        assertThat(charStream.getStreamPosition(), is(5L));

        SPACE();
        assertThat(charStream.getStreamPosition(), is(6L));

        number = NUMBER();
        assertThat(charStream.getStreamPosition(), is(8L));
        assertThat(number, is(16));

        NEWLINE();
        assertThat(charStream.getStreamPosition(), is(9L));

        assertThat(readData(number), is(equalTo("abcdefghijklmnop")));
        assertThat(charStream.getStreamPosition(), is(25L));

        NEWLINE();
        assertThat(charStream.getStreamPosition(), is(26L));

        READ();
        assertThat(charStream.getStreamPosition(), is(30L));

        COLON();
        assertThat(charStream.getStreamPosition(), is(31L));

        SPACE();
        assertThat(charStream.getStreamPosition(), is(32L));

        number = NUMBER();
        assertThat(charStream.getStreamPosition(), is(33L));
        assertThat(number, is(3));

        NEWLINE();
        assertThat(charStream.getStreamPosition(), is(34L));

        assertThat(readData(number), is(equalTo("abc")));
        assertThat(charStream.getStreamPosition(), is(37L));

        NEWLINE();
        assertThat(charStream.getStreamPosition(), is(38L));

        READ();
        assertThat(charStream.getStreamPosition(), is(42L));

        COLON();
        assertThat(charStream.getStreamPosition(), is(43L));

        SPACE();
        assertThat(charStream.getStreamPosition(), is(44L));

        number = NUMBER();
        assertThat(number, is(5));
        assertThat(charStream.getStreamPosition(), is(45L));

        NEWLINE();
        assertThat(charStream.getStreamPosition(), is(46L));


        assertThat(readData(number), is(equalTo("abcde")));
        assertThat(charStream.getStreamPosition(), is(51L));

        assertThat(charStream.buffer.length, is(SvnDumpCharStream.INITAL_BUFFER_LENGTH));
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

        charStream = new SvnDumpCharStream(inputStream);

        int number;
        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(16));

        assertThat(new String(charStream.readBytes(number)), is(equalTo("abcdefghijklmnop")));
        assertThat(charStream.getStreamPosition(), is(25L));

        NEWLINE();
        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(3));

        assertThat(new String(charStream.readBytes(number)), is(equalTo("abc")));
        assertThat(charStream.getStreamPosition(), is(37L));
        NEWLINE();

        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(5));

        assertThat(new String(charStream.readBytes(number)), is(equalTo("abcde")));
        assertThat(charStream.getStreamPosition(), is(51L));

        assertThat(charStream.buffer.length, is(SvnDumpCharStream.INITAL_BUFFER_LENGTH));
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


        charStream = new SvnDumpCharStream(new ByteArrayInputStream(builder.toString().getBytes()));

        int number;

        {
            READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
            assertThat(number, is(8192));
            assertThat(charStream.getStreamPosition(), is(11L));

            byte[] firstBuffer = charStream.readBytes(number);
            assertThat(charStream.getStreamPosition(), is(11L + 8192L));

            assertThat(firstBuffer.length, is(8192));
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5sum_firstBuffer = md5.digest(firstBuffer);
            assertArrayEquals(md5sum_8192, md5sum_firstBuffer);
        } {
            NEWLINE();
            READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
            assertThat(number, is(10000));
            assertThat(charStream.getStreamPosition(), is(8216L));

            byte[] secondBuffer = charStream.readBytes(number);
            assertThat(charStream.getStreamPosition(), is(18216L));

            assertThat(secondBuffer.length, is(10000));
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5sum_secondbuffer = md5.digest(secondBuffer);
            assertArrayEquals(md5sum_10000, md5sum_secondbuffer);
            NEWLINE();
        }

        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(4));
        assertThat(charStream.getStreamPosition(), is(18225L));

        assertThat(new String(charStream.readBytes(number)), is(equalTo("abcd")));
        assertThat(charStream.getStreamPosition(), is(18229L));

        assertThat(charStream.buffer.length, is(SvnDumpCharStream.INITAL_BUFFER_LENGTH));
    }

    @Test
    public void parse_tokens_and_two_consecutive_read_chars() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] fullBuffer = new byte[8192];
        byte[] firstHalf = new byte[8192/2];
        byte[] secondHalf = new byte[8192/2];
        {
            baos.write("READ: 8192\n".getBytes());
            for (int i = 0; i < 8192; i++) {
                byte nextChar = (byte)('a' + i % 8);
                fullBuffer[i] = nextChar;
                if( i < 8192/2) {
                    firstHalf[i] = nextChar;
                } else {
                    secondHalf[i - (8192/2)] = nextChar;
                }
            }
            baos.write(fullBuffer);
            baos.write("\n".getBytes());
        }

        charStream = new SvnDumpCharStream(new ByteArrayInputStream(baos.toByteArray()));

        int number;
        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();

        assertThat(number, is(8192));

        byte[] readChars = charStream.readBytes(8192 / 2);
        assertThat(firstHalf, is(equalTo(readChars)));

        readChars = charStream.readBytes(8192 / 2);
        assertThat(secondHalf, is(equalTo(readChars)));

        NEWLINE();
    }

    @Test
    public void parse_lower_UTF8_chars() throws IOException {
        final String utf8String = "abČdԵfgђlͷȎ";

        String builder = "READ: " +
                utf8String.length() +
                "\n" +
                utf8String +
                "\n";
        charStream = new SvnDumpCharStream(new ByteArrayInputStream(builder.getBytes()));

        int number;
        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(utf8String.length()));

        StringBuilder chars = new StringBuilder();
        for(int i = 0; i < number; i++) {
            chars.append(charStream.readChar());
        }

        assertThat(chars.toString(), is(equalTo(utf8String)));
    }
}