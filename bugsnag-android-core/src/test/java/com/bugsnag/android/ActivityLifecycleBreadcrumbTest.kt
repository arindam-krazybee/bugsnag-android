package com.bugsnag.android

import android.app.Activity
import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class ActivityLifecycleBreadcrumbTest {

    private lateinit var tracker: ActivityBreadcrumbCollector

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var bundle: Bundle

    var resultActivity: String? = null
    var resultMetadata: Map<String, Any>? = null

    @Before
    fun setUp() {
        resultActivity = null
        resultMetadata = null
        tracker = ActivityBreadcrumbCollector { activity, data ->
            resultActivity = activity
            resultMetadata = data
        }
    }

    @Test
    fun onCreateBreadcrumbNoBundle() {
        tracker.onActivityCreated(activity, null)
        assertEquals("Activity", resultActivity)
        assertEquals("onCreate()", resultMetadata!!["to"])
        assertFalse(resultMetadata!!["hasBundle"] as Boolean)
    }

    @Test
    fun onCreateBreadcrumbBundle() {
        tracker.onActivityCreated(activity, bundle)
        assertEquals("Activity", resultActivity)
        assertEquals("onCreate()", resultMetadata!!["to"])
        assertTrue(resultMetadata!!["hasBundle"] as Boolean)
    }

    @Test
    fun onStartBreadcrumb() {
        tracker.onActivityStarted(activity)
        assertEquals("Activity", resultActivity)
        assertEquals("onStart()", resultMetadata!!["to"])
        assertNull(resultMetadata!!["hasBundle"])
    }

    @Test
    fun onResumeBreadcrumb() {
        tracker.onActivityResumed(activity)
        assertEquals("Activity", resultActivity)
        assertEquals("onResume()", resultMetadata!!["to"])
        assertNull(resultMetadata!!["hasBundle"])
    }

    @Test
    fun onPauseBreadcrumb() {
        tracker.onActivityPaused(activity)
        assertEquals("Activity", resultActivity)
        assertEquals("onPause()", resultMetadata!!["to"])
        assertNull(resultMetadata!!["hasBundle"])
    }

    @Test
    fun onStopBreadcrumb() {
        tracker.onActivityStopped(activity)
        assertEquals("Activity", resultActivity)
        assertEquals("onStop()", resultMetadata!!["to"])
        assertNull(resultMetadata!!["hasBundle"])
    }

    @Test
    fun onSaveInstanceState() {
        tracker.onActivitySaveInstanceState(activity, bundle)
        assertEquals("Activity", resultActivity)
        assertEquals("onSaveInstanceState()", resultMetadata!!["to"])
        assertNull(resultMetadata!!["hasBundle"])

    }

    @Test
    fun onDestroyBreadcrumb() {
        tracker.onActivityDestroyed(activity)
        assertEquals("Activity", resultActivity)
        assertEquals("onDestroy()", resultMetadata!!["to"])
        assertNull(resultMetadata!!["hasBundle"])
    }


    @Test
    fun prevStateTesat() {
        tracker.onActivityCreated(activity, null)
        assertEquals("Activity", resultActivity)
        assertEquals("onCreate()", resultMetadata!!["to"])
        assertNull(resultMetadata!!["from"])

        tracker.onActivityStarted(activity)
        assertEquals("onStart()", resultMetadata!!["to"])
        assertEquals("onCreate()", resultMetadata!!["from"])
    }
}
