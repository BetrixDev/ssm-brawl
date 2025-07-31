package dev.betrix.superSmashMobsBrawl.minigames.instances

/**
 * Represents the result of a player's request to leave a minigame.
 * This provides clear semantics for minigames to approve or deny leave requests.
 */
enum class LeaveRequestResult(val message: String) {
    /**
     * The player is allowed to leave the minigame immediately.
     */
    APPROVED(""),
    
    /**
     * The player cannot leave because the game is in a critical state.
     * Used when leaving would unfairly impact other players.
     */
    DENIED_GAME_IN_PROGRESS("You cannot leave while the game is in progress"),
    
    /**
     * The player cannot leave because they are currently dead/spectating
     * and leaving would skip penalty mechanics.
     */
    DENIED_CURRENTLY_DEAD("You cannot leave while dead - wait for respawn or round end"),
    
    /**
     * The player cannot leave because the game is about to end naturally.
     */
    DENIED_GAME_ENDING_SOON("The game is ending soon - please wait"),
    
    /**
     * The player cannot leave because it would end the game prematurely
     * and other players haven't agreed to end.
     */
    DENIED_WOULD_END_GAME("Leaving would end the game for other players"),
    
    /**
     * The player cannot leave due to a tournament or competitive mode restriction.
     */
    DENIED_COMPETITIVE_MODE("You cannot leave during competitive play"),
    
    /**
     * Generic denial when none of the other reasons apply.
     * Minigames should provide a custom message in this case.
     */
    DENIED_CUSTOM("You cannot leave this minigame right now");
    
    /**
     * Returns true if the leave request was approved.
     */
    val isApproved: Boolean
        get() = this == APPROVED
        
    /**
     * Returns true if the leave request was denied.
     */
    val isDenied: Boolean
        get() = !isApproved
}