package pt.fabm.types

class MapType(val key: Type, val value: Type) :
    Type {
    override val literalName: String get() =
        "map<${key.literalName.toLowerCase()}, ${value.literalName.toLowerCase()}>"
}