# More spliterators

This is a set of four spliterators that allow operations on streams that are not part of the classical map / filter / reduce operations.

Basically, a classical stream operation can only work on the current element of the given stream, but cannot take into account the previous elements.

The right solution is to change the stream itself. Suppose we have a stream `{a, b, c, d, ...}` and that we need to remember the value of `a` when we process `b`. The problem can be solved by changing this stream to the following : `{[a, b], [b, c], [c, d], ...}`. We go from a stream of objects to a stream of pairs of objects.

This problem can be solved by creating a spliterator on the original stream spliterator. This is the object of this API.

## The GroupingSpliterator

## The RollingSpliterator

## The Weaving Spliterator

## the Zipping Spliterator

