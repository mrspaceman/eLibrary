package uk.co.droidinactu.elibrary.library

import android.app.Dialog
import uk.co.droidinactu.elibrary.room.Tag
import java.util.*

class TagTree {

    private var tagMap: MutableMap<Int, TreeTag> = HashMap()
    var rootTags: MutableList<TreeTag> = ArrayList()

    fun add(t: Tag) {
        var newT: TreeTag?
        if (tagMap.containsKey(t.getUniqueId())) {
            newT = tagMap[t.getUniqueId()]
        } else {
            newT = TreeTag()
            newT.me = t
            tagMap[t.getUniqueId()] = newT
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
