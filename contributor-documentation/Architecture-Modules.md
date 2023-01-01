# Architecture: Modules

Recaf's backend is split into two modules. The `api` and `core` modules.

## API

This portion of Recaf's source contains mostly interfaces and a few basic implementations for regular data types.
The interfaces and classes here can be thought of as a _"library"_ in their design. Recaf as an application does not
exist within this module, but almost all of its features are outlined here.

## Core

This portion of Recaf's source is where the _"application"_ logic of Recaf begins to form. Interfaces are given more 
complex implementations in this module, especially if they depend on specific 3rd party libraries _(As to not bloat
the dependency tree of the `api` module)_.