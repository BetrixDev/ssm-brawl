package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class Tags : EntityTags by entityTagOf() {
    DEAD,
    ELIMINATED
}
