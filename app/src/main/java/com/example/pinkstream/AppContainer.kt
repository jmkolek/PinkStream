package com.example.pinkstream

import android.content.Context

/**
 * Holds the app-wide objects (the Streamer.bot client, repositories) so screens can
 * share one instance of each. This is our hand-written replacement for a DI framework.
 * Empty for now; milestones 1 and 2 add the client and the settings repository.
 */
class AppContainer(val appContext: Context)
