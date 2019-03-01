package uk.co.droidinactu.elibrary

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import uk.co.droidinactu.elibrary.badgedimageview.BadgedImageView
import uk.co.droidinactu.elibrary.library.LibraryManager
import uk.co.droidinactu.elibrary.room.EBook
import uk.co.droidinactu.elibrary.room.FileType


/**
 * An adapter for displaying a list of [uk.co.droidinactu.elibrary.room.EBook] objects.
 * Created by aspela on 01/09/16.
 */
class BookListItemAdaptor(private val mBooks: MutableList<EBook>) :
    RecyclerView.Adapter<BookListItemAdaptor.ViewHolder>() {

    private val NOTIFY_DELAY = 500
    private var libMgr: LibraryManager? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.book_list_item, parent, false)
        libMgr = (parent.context.applicationContext as BookLibApplication).getLibManager()
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val book = mBooks[position]

        var cvrBmp = book.coverImageAsBitmap
        if (cvrBmp == null) {
            cvrBmp = BitmapFactory.decodeResource(viewHolder.mCover.context.resources, R.drawable.generic_book_cover)
        }
        viewHolder.ebk = book
        val tmpStr = book.bookTitle
        viewHolder.mTitle.text = tmpStr
        viewHolder.mCover.setImageBitmap(cvrBmp)
        val ftypes = libMgr!!.getFileTypesForBook(book)
        if (ftypes.size == 1) {
            viewHolder.mCover.showBadge(true)
            viewHolder.mCover.setBadgeText(ftypes.get(0).toString())
        } else {
            viewHolder.mCover.showBadge(true)
            viewHolder.mCover.setBadgeText(FileType.EPUB.toString() + "/" + FileType.PDF)
        }
        viewHolder.mTitle.text = tmpStr
    }

    override fun getItemCount(): Int {
        return mBooks.size
    }

    //    @Override
    //    public long getItemId(int position) {
    //        return mBooks.get(position).getFull_file_dir_name();
    //    }

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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener,
        PopupMenu.OnMenuItemClickListener {
        private val mOverflowIcon: ImageView
        internal var mTitle: TextView
        internal var mCover: BadgedImageView
        internal var ebk: EBook? = null

        init {
            mTitle = view.findViewById<View>(R.id.book_list_item_title) as TextView
            mCover = view.findViewById<View>(R.id.book_list_item_cover) as BadgedImageView
            view.setOnClickListener(this)
            mOverflowIcon = view.findViewById<View>(R.id.book_list_item_context_menu) as ImageView
            mOverflowIcon.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (view === mOverflowIcon) {
                val popup = PopupMenu(view.getContext(), view)
                popup.inflate(R.menu.menu_book_item)
                popup.setOnMenuItemClickListener(this)
                popup.show()
            } else {
                val ftypes =
                    (view.context.applicationContext as BookLibApplication).getLibManager()!!.getFileTypesForBook(ebk)
                if (ftypes.size > 1) {
                    showFileTypePickerDialog(view.context)
                } else {
                    openBook(view.context, ftypes.get(0).toString().toLowerCase())
                }
            }
        }

        private fun showFileTypePickerDialog(ctx: Context) {
            val dialog = Dialog(ctx)
            dialog.setContentView(R.layout.filetype_picker_dialog)
            dialog.setTitle("Pick an EBook Type to Open")

            val radioGroup = dialog.findViewById<View>(R.id.filetype_picker_dialog_group) as RadioGroup
            val filetypePickerButton = dialog.findViewById<View>(R.id.filetype_picker_dialog_btn) as Button
            filetypePickerButton.setOnClickListener {
                val selectedId = radioGroup.checkedRadioButtonId
                val btnSelctd = dialog.findViewById<View>(selectedId) as RadioButton
                val selectedFileType = btnSelctd.text.toString().toLowerCase()
                openBook(ctx, selectedFileType)
                dialog.hide()
            }
            dialog.show()
        }

        private fun openBook(ctx: Context, selectedFileType: String) {
            (ctx.applicationContext as BookLibApplication).getLibManager()!!.addTagToBook(
                BookTag.CURRENTLY_READING,
                ebk
            )
            val i = (ctx.applicationContext as BookLibApplication).getLibManager()!!.getOpenIntentForBook(
                ebk!!,
                selectedFileType
            )
            if (i != null) {
                ctx.startActivity(i)
            }
        }


        override fun onMenuItemClick(item: MenuItem): Boolean {
            Toast.makeText(mOverflowIcon.context, "DO SOME STUFF HERE\n" + ebk!!.bookTitle, Toast.LENGTH_LONG).show()

            when (item.itemId) {
                R.id.action_book_show_details -> {
                    val i = Intent(mCover.context, BookLibBookDetailsActivity::class.java)
                    val b = Bundle()
                    b.putString("book_full_file_dir_name", ebk!!.fullFileDirName) //Your id
                    i.putExtras(b) //Put your id to your next Intent
                    mCover.context.startActivity(i)
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
