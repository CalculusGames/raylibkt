package kray.shaders

import raylib.Shader

/**
 * The shader used for rendering with lighting and instancing support.
 */
val LIGHTING_SHADER: Shader
	get() = Shader.load("shaders/lighting.vs", "shaders/lighting.fs")

/**
 * The shader used for rendering with lighting and instancing support.
 */
val LIGHTING_SHADER_INSTANCED: Shader
	get() = Shader.load("shaders/lighting_instancing.vs", "shaders/lighting.fs")
