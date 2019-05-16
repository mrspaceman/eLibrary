package uk.co.droidinactu.elibrary

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import uk.co.droidinactu.ebooklib.room.EBook


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BookDetailsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BookDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class BookDetailsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var mFilepath: TextView
    private lateinit var dateAddedToLib: TextView
    private lateinit var dateLibEntryMod: TextView
    private lateinit var mCover: ImageView
    private lateinit var mTitle: EditText
    private lateinit var mFilename: EditText
    private lateinit var mAuthor: EditText
    private lateinit var mTagList: ListView
    private lateinit var bookFullFileDirName: String

    private var mFrgAct: FragmentActivity? = null
    private lateinit var mIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_details, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCover = view.findViewById(R.id.book_details_cover)
        mTitle = view.findViewById(R.id.book_details_title)
        mFilename = view.findViewById(R.id.book_details_filename)
        mAuthor = view.findViewById(R.id.book_details_author)
        mTagList = view.findViewById(R.id.book_details_tags)
        mFilepath = view.findViewById(R.id.book_details_filepath)

        dateAddedToLib = view.findViewById(R.id.book_details_added_to_lib)
        dateLibEntryMod = view.findViewById(R.id.book_details_lib_entry_modified)

        mTitle.isEnabled = false
        mAuthor.isEnabled = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mFrgAct = getActivity()
        mIntent = mFrgAct!!.getIntent()
        val b = mIntent.extras
        if (b != null) {
            bookFullFileDirName = b.getString("book_full_file_dir_name")
        }
        updateBookDetails()
    }

    private fun updateBookDetails() {
        val libMgr = BookLibApplication.instance.getLibManager()
        doAsync {
            val ebk = libMgr.getBook(bookFullFileDirName)
            uiThread {
                updateBookDetails(ebk)
            }
        }
    }

    private fun updateBookDetails(ebk: EBook) {
        var cvrBmp = ebk.coverImageAsBitmap
        if (cvrBmp == null) {
            cvrBmp = BitmapFactory.decodeResource(resources, R.drawable.generic_book_cover)
        }

        var someDate = DateTime(ebk.addedToLibrary)
        dateAddedToLib.text = someDate.toString(BookLibApplication.sdf)

        someDate = DateTime(ebk.lastRefreshed)
        dateLibEntryMod.text = someDate.toString(BookLibApplication.sdf)

        mTitle.setText(ebk.bookTitle)
        doAsync {
            val auths = BookLibApplication.instance.getLibManager().getAuthorsForBook(ebk)
            uiThread {
                if (auths.isNotEmpty()) {
                    mAuthor.setText(auths[0].fullName)
                }

                val tagStrs = mutableListOf<String>()
                for (t in ebk.tags) {
                    tagStrs.add(t)
                }
                val tagListAdaptor = ArrayAdapter(
                    BookLibApplication.instance.applicationContext,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    tagStrs
                )
                mTagList.adapter = tagListAdaptor
            }
        }

        mFilepath.setText(ebk.fileDir)
        mFilename.setText(ebk.fileName)
        mCover.setImageBitmap(cvrBmp)
        val ftypes = ebk.filetypes
//        if (ftypes.size == 1) {
//            mCover.showBadge(true)
//            mCover.setBadgeText(ftypes.first().toString())
//        } else {
//            mCover.showBadge(true)
//            mCover.setBadgeText("${FileType.EPUB}/${FileType.PDF}")
//        }
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is BookSearchFragment.on) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        listener = null
//    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BookDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
