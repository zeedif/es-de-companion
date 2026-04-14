package com.esde.companion.data

/**
 * Represents the current state of the ES-DE Companion app.
 *
 * Using a sealed class ensures only one state can be active at a time,
 * preventing impossible state combinations (e.g., can't be in GamePlaying
 * AND Screensaver simultaneously).
 */
sealed class AppState {

    /**
     * ES-DE is currently closed or the Companion has just launched
     * and is waiting for an initial connection.
     *
     * Companion shows:
     * - Waiting custom image (if provided by the user)
     * - Solid black screen (default fallback)
     * - No widgets
     */
    object WaitingForESDE : AppState()

    /**
     * ES-DE is currently booting up (has sent the startup event) but hasn't 
     * reached the system selection menu yet.
     *
     * Companion shows:
     * - Startup custom image (if provided by the user)
     * - Falls back to pre-startup behavior if no startup image is set
     * - No widgets
     */
    object ESDEStarting : AppState()

    /**
     * User is browsing system selection in ES-DE.
     *
     * Companion shows:
     * - System logo
     * - Random game artwork from system
     * - System widgets (logo only)
     *
     * @param systemName The ES-DE system name (e.g., "snes", "genesis")
     */
    data class SystemBrowsing(
        val systemName: String
    ) : AppState()

    /**
     * User is browsing games within a system.
     *
     * Companion shows:
     * - Game background (fanart/screenshot)
     * - Game widgets (marquee, box art, description, etc.)
     * - Optional video after delay
     *
     * @param systemName The ES-DE system name
     * @param gameFilename Full path from ES-DE (may include subfolders)
     * @param gameName Display name from ES-DE (optional)
     */
    data class GameBrowsing(
        val systemName: String,
        val gameFilename: String,
        val gameName: String?
    ) : AppState()

    /**
     * A game is currently running on the other screen.
     *
     * Companion shows (based on settings):
     * - Black screen
     * - Game artwork with marquee
     * - Default/custom background
     *
     * @param systemName The system the game belongs to
     * @param gameFilename The game file path
     */
    data class GamePlaying(
        val systemName: String,
        val gameFilename: String
    ) : AppState()

    /**
     * ES-DE screensaver is active.
     *
     * Companion shows (based on settings):
     * - Black screen
     * - Current screensaver game artwork
     * - Default/custom background
     *
     * @param currentGame The game currently displayed in screensaver (changes frequently in slideshow mode)
     * @param previousState Where we were before screensaver started (for returning)
     */
    data class Screensaver(
        val currentGame: ScreensaverGame?,
        val previousState: SavedBrowsingState
    ) : AppState()
}

/**
 * Represents a game being shown in the screensaver.
 */
data class ScreensaverGame(
    val gameFilename: String,
    val gameName: String?,
    val systemName: String
)

/**
 * Saves the browsing state before entering screensaver,
 * so we can return to the exact same place.
 */
sealed class SavedBrowsingState {
    /**
     * User was browsing system selection.
     */
    data class InSystemView(
        val systemName: String
    ) : SavedBrowsingState()

    /**
     * User was browsing games in a system.
     */
    data class InGameView(
        val systemName: String,
        val gameFilename: String,
        val gameName: String?
    ) : SavedBrowsingState()
}

fun AppState.getCurrentSystemName(): String? {
    return when (this) {
        is AppState.WaitingForESDE -> null
        is AppState.ESDEStarting -> null
        is AppState.SystemBrowsing -> systemName
        is AppState.GameBrowsing -> systemName
        is AppState.GamePlaying -> systemName
        is AppState.Screensaver -> currentGame?.systemName
    }
}

fun AppState.getCurrentGameFilename(): String? {
    return when (this) {
        is AppState.WaitingForESDE -> null
        is AppState.ESDEStarting -> null
        is AppState.GameBrowsing -> gameFilename
        is AppState.GamePlaying -> gameFilename
        is AppState.Screensaver -> currentGame?.gameFilename
        else -> null
    }
}