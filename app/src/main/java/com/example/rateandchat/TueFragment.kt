package com.example.rateandchat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TueFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TueFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val tuesdayProgramsList = mutableListOf<FragmentDataClass>()
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FragmentAdapter

    private var param1: String? = null
    private var param2: String? = null

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
        return inflater.inflate(R.layout.fragment_tue, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TueFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TueFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        downloadData()
        val layoutManager = LinearLayoutManager(context)
        recyclerView = view.findViewById(R.id.tuesdayRV)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        adapter = FragmentAdapter(context,tuesdayProgramsList)
        recyclerView.adapter = adapter
    }
    //to download the data into "tuesdayProgramList" list
    private fun downloadData(){
        db = Firebase.firestore
        db.collection("tuesday").get().addOnSuccessListener { documentSnapShot ->
            tuesdayProgramsList.clear()
            for (document in documentSnapShot.documents){
                val program = document.toObject<FragmentDataClass>()
                if (program != null){
                    tuesdayProgramsList.add(program)
                }
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
}