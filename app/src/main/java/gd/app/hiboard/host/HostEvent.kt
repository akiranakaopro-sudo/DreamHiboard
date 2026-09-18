package gd.app.hiboard.host

/**
 * Lifecycle the launcher (or [gd.app.hiboard.HiboardActivity]) drives.
 * Mirrors ColorOS `IAssistantScreenCtrl`: create / enter / exit / pause / resume / destroy.
 */
enum class HostEvent {
    Create,
    Enter,
    Exit,
    Pause,
    Resume,
    Destroy,
}
