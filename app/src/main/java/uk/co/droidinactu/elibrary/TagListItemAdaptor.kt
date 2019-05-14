package uk.co.droidinactu.elibrary

import android.annotation.TargetApi
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * An adapter for displaying a list of [uk.co.droidinactu.ebooklib.room.EBook] objects.
 * Created by aspela on 01/09/16.
 */
class TagListItemAdaptor(private val mTags: MutableList<String>, val tagSelectedHandler: Handler) :
    RecyclerView.Adapter<TagListItemAdaptor.ViewHolder>() {

    private val NOTIFY_DELAY = 500

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.tag_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val tag = mTags[position]
        viewHolder.tag = tag
        viewHolder.mTitle.text = tag
    }

    override fun getItemCount(): Int {
        return mTags.size
    }

    // region List manipulation methods

    @TargetApi(11)
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        internal var mTitle: TextView = view.findViewById<View>(R.id.tag_list_item_title) as TextView
        internal var tag: String = ""

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            var textView = ((view as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0) as TextView
            val newTagSelectedMessage = tagSelectedHandler!!.obtainMessage(64, textView.text)
            newTagSelectedMessage.sendToTarget()
        }

        fun findTagTextView(): TextView? {
            return null
        }
    }
}
