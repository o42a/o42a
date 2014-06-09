o42a Programming Language
=========================

[o42a][] is a high-level general purpose programming language.
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

> See the [Mission Statement][].  
> Take a look at the [feature list][] for overall language description.  
> Read the [documentation][] for the details.

Development Status
------------------
A prototype of compiler is under development. This compiler is written in
Java and uses [LLVM](http://llvm.org/) for executable code generation.

All documented language features are implemented. The development currently
focused on core libraries implementation, compiler stability, and implementation
of additional language features necessary to make libraries handy and efficient.

> See the detailed [development][] status and plan.

Hello, World!
-------------
Here is a "Hello, World!" program written in o42a:
```o42a
Use namespace 'Console'

@Main (
  Print "Hello, World!" nl
)
```
> See the [explanation][hello world].


[o42a]:              <http://o42a.org/>
    "o42a programming language official site"
[Mission statement]: <http://o42a.org/devel/mission>
    "o42a mission statement"
[Feature list]:      <http://o42a.org/docs/intro/features>
    "o42a features"
[Documentation]:     <http://o42a.org/docs/>
    "o42a documentation"
[Development]:       <http://o42a.org/devel/>
    "o42a development status"
[Hello world]:       <http://o42a.org/docs/intro/hello_world_explained>
    ("Hello, World!" explained)
