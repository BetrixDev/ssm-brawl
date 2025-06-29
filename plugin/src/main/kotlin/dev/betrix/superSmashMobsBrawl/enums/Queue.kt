package dev.betrix.superSmashMobsBrawl.enums

enum class Queue(
    val id: String,
    val playersPerTeam: Int,
    val totalTeams: Int,
    val displayName: String,
) {
    ONE_ON_ONE_DUALS(id = "1v1", playersPerTeam = 1, totalTeams = 2, displayName = "1v1 Duels");

    companion object {
        fun fromId(id: String): Queue? {
            return entries.find { it.id == id }
        }

        fun findClosest(id: String): Queue? {
            if (id.isBlank()) return null

            // First try exact match
            fromId(id)?.let {
                return it
            }

            // Then try case-insensitive match
            entries
                .find { it.id.equals(id, ignoreCase = true) }
                ?.let {
                    return it
                }

            // Finally try partial match (contains)
            return entries.find { it.id.contains(id, ignoreCase = true) }
        }
    }
}
