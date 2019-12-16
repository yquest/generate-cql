package pt.fabm

import pt.fabm.types.Type

class Field(val name: String, val type: Type, val pkType: KeyType = KeyType.NONE, val order:Int = -1) {
    enum class KeyType {
        CLUSTER, PARTITION, NONE
    }
}