package dev.betrix.superSmashMobsBrawl.minigames.instances

/**
 * Represents the different reasons why a player's leave request might be denied.
 * This is used as the error type in Result<Unit, LeaveRequestDenialReason>.
 */
enum class LeaveRequestDenialReason {
    /**
     * The player cannot leave because the game is in a critical state.
     */
    GAME_IN_PROGRESS,
    
    /**
     * The player cannot leave because they are currently dead/spectating.
     */
    CURRENTLY_DEAD,
    
    /**
     * The player cannot leave because the game is about to end naturally.
     */
    GAME_ENDING_SOON,
    
    /**
     * The player cannot leave because it would end the game prematurely.
     */
    WOULD_END_GAME,
    
    /**
     * The player cannot leave due to competitive mode restrictions.
     */
    COMPETITIVE_MODE,
    
    /**
     * Generic denial reason.
     */
    NOT_ALLOWED
}