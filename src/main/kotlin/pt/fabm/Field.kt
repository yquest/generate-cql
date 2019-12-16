package pt.fabm

import pt.fabm.types.Type

class Field(val name: String, val type: Type, val pkType: KeyType = KeyType.NONE) {
    enum class KeyType {
        CLUSTER, PARTITION, NONE
    }
}