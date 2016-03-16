Scenario: The content chunk copy constructor should make a deep copy.

Given a ContentChunkImpl with the content "great"
When we use the copy constructor to make a new ContentChunkImpl
And set the content of the new copy to "XXXXX"
Then the original ContentChunkImpl should still contain the content "great"
And the new ContentChunkImpl should contain the content "XXXXX"

Scenario: Cannot instantiate a content chunk with null content

When we instantiate a ContentChunkImpl with null content
Then it should throw an java.lang.IllegalArgumentException