Although parsing of the Relationships table should be easy enough to create dictories with its content, it's a bit of a challenge (because of its recursive nature).

As a result, another column will be added to the Relationships table called Path, which will store the path to each node when added. So, when the root node is added (Professor), it's value in the Path column would be 'Processor'. Then when 2 children are added to Professor (eg. Teaching, Research), the values for their paths would be 'Professor\Teaching' and 'Professor\Research' respectively. The path to the parent would simply be fetched during the same time the parent's id is being fetched.

This way, when it's time to create directories, we simply iterate through the Path's column of the Relationships table and create the paths found.

A third column will be added to the Relationships table called 'Relation' which will specify how a parent and a child are related