Description:
Define the behaviour of our implementation of the ContentChunk interface.

Scenario: The copy constructor should make a deep copy.

Given a ContentChunkImpl with the content "great"
When we pass it to the copy constructor of ContentChunkImpl
And set the content of the new copy to "XXXXX"
Then the original ContentChunkImpl should still contain the content "great"
And the new ContentChunkImpl should contain the content "XXXXX"

Scenario: The copy constructor should check for null content.

Given a ContentChunk that returns null content
When we pass it to the copy constructor of ContentChunkImpl
Then it should throw an java.lang.IllegalArgumentException

Scenario: The content constructor should not allow null content.

Given a null byte array
When we pass it to the constructor of ContentChunkImpl
Then it should throw an java.lang.IllegalArgumentException