package uk.co.droidinactu.elibrary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BookSearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BookSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class BookSearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var txtSearchText: EditText? = null
    private var btnSearch: Button? = null
    private var cbTags: CheckBox? = null
    private var cbTitle: CheckBox? = null
    private var cbDirs: CheckBox? = null
    private var bkListSearch: RecyclerView? = null
    private var bkListSearchAdaptor: BookListItemAdaptor? = null


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
        return inflater.inflate(R.layout.fragment_book_search, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtSearchText = view.findViewById<View>(R.id.book_lib_search_text) as EditText
        btnSearch = view.findViewById<View>(R.id.book_lib_search_button) as Button
        cbTags = view.findViewById<View>(R.id.book_lib_search_checkBoxTags) as CheckBox
        cbTitle = view.findViewById<View>(R.id.book_lib_search_checkBoxTitle) as CheckBox
        cbDirs = view.findViewById<View>(R.id.book_lib_search_checkBoxDirs) as CheckBox
        bkListSearch = view.findViewById<View>(R.id.book_lib_search_results) as RecyclerView

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val gridLayoutManager = GridLayoutManager(activity, 4)
        bkListSearch?.layoutManager = gridLayoutManager
        bkListSearch?.setHasFixedSize(true)

        cbTags?.isSelected = true
        cbTitle?.isSelected = true
        cbDirs?.isSelected = true

        btnSearch?.setOnClickListener {
            doAsync {
                val bklist = BookLibApplication.instance.getLibManager()
                    .searchBooksMatching(txtSearchText?.text.toString())
                uiThread {
                    bkListSearchAdaptor = BookListItemAdaptor(this@BookSearchFragment.context!!, bklist)
                    bkListSearch?.adapter = bkListSearchAdaptor
                }
            }
        }
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
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
         * @return A new instance of fragment BookSearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookSearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
