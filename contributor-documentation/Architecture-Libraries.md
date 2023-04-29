# Architecture: Libraries used

## API

**JVM Bytecode Manipulation**: Recaf uses [ASM](https://asm.ow2.io/) and [CafeDude](https://github.com/Col-E/CAFED00D)
to parse bytecode. Most operations will be based on ASM since heavily abstracts away the class file format, making 
what would otherwise be tedious work simple. CafeDude is used for lower level operations and patching classes that
are not compliant with ASM.

**Android Dalvik Bytecode Manipulation**: Recaf uses [DexLib & Smali](https://github.com/google/smali) to parse
and manipulate Android bytecode. To translate between Dalvik and JVM bytecode we use 
[Dex-Translator](https://github.com/Col-E/dex-translator).

**ZIP Files**: Recaf uses [LL-Java-Zip](https://github.com/Col-E/LL-Java-Zip) to read ZIP files. We do not use the
standard `java.util.zip` classes because they do not allow for parsing a number of edge cases used certain obfuscators.

## Core

**CDI**: Recaf uses [Weld](https://weld.cdi-spec.org/) as its CDI implementation. 
You can read the [CDI article](Architecture-CDI.md) for more information.

## UI

**JavaFX**: Recaf uses [JavaFX](https://openjfx.io/) as its UI framework.
The observable property model it uses makes managing live updates to UI components when backing data is changed easy.
Additionally, it is styled via CSS which makes customizing the UI for Recaf-specific operations much more simple 
as opposed to something like Swing.

**AtlantaFX**: Recaf uses [AtlantaFX](https://github.com/mkpaz/atlantafx) to handle common theming.

**Ikonli**: Recaf uses [Ikonli](https://github.com/kordamp/ikonli) for scalable icons.
Specifically the [Carbon](https://kordamp.org/ikonli/cheat-sheet-carbonicons.html) pack.

**Docking**: Recaf uses [Tiwul-FX's docking](https://github.com/panemu/tiwulfx-dock) framework for handling dockable
tabs across all open windows.