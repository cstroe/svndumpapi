Scenario: The ContentChunkImpl copy constructor should make a deep copy.

Given a ContentChunkImpl with the content "great"
When we use the copy constructor to make a new ContentChunkImpl
And set the content of the new copy to "XXXXX"
Then the original ContentChunkImpl should still contain the content "great"
And the new ContentChunkImpl should contain the content "XXXXX"