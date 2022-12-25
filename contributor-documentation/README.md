# Contributor Documentation Index

This document contains a brief overview of what a developer should know when working on Recaf.
Do not take this as a requirements list, but more like a wish-list. Knowing more is always great but
not strictly necessary.

More in depth information can be found here:

 - [Architecture](Architecture.md)
 - [Workspaces: What are they and how do I work with them?](Workspace.md)

With that in mind, here are the big three questions:

1. What is Recaf?
2. What are the separate modules for?
3. What concepts do I need to get started on contributing?

## 1. What is Recaf?

Recaf is a one-stop solution for everything Java bytecode reverse engineering. 
For users this means having a variety of useful tools and capabilities:

- Decompiling
  - Multiple decompilers and the ability to configure each to ensure something is always available if one decompiler fails.
- Bytecode disassemble & reassemble
  - A rich disassembler to make modifying Java bytecode as simple as possible.
  - Simplifies and abstracts away complex tasks and concepts such as stack frames, indexed variables, etc
  - Provides useful feedback when users make mistakes editing bytecode.
- Recompiling
  - When possible _(non-obfuscated classes)_ allowing the compiler to be used to make sense eliminates the need to learn any Java bytecode.
  - Recaf can, in most circumstances, eliminate the need to specify dependencies since it can fill in the blanks with analysis of the whole input program.
- Searching
  - Strings, numbers, references to classes/fields/methods, and more all through an easy-to-use interface.
- Automation
  - Scripting support allows users to create and share scripts for automating a variety of tasks.
  - Plugin support for advanced cases not suitable for single script files, such as adding new functionality, automatically responding to certain events, etc.
- And much more
  - Recaf offers many more advanced features, but for a vast majority of users, this is what Recaf is.

## 2. What are the separate modules for?

### `recaf-api`

Defines common interfaces describing the capabilities of Recaf and its services.
For data types, such as the contents of workspaces, basic implementations are also offered here.

### `recaf-core`

There are two key points for this module:

**1. Defines implementations of the interfaces / services given in the `api` module.**

While there are a number of basic implementations in the `api` module, they are intended to be minimal in design. 
Consumers of an API do not need to know how it is implemented, and for the sake of simplicity it's often better
to offer them an outline of capabilities instead of a flood of implementation details. That sort of content goes here.

**2. Defines the lifecycle of the Recaf application**

Where the `api` module can be seen as a library, the `core` module is where the actual application logic 
for Recaf begins.

### `recaf-ui`

The default JavaFX UI implementation of features defined in the `api` and `core` modules.

## 3. What concepts do I need to get started on contributing?

That depends entirely on what you want to contribute. 

**Translations**: Additional translations do not require any prior technical knowledge for the most part.

**Ideas**: You're always free to open an issue if you have an idea for something useful to add.

**Bug reports**: Please, if you encounter any issues do share ***all*** the information that you can. 
We cannot fix issues we are not aware of. In a lot of cases we need samples of the workspace files used
to fully diagnose an issue and determine a fix. Understandably, that is not always possible, but it helps
to at least be aware of issues even if we cannot use your samples to fix the problem immediately.

**Features**: While the list may vary between different sections of the code base, in general you should know:

 - Core Java language features
 - A rough understanding of how the Java class file format is laid out
   - Bonus: How to read/write/modify classes with Objectweb ASM
 - How to write testable code
   - Additions to the `api` and `core` modules should be tested to show examples of expected behavior.
 - Contexts and Dependency Injection (CDI)
   - Recaf uses CDI to facilitate automatically passing necessary components to class constructors 
     labeled with `@Inject`. For the most part if you're not creating new services you don't need to worry about how
     this works.