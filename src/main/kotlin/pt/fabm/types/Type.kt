package pt.fabm.types

interface Type {
    val literalName: String
    fun asFrozen(): FrozenType = FrozenType(this)
}