# raylibkt

> Kotlin/Native bindings for raylib

## Overview

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
![GitHub License](https://img.shields.io/github/license/CalculusGames/raylibkt)

![badge-mac](http://img.shields.io/badge/platform-macos-cccccc.svg?style=flat)
![badge-linux-x64](http://img.shields.io/badge/platform-linux--x64-2D3F6C.svg?style=flat)
![badge-windows](http://img.shields.io/badge/platform-windows-4D76CD.svg?style=flat)

[raylib](https://github.com/raysan5/raylib) is a free and open source library to enjoy videogame programming in C.

This repository provides Kotlin/Native bindings for raylib, allowing you to use raylib in Kotlin/Native projects and
make games or graphical applications in Kotlin.

## Installation

```kts
plugins {
    kotlin("multiplatform") version "[KOTLIN_VERSION]"
}

dependencies {
    implementation("xyz.calcugames:raylibkt:[VERSION]")
}
```

## Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.
