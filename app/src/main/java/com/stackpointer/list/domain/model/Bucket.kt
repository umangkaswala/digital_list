package com.stackpointer.list.domain.model

/** One labelled group of items within a saved view, e.g. Today's "Past" / "Soon" / "Completed". */
data class Bucket(
    val label: BucketLabel,
    val items: List<Item>,
)

/**
 * The bucket headers a saved view can produce. Not every view uses every label — see
 * [com.stackpointer.list.domain.usecase.BucketItems] for which labels each view emits.
 */
enum class BucketLabel {
    PAST,
    TODAY,
    SOON,
    NEXT_7_DAYS,
    LATER,
    EARLIER_THIS_WEEK,
    OLDER,
    COMPLETED,
    UNBUCKETED,
}
