package com.bugsnag.android.mazerunner.scenarios

import android.app.Activity
import android.content.Context
import com.bugsnag.android.Configuration

/**
 * Sends an unhandled exception which is cached on disk to Bugsnag, then sent on a separate launch.
 */
internal class ReportCacheScenario(config: Configuration,
                                   context: Context) : Scenario(config, context) {
    init {
        config.setAutoCaptureSessions(false)
        if (context is Activity) {
            if (eventMetaData != "online") {
                disableAllDelivery(config)
            }
        }
    }

    override fun run() {
        super.run()
        if (eventMetaData != "online") {
            throw RuntimeException("ReportCacheScenario")
        }
    }

}
