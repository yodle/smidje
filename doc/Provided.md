# Provided And Function Mocking

###Mocking Using Provided

Smidje gives you the ability to mock out functions using the provided function. 
Lets take a look at a simple example. For our example we will have two functions foo and bar
defined below.

```clojure
(defn foo [x]
  (throw js/Error. "unimplemented")
)
(defn bar []
  (inc (foo 1));
)
```
foo is an unimplemented function which bar uses for it's calculation. Normally calling bar would result
in an exception which is not very useful for unit testing bar. By using provided we can override how foo behaves, allowing
us to test bar.

```clojure
(fact "bar increments foo"
  (bar) => 1
  (provided 
    (foo 1) => 0)
)
```

###Implementation of Provided

under the hood smidje will wrap your test in a with-redefs block and redefine the functions specified in the provided block
with a mocking function. The mocking function will return the specified value for the designated function inputs and nil otherwise.
For example in the above sample test we have

```clojure
(fact 
  ....
  (provided 
    (foo 1) => 0)
)
```

Smidje will replace foo with a mock function that returns 0 when called with the argument 1 and nil otherwise so
`(mock-foo 1) = 0` but `(mock-foo 2) = nil`

The mock function will also keep track of calls made to it and the arguments used. At the end of your test Smidje will validate
the following:
  1) All functions in the provided block were called with the specified arguments at least once
  2) No calls were made to the mocked functions with arguments that were not specified