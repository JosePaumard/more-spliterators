# More spliterators

Basically, a classical stream operation can only work on the current element of the given stream, but cannot take into account the previous elements.

The right solution is to change the stream itself. Suppose we have a stream `{a, b, c, d, ...}` and that we need to remember the value of `a` when we process `b`. The problem can be solved by changing this stream to the following : `{[a, b], [b, c], [c, d], ...}`. We go from a stream of objects to a stream of pairs of objects, or maybe a stream of streams of objects.

This problem can be solved by creating a spliterator on the original stream spliterator. This is the object of this API.

The entry point of this API is meant to be the `MoreSpliterators` factory class. Reading the Javadoc is a good ideas, patterns are provided.

So far this API provides seven operations

## Cycling

Takes a stream and repeats it forever, as long as this stream has a finite size. 

## Grouping

Takes a stream `[a, b, c, d]` and returns `[[a, b], [c, d]]`. The grouping factor is parametrized.

## Repeating

Takes a stream `[a, b, c, d]` and returns `[a, a, b, b, c, c, d, d]`. The repeating factor is parametrized.

## Rolling

Takes a stream `[a, b, c, d, e]` and returns `[[a, b], [b, c], [c, d], [d, e]]`. The rolling factor is parametrized.

## Traversing

Takes a set of streams and builds a stream of substreams. Each substream is made of the nth element of the corresponding stream. For instance, if we have:
```
stream0 = ["a00", "a01", "a02", "a03"]
stream1 = ["a10", "a11", "a12", "a13"]
stream2 = ["a20", "a21", "a22", "a23"]
stream3 = ["a30", "a31", "a32", "a33"]
```

The resulting stream is the following:
```
[["a00", "a10", "a20", "a30"],
 ["a01", "a11", "a21", "a31"],
 ["a02", "a12", "a22", "a32"],
 ["a03", "a13", "a23", "a33"]]
```

## Weaving

The weaving operator is another version of the traversing operator, it could be seen as a traversing followed by a flatmap. Here is an example:
```
stream0 = ["a00", "a01", "a02", "a03"]
stream1 = ["a10", "a11", "a12", "a13"]
stream2 = ["a20", "a21", "a22", "a23"]
stream3 = ["a30", "a31", "a32", "a33"]
```

The resulting stream is the following:
```
["a00", "a10", "a20", "a30", "a01", "a11", "a21", "a31",
 "a02", "a12", "a22", "a32", "a03", "a13", "a23", "a33"]
```

## Zipping

The zipping operator takes two streams and a bifunction. The resulting stream is the application of the bifunction on two elements of the streams, one at a time. 

## Acknowledgements

Many thanks to Rémi Forax for his valuable advice during the development of this API. 

Now Rémi you can implement `FizzBuzz` using the following code:
```
Stream<String> fizzBuzz = 
   zip(
      IntStream.range(0, 101).boxed(), 
      zip(
         cycle(Stream.of("fizz", "", "")), 
         cycle(Stream.of("buzz, "", "", "", ""))
         String::concat
      ), 
      (i, string) -> string.isEmpty() ? i.toString() : string
   );
fizzBuzz.skip(1).forEach(System.out::println);
```