package com.stackpointer.list.ui.screens.capture

/** Which of the capture sheet's five mode panels is showing (screens 17-23). Independent of
 * whether the draft actually has that kind of data set — switching away from Checklist mode
 * doesn't clear the sub-items the user already typed. */
enum class CaptureMode { NONE, TIME, PLACE, CHECKLIST, LABEL }

/** What the capture bar prefills into a fresh draft, per README.md's "Prefill by context" —
 * depends on which saved view opened it. */
enum class CapturePrefill { NONE, TODAY, SCHEDULED, PLACE }
