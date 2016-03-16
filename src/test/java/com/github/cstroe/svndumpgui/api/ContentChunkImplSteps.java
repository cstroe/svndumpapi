package com.github.cstroe.svndumpgui.api;

import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContentChunkImplSteps {

    private byte[] content;
    private ContentChunk contentChunk;
    private ContentChunk newContentChunk;

    @Given("a ContentChunkImpl with the content \"$content\"")
    public void aContentChunk(byte[] content) {
        contentChunk = new ContentChunkImpl(content);
    }

    @When("we use the copy constructor to make a new ContentChunkImpl")
    public void newContentChunk() {
        newContentChunk = new ContentChunkImpl(contentChunk);
    }

    @When("set the content of the new copy to \"$content\"")
    public void setNewContentChunkContent(byte[] content) {
        byte[] oldContent = newContentChunk.getContent();
        System.arraycopy(content, 0, oldContent, 0, content.length);
    }

    @Then("the original ContentChunkImpl should still contain the content \"$content\"")
    public void checkOriginalContent(byte[] content) {
        assertTrue(Arrays.equals(content, contentChunk.getContent()));
    }

    @Then("the new ContentChunkImpl should contain the content \"$content\"")
    public void checkNewContent(byte[] content) {
        assertTrue(Arrays.equals(content, newContentChunk.getContent()));
    }

    @When("we instantiate a ContentChunkImpl with null content")
    public void chunkWithContent() {
        content = null;
    }

    @Then("it should throw an $exception")
    public void shouldThrowException(String exception) throws ClassNotFoundException {
        Class<?> exceptionClass = Class.forName(exception);
        try {
            new ContentChunkImpl(content);
        } catch (Exception ex) {
            assertEquals(exceptionClass, ex.getClass());
        }
    }
}
