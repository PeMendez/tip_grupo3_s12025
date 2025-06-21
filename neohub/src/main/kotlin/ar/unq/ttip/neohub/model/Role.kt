package ar.unq.ttip.neohub.model

enum class Role {
    ADMIN,
    USER;

    companion object {
        fun fromString(s: String): Role {
            return when (s) {
                "ADMIN" -> ADMIN
                "USER" -> USER
                else -> throw IllegalArgumentException("Unknown Role: $s")
            }
        }
    }
}

