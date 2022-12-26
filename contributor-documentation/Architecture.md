# Architecture

This article discusses the high level architecture design of Recaf.

## Modules

Recaf's backend is split into two modules. The `api` and `core` modules.

### API

This portion of Recaf's source contains mostly interfaces and a few basic implementations for regular data types.
The interfaces and classes here can be thought of as a _"library"_ in their design. Recaf as an application does not
exist within this module, but almost all of its features are outlined here.

### Core

This portion of Recaf's source is where the _"application"_ logic of Recaf begins to form.

**CDI**

Recaf as an application is a CDI container. This facilitates dependency injection throughout the application.

> What is CDI though?

CDI is [Contexts and Dependency Injection for Java EE](https://www.cdi-spec.org/). If that sounds confusing here's what
that actually means in practice. When a `class` implements one of Recaf's service interfaces we need a way to access
that implementation so that the feature can be used. CDI uses annotations to determine when to allocate new instances
of these implementations. The main three used in Recaf are the following:

- `@ApplicationScoped`: This implementation is lazily allocated once and used for the entire duration of the application.
- `@WorkspaceScoped`: This implementation is lazily allocated once, but the value is then thrown out when a new `Workspace`
                      is loaded. This way when the implementation is requested an instance linked to the current workspace
                      is always given.
- `@Dependent`: This implementation is not cached, so a new instance is provided every time upon request.

When creating a class in Recaf, you can supply these implementations in a constructor that takes in parameters for all
the needed types, and is annotated with `@Inject`. This means you will not be using the constructor yourself. You will 
let CDI allocate it for you. Your new class can then also be used the same way via `@Inject` annotated constructors.

> What does that look like?

Let's assume a simple case. We'll create an interface outlining some behavior, like compiling some code.
We will create a single implementation class and mark it as `@ApplicationScoped` since it is not associated with
any specific state, like the current Recaf workspace.
```java
interface Compiler {
	byte[] build(String src);
}

@ApplicationScoped
class CompilerImpl implements Compiler {
	@Override
	Sbyte[] build(String src) { ... }
}
```
Then in our UI we can create a class that injects the base `Compiler` type. We do not need to know any implementation
details. Because we have only one implementation the CDI container knows the grab an instance of `CompilerImpl` and
pass it along to our constructor annotated with `@Inject`.
```java
@WorkspaceScoped
class CompilerGui {
	TextArea textEditor = ...
	
	@Inject CompilerGui(Compiler compiler) { this.compiler = compiler; }
    
    // called when user wants to save (CTRL + S)
    void onSaveRequest() {
		byte[] code = compiler.build(textEditor.getText());
    }
}
```

> What if I want multiple implementations?

Recaf has multiple decompiler implementations built in. Lets look at a simplified version of how that works.
Instead of declaring a parameter of `Decompiler` which takes _one_ value, we use `Instance<Decompiler>` which
is an `Iterable<T>` allowing us to loop over all known implementations of the `Decompiler` interface.
```java
@ApplicationScoped
class DecompileManager {
	@Inject DecompileManager(Instance<Decompiler> implementations) {
		for (Decompiler implementation : implementations)
			registerDecompiler(implementation);
    }
}
```
From here, we can define methods in `DecompileManager` to manage which decompile we want to use.
Then in the UI, we `@Inject` this `DecompileManager` and use that to interact with `Decompiler` instances
rather than directly doing so.

> What if I need a value dynamically, and getting values from the constructor isn't good enough?

In situations where providing values to constructors is not feasible, the `Recaf` class provides methods for accessing
CDI managed types.

- `Instance<T> instance(Class<T>)`: Gives you a `Supplier<T>` / `Iterable<T>` for the requested type `T`.
                                    You can use `Supplier.get()` to grab a single instance of `T`, 
                                    or use `Iterable.iterator()` to iterate over multiple instances of `T` if more than
                                    one implementation exists.
- `T get(Class<T>)`: Gives you a single instance of the requested type `T`.

**Launching**

When Recaf is launched, the `Bootstrap` class is used to initialize an instance of `Recaf`.
The `Bootstrap` class creates a CDI container that is configured to automatically discover implementations of the
servivces outlined in the `api` module. Once this process is completed, the newly made CDI container is wrapped in
a `Recaf` instance which lasts for the duration of the application.