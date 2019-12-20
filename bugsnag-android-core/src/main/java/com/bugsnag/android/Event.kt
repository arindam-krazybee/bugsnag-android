package com.bugsnag.android

import java.io.IOException

/**
 * Information and associated diagnostics relating to a handled or unhandled
 * Exception.
 *
 * This object is made available in OnErrorCallback callbacks, so you can
 * inspect and modify it before it is delivered to Bugsnag.
 *
 * @see OnErrorCallback
 */
class Event @JvmOverloads internal constructor(
    val originalError: Throwable? = null,
    config: ImmutableConfig,
    private var handledState: HandledState,
    data: Metadata = Metadata()
) : JsonStream.Streamable, MetadataAware, UserAware {

    internal val metadata: Metadata = data.copy()
    private val ignoreClasses: Set<String> = config.ignoreClasses.toSet()

    var session: Session? = null

    /**
     * Set the Severity of this Event.
     *
     * By default, unhandled exceptions will be Severity.ERROR and handled
     * exceptions sent with bugsnag.notify will be Severity.WARNING.
     * @see Severity
     */
    var severity: Severity
        get() = handledState.currentSeverity
        set(value) {
            handledState.currentSeverity = value
        }

    var apiKey: String = config.apiKey

    lateinit var app: AppWithState
    lateinit var device: DeviceWithState

    val isUnhandled: Boolean = handledState.isUnhandled

    var breadcrumbs: List<Breadcrumb> = emptyList()

    var errors: List<Error> = when (originalError) {
        null -> listOf()
        else -> Error.createError(originalError, config.projectPackages, config.logger)
    }

    var threads: List<Thread> = when {
        config.sendThreads -> ThreadState(config, if (isUnhandled) originalError else null).threads
        else -> emptyList()
    }

    /**
     * Set a custom grouping hash to use when grouping this Event on the
     * Bugsnag dashboard. By default, we use a combination of error class
     * and top-most stacktrace line to calculate this, and we do not recommend
     * you override this.
     */
    var groupingHash: String? = null

    /**
     * Override the context sent to Bugsnag with this Event. By default we'll
     * attempt to detect the name of the top-most Activity when this error
     * occurred, and use this as the context, but sometimes this is not
     * possible.
     */
    var context: String? = null

    /**
     * @return user information associated with this Event
     */
    internal var _user = User(null, null, null)

    protected fun shouldIgnoreClass(): Boolean {
        return when {
            errors.isEmpty() -> true
            else -> errors.any { ignoreClasses.contains(it.errorClass) }
        }
    }

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        // Write error basics
        writer.beginObject()
        writer.name("context").value(context)
        writer.name("metaData").value(metadata)

        writer.name("severity").value(severity)
        writer.name("severityReason").value(handledState)
        writer.name("unhandled").value(handledState.isUnhandled)

        // Write exception info
        writer.name("exceptions")
        writer.beginArray()
        errors.forEach { writer.value(it) }
        writer.endArray()

        // Write user info
        writer.name("user").value(_user)

        // Write diagnostics
        writer.name("app").value(app)
        writer.name("device").value(device)
        writer.name("breadcrumbs").value(breadcrumbs)
        writer.name("groupingHash").value(groupingHash)

        writer.name("threads")
        writer.beginArray()
        threads.forEach { writer.value(it) }
        writer.endArray()

        if (session != null) {
            val copy = Session.copySession(session)
            writer.name("session").beginObject()
            writer.name("id").value(copy.id)
            writer.name("startedAt").value(DateUtils.toIso8601(copy.startedAt))

            writer.name("events").beginObject()
            writer.name("handled").value(copy.handledCount.toLong())
            writer.name("unhandled").value(copy.unhandledCount.toLong())
            writer.endObject()
            writer.endObject()
        }

        writer.endObject()
    }

    protected fun updateSeverityInternal(severity: Severity) {
        handledState = HandledState.newInstance(handledState.severityReasonType,
            severity, handledState.attributeValue)
        this.severity = severity
    }

    /**
     * Set user information associated with this Event
     *
     * @param id    the id of the user
     * @param email the email address of the user
     * @param name  the name of the user
     */
    override fun setUser(id: String?, email: String?, name: String?) {
        _user = User(id, email, name)
    }

    override fun getUser() = _user

    override fun addMetadata(section: String, value: Map<String, Any?>) = metadata.addMetadata(section, value)
    override fun addMetadata(section: String, key: String, value: Any?) =
        metadata.addMetadata(section, key, value)

    override fun clearMetadata(section: String) = metadata.clearMetadata(section)
    override fun clearMetadata(section: String, key: String) = metadata.clearMetadata(section, key)

    override fun getMetadata(section: String) = metadata.getMetadata(section)
    override fun getMetadata(section: String, key: String) = metadata.getMetadata(section, key)
}
