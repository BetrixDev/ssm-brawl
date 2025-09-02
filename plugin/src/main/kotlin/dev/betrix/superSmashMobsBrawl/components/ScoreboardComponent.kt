package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component as EcsComponent
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import gg.flyte.twilight.scoreboard.TwilightScoreboard
import net.kyori.adventure.text.Component

data class ScoreboardComponent(
    val scoreboard: TwilightScoreboard
) : EcsComponent<ScoreboardComponent> {
    override fun type() = ScoreboardComponent

    companion object : ComponentType<ScoreboardComponent>()

    // Composable scoreboard sections
    private var titleComponent: Component? = null
    private var headerLines: List<Component> = emptyList()
    private var contentLines: List<Component> = emptyList()
    private var footerLines: List<Component> = emptyList()
    private var tabHeader: Component? = null
    private var tabFooter: Component? = null

    override fun World.onRemove(entity: Entity) {
        scoreboard.delete()
    }

    // Helper methods for composable scoreboard management
    fun setTitle(title: Component) {
        this.titleComponent = title
    }

    fun setHeaderLines(lines: List<Component>) {
        this.headerLines = lines
    }

    fun setContentLines(lines: List<Component>) {
        this.contentLines = lines
    }

    fun setFooterLines(lines: List<Component>) {
        this.footerLines = lines
    }

    fun setTabHeader(header: Component) {
        this.tabHeader = header
    }

    fun setTabFooter(footer: Component) {
        this.tabFooter = footer
    }

    fun updateScoreboard(divider: Component? = null) {
        // Update title if set
        titleComponent?.let { scoreboard.updateSidebarTitle(it) }

        // Combine all sections with empty lines between them
        val allLines = mutableListOf<Component>()

        // Add top divider if provided
        divider?.let { allLines.add(it) }

        // Add header section
        if (headerLines.isNotEmpty()) {
            allLines.add(Component.empty())
            allLines.addAll(headerLines)
        }

        // Add content section
        if (contentLines.isNotEmpty()) {
            allLines.add(Component.empty())
            allLines.addAll(contentLines)
        }

        // Add footer section
        if (footerLines.isNotEmpty()) {
            allLines.add(Component.empty())
            allLines.addAll(footerLines)
        }

        // Add bottom divider if provided
        divider?.let {
            allLines.add(Component.empty())
            allLines.add(it)
        }

        // Update sidebar lines
        if (allLines.isNotEmpty()) {
            scoreboard.updateSidebarLines(*allLines.toTypedArray())
        }

        // Update tab list if header or footer is set
        if (tabHeader != null || tabFooter != null) {
            tabHeader?.let { { it } }?.let {
                tabFooter?.let { { it } }?.let { it1 ->
                    scoreboard.updateTabList(
                        header = it,
                        footer = it1
                    )
                }
            }
        }
    }
}