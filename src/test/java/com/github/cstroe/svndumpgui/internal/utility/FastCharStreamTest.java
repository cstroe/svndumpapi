package com.github.cstroe.svndumpgui.internal.utility;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FastCharStreamTest {

    private FastCharStream charStream;

    @Test
    public void parse_tokens() throws IOException {

        final InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("FastCharStreamTokens.txt");

        charStream = new FastCharStream(new InputStreamReader(inputStream));

        StringBuilder buffer = new StringBuilder();

        int number;
        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(16));

        assertThat(readData(number), is(equalTo("abcdefghijklmnop")));

        NEWLINE();
        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(3));

        assertThat(readData(number), is(equalTo("abc")));
        NEWLINE();

        READ(); COLON(); SPACE(); number = NUMBER(); NEWLINE();
        assertThat(number, is(5));

        assertThat(readData(number), is(equalTo("abcde")));
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
}