o42a Programming Language
=========================

[o42a](http://o42a.org/) is a high-level general purpose programming language.
It is:

* **compiled**,
* **statically-typed**,
* **prototype-based**,
* **logic-driven**, and
* primarily **declarative**, while
* the **imperative** programming style is also supported. 

A program written in o42a is closer to natural English text than one written
in any C-like programming language.

The language is designed with programming productivity and code maintainability
as main priorities. This achieved by

* **powerful, yet restrained, semantics**, and
* **expressive and natural syntax**.

>>>>>>>>>>>> See the [Mission Statement](http://o42a.org/devel/mission).

>>>>>>>>>>>> Take a look at the
[feature list](http://o42a.org/docs/intro/features)
for overall language description.

>>>>>>>>>>>> Read [documentation](http://o42a.org/docs/) for the details.


Development Status
------------------
A prototype of compiler is under development. This compiler is written in
Java and uses [LLVM](http://llvm.org/) for executable code generation.

Implementation status is draft. The majority of the language features are
already in place. Development is currently focused on compiler stability,
testing and implementation of the missing features.

>>>>>>>>>>>> See the detailed [development](http://o42a.org/devel/)
status and plan.

Hello, World!
-------------
Here is a "Hello, World!" program written in o42a:

    Use namespace 'Console'.

    @Main := *{
      Print "Hello, World!" nl.
    }

>>>>>>>>>>>> See the
[explanation](http://o42a.org/docs/intro/hello_world_explained).
