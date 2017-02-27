# MiniJavaCompiler
It is a mini java compiler which can be used to compile mini-java program. The destination code can be used on JVM.
We use Java as the implementation programming language.
It contains several modules include the Straight-Line Programming Language (SLP) and its interpreter and compiler; the lexer, , the parser, code generators,a garbage collector, an abstract syntax tree for MiniJava, an elaborator for the MiniJava language, which involves symbol table design, type checking, and translation.

## Technology
Java, C, Compilers Principles, Object-Oriented, Design Pattern(Observer Pattern), Garbage Collector, Memory Maps

## Modules
### lexer and parser
+ As its name illustrates, SLP is very simple in that it contains no control structures as found in most languages.
+ Generally speaking, there are two ways to run a program: interpretation and compilation. In this part, you will first write an interpreter for SLP, and in the next part, you will write a compiler so that you can gain deeper understanding of both the two ways.
+ Basically, an interpreter runs a given program online, that is, it analyzes the program and mimic the behaviour of the program during the analysis. As SLP has assignment statement like x:=e, so the interpreter must abstract the memory, which keeps track of the current value of a variable. There are several ways to implement an abstract memory: you can use a imperative memory, or you can use functional memory.
+ In an imperative memory, when a variable x is assigned a new value v, the memory chunk for x will be updated (thus, the old memory state is modified); whereas in a functional memory, when some variable is changed, a new memory is generated without modifying the old memory. Thus, a functional memory model, and generally speaking, a functional programming style, can make the code much more elegant and maintainable.

### abstract syntax trees and elaborator
+ We are implementing a compiler for MiniJava using the implementation language Java, so we use a so-called local class hierarchy technique from Java. Essentially, to represent a program written in MiniJava, we define a set of classes. 
+ Before continuing to do other operations on the abstract syntax trees, one must check these trees to ensure they are well-formed. By the terminology well-formed, we mean that the input MiniJava program must obey all constraint rules specified by the MiniJava language specification (in turn by the Java specification). Typical rules include: a variable must be declared first before its use; the "+" operators must apply to two operands of integer type; the methods being invoked on an object must have been defined in its class or superclass; and so on. 

### code generator
+ To be specific, there are 4 code generators in this lab: the first one is a C code generator generating ANSI C code, the second one is a Java bytecode generator targeting Oracle's JVML (this is Java's standard target code format), the third one is a Dalvik bytecode generator targeting Google's Dalvik virtual machine, and the fourth and last one is an x86 code generator targeting x86 chips from both Intel and AMD.

### garbage collector
+ It can manage the Java heap in an automatic way, if the garbage collector was linked to the Mini Java Compiler
+ During the execution of a Java application on a uniprocessor, the application allocates objects in the Java heap, and whenever the application need to allocate more objects but there is no enough space left in the Java heap, the application will be temporarily stopped (the so-called stop-the-world) and the garbage collector takes over to reclaim garbages in the Java heap. And after this round of collection, hopefully there is enough space available so that the application can resume.
+ Conceptually, the task of a garbage collector can be divided into two sub-tasks: scanning and collecting. During the scanning phase, the collector scans through the (object) directed graph to mark which objects are reachable whereas which are not. Then, during the collecting phase, the collector will reclaim unreachable objects to obtain more spaces. After the collection, it's often the case that there will be more spaces in the heap for future allocations. Of course, the division of the garbage collection into canning and collecting is more of explanation purpose. Many real garbage collectors combine the two phases in some way or even make use of other strategies.

