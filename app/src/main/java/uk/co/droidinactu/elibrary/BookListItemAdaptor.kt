package uk.co.droidinactu.elibrary

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import uk.co.droidinactu.ebooklib.room.EBook

/**
 * An adapter for displaying a list of [uk.co.droidinactu.ebooklib.room.EBook] objects.
 * Created by aspela on 01/09/16.
 */
class BookListItemAdaptor(
    private val ctx: Context,
    private val mBooks: MutableList<EBook>,
    private val openBookHandler: Handler
) :
    RecyclerView.Adapter<BookListItemAdaptor.ViewHolder>() {

    private val NOTIFY_DELAY = 500

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(ctx)
            .inflate(R.layout.book_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val book = mBooks[position]

        var cvrBmp = book.coverImageAsBitmap
        if (cvrBmp == null) {
            cvrBmp = BitmapFactory.decodeResource(ctx.resources, R.drawable.generic_book_cover)
        }
        viewHolder.ebk = book
        viewHolder.mTitle.text = book.bookTitle
        viewHolder.mCover.setImageBitmap(cvrBmp)
//        if (book.filetypes.size == 1) {
//            viewHolder.mCover.showBadge(true)
//            viewHolder.mCover.setBadgeText(book.filetypes.first().toString())
//        } else {
//            viewHolder.mCover.showBadge(true)
//            viewHolder.mCover.setBadgeText(FileType.EPUB.toString() + "/" + FileType.PDF)
//        }
    }

    override fun getItemCount(): Int {
        return mBooks.size
    }

    fun addBook(book: EBook, position: Int) {
        val handler = Handler()
        handler.postDelayed({
            mBooks.add(position, book)
            notifyItemInserted(position)
        }, NOTIFY_DELAY.toLong())
    }

    // region List manipulation methods

    fun removeBook(position: Int) {
        mBooks.removeAt(position)

        // notify of the removal with a delay so there is a brief pause after returning
        // from the book details screen; this makes the animation more noticeable
        val handler = Handler()
        handler.postDelayed({ notifyItemRemoved(position) }, NOTIFY_DELAY.toLong())
    }

    @TargetApi(11)
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener,
        PopupMenu.OnMenuItemClickListener {
        private val mOverflowIcon: ImageView
        internal var mTitle: TextView = view.findViewById<View>(R.id.book_list_item_title) as TextView
        internal var mCover: ImageView = view.findViewById<View>(R.id.book_list_item_cover) as ImageView
        internal var ebk: EBook? = null
        internal var ctx: Context? = null

        init {
            ctx = this@BookListItemAdaptor.ctx
            view.setOnClickListener(this)
            mOverflowIcon = view.findViewById<View>(R.id.book_list_item_context_menu) as ImageView
            mOverflowIcon.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (view === mOverflowIcon) {
                val popup = PopupMenu(ctx, view)
                popup.inflate(R.menu.menu_book_item)
                popup.setOnMenuItemClickListener(this)
                popup.show()
            } else {
                val completeMessage = openBookHandler.obtainMessage(61, "openbook:" + ebk!!.fullFileDirName)
                completeMessage.sendToTarget()
            }
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            Toast.makeText(ctx, "DO SOME STUFF HERE\n" + ebk!!.bookTitle, Toast.LENGTH_LONG).show()

            when (item.itemId) {
                R.id.action_book_show_details -> {
                    val b = Bundle()
                    b.putString("book_full_file_dir_name", ebk!!.fullFileDirName) //Your id

                    val i = Intent(ctx, BookLibBookDetailsActivity::class.java)
                    i.putExtras(b) //Put your id to your next Intent
                    ctx!!.startActivity(i)
                }
                R.id.action_book_add_tag -> {
                }
                R.id.action_book_delete -> {
                }
            }
            return true
        }
    }

}
