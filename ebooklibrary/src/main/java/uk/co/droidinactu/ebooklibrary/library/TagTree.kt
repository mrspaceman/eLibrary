package uk.co.droidinactu.ebooklibrary.library

import android.app.Dialog
import uk.co.droidinactu.ebooklibrary.room.Tag
import java.util.*

class TagTree {

    private var tagMap: MutableMap<Long, TreeTag> = HashMap()
    var rootTags: MutableList<TreeTag> = ArrayList()

    fun add(t: Tag) {
        var newT: TreeTag? = null
        if (tagMap.containsKey(t.id)) {
            newT = tagMap[t.id]
        } else {
            newT = TreeTag()
            newT.me = t
            tagMap[t.id] = newT
        }

        if (newT!!.me!!.parentTagId == null) {
            rootTags.add(newT)
        } else {
            val pt = tagMap[newT.me!!.parentTagId]
            pt!!.children.add(newT)
        }
    }

    fun remove(currentlyReading: String) {}

    class TreeTag {
        var me: Tag? = null
        var children: ArrayList<TreeTag> = ArrayList()
        var dialog: Dialog? = null
    }

}
