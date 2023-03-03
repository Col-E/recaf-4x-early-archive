# Architecture: CDI

Recaf as an application is a CDI container. This facilitates dependency injection throughout the application.

## What is CDI though?

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

## What scope should be used for ... ?

Services that are effectively singletons will be `@ApplicationScoped`.

Services that depend on the current content of a workspace will be `@WorkspaceScoped`.

- In some cases, you may want to design a service as `@ApplicationScoped` and just pass in the `Workspace` as a method parameter.
  For instance, implementing a search. It needs `Workspace` access for sure, but the behavior is constant so it makes more
  sense to implement it this way as an `@ApplicationScoped` type.
- A strong case for `@WorkspaceScoped` are services that directly correlate with the contents of a `Workspace`.
  For instance, the inheritance graph service. The data it models will only ever be relevant to an active workspace.
  Having to pass in a `Workspace` every time would make implementing caching difficult.

Components acting only as views and wrappers to other components can mirror their dependencies' scope, 
or use `@Dependent` since its not the view that really matters, but the data backing it.

## Launching Recaf

When Recaf is launched, the `Bootstrap` class is used to initialize an instance of `Recaf`.
The `Bootstrap` class creates a CDI container that is configured to automatically discover implementations of the
servivces outlined in the `api` module. Once this process is completed, the newly made CDI container is wrapped in
a `Recaf` instance which lasts for the duration of the application.

## Why are so many UI classes `@Dependent` scoped?

There are multiple reasons.

**1. On principle, they should not model/track data by themselves**

For things like interactive controls that the user sees, they should not _ever_ track data by themselves.
If a control cannot be tossed in the garbage without adverse side effects, it is poorly designed.
These controls provide visual access to the data within the Recaf instance _(Like workspace contents)_, nothing more.

This is briefly mentioned before when discussing _"when should I use X scope?"_.

**2. CDI cannot create proxies of classes with `final` methods, which UI classes often define**

UI classes like JavaFX's `Menu` often have methods marked as `final` to prevent extensions to override certain behavior.
The `Menu` class's `List<T> getItems()` is one of these methods. This prevents any `Menu` type being marked as a scope
like `@ApplicationScoped` since our CDI implementation heavily relies on proxies for scope management.
When a class is `@Dependent` that means it effectively has no scope, so there is no need to wrap it in a proxy.

## When are components created?

CDI instantiates components when they are first used. If you declare an `@ApplicationScoped` component, but it is never
used anywhere, it will never be initialized.

If you want or need something to be initialized immediately when Recaf launches add the extra annotation `@EagerInitialization`.
Any component that has this will be initialized at the moment defined by the `value()` in `EagerInitialization`.
This annotation can be used in conjunction with `@ApplicationScoped` or `@WorkspaceScoped`.

There are two options:

**`IMMEDIATE`**: The component is initialized as soon as possible. 

- For `@ApplicationScoped` this occurs right after the CDI container is created.
- For `@WorkspaceScoped` this occurs right after a workspace is opened.

**`AFTER_UI_INIT`**: The component is initialized after the UI platform is initialized. 

- For `@ApplicationScoped` this occurs right after the UI platform is initialized, as expected.
- For `@WorkspaceScoped` this occurs right after a workspace is opened, with the assumption the UI is already initialized.

Be aware that any component annotated with this annotation forces all of its dependency components to also be initialized eagerly.