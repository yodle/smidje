# Data-Model

smidje uses a two step process to generate tests from the provided midje syntax. It will first convert the test syntax 
into a clojure map with all of the relevant test information. The map is then passed to a generator which will use it 
to build out a cljs compatible test. Currently the only supported generator is the cljs-test generator which converts
the map into cljs-test syntax.

```clojure
{:namespace ""
 :tests [{
            :name ""
            :metaconstants {...}
            :assertions [{
                            :function-under-test ""
                            :call-form ""
                            :position ""
                            :expected-result-form ""
                            :expected-result ""
                            :arrow ""
                            :throws-exception ""
                            :throws-message ""
                            :truth-testing ""
                            :provided [{:mock-function ()
                                        :return {'(<params>) {:result [...] ;list of results to support =stream=>
                                                              :calls ""
                                                              :arrow ""}}}]}]}]}
``` 
`:namespace`- The namespace of the fact.  
`:tests`- A vector of facts (this will be used for nesting which is not supported yet).  
`:name`- The name of a single fact.  
`:metaconstants`- A map of metaconstant info, with keys set to a generated, bindable symbol that can represent the
 metaconstant in test generation, mapped to the original metaconstant symbol.
set to generated symbols that the metaconstant is replaced with in the adjusted form.
`:assertions`- A vector of assertions which are individual arrow statements for example `(+ 1 1) => 2`  
`:function-under-test`- Quoted form of the function under test (the left side of the arrow)  
`:call-form`- The unquoted function under test  
`:position`- The line number of the arrow statement  
`:expected-result-form`- Quoted form of the expected result (right hand side of the arrow)  
`:expected-result`- Evaluated result of the expected value  
`:arrow`- The arrow that is being used (=>, =not=>, etc)  
`:throws-exception`- Present when testing an exception is thrown, indicates the exception type  
`:throws-message`- Optionally present when testing an exception, indicates the thrown exception message  
`:truth-testing`- Present when testing truthiness, indicates the desired truthiness checker  
`:provided`- A vector of maps representing mocks  
`:mock-function`- The function to mock  
`:return`- a map of parameters that will be passed to :mock-function and the corresponding value to be returned  
`:result`- a vector of result values to return (in preparation for the =stream=> arrow)  
`:calls`- the expected number of calls to this mock  
`:arrow`- The arrow used for mock creation (=>, =throws=>, etc.)                                                                                                                                                                                                                   