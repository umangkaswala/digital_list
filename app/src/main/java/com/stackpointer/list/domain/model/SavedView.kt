package com.stackpointer.list.domain.model

/** The seven saved views from DATA_MODEL.md — each backed by a repository query and a
 * bucketing rule (see [com.stackpointer.list.domain.usecase.BucketItems]). */
enum class SavedView { TODAY, SCHEDULED, STARRED, PLACE, NO_ALERT, COMPLETED, RECYCLE_BIN }
