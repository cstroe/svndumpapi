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

Scenario: The content constructor properly sets the chunk's content.

Given a byte array with the content "someContent"
When we pass it to the constructor of ContentChunkImpl
Then the chunk's content should be "someContent"

Scenario: The content constructor should not allow null content.

Given a null byte array
When we pass it to the constructor of ContentChunkImpl
Then it should throw an java.lang.IllegalArgumentException

Scenario: The content setter updates the chunk's content.

Given a ContentChunkImpl with the content "ABCDE"
When we set the chunk's content to "abcd"
Then the chunk's content should be "abcd"

Scenario: The content setter does not allow null content.

Given a ContentChunkImpl with the content "1234"
When we set the chunk's content to null
Then it should throw an java.lang.IllegalArgumentException

Scenario: The toString method should print the length.

Given a ContentChunkImpl with the content "<content>"
Then the toString method should return "<toString>"

Examples:
|content|toString|
||0 bytes|
|12345|5 bytes|
|99999999999999999999999999999999|32 bytes|