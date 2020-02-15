package com.example.takenotes.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.takenotes.R
import com.example.takenotes.databinding.FragmentNoteListBinding
import com.example.takenotes.databinding.NotesListBinding
import com.example.takenotes.modal.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NoteListFragment : Fragment(), INoteGetData, INoteDelete, INoteUpdateData {
    private var dataList: MutableList<Note>? = null
    private val notesAdapter by lazy { NotesAdapter(dataList, ::onAction) }
    private var binding: FragmentNoteListBinding? = null
    private var dialog: AlertDialog? = null
    private var removeNotePosition: Int = -1
    private val sortBottomSheet by lazy { BottomSheetBehavior.from<ConstraintLayout>(activity?.findViewById(R.id.lv_sort_root_layout)) }
    private val filterBottomSheet by lazy { BottomSheetBehavior.from<ConstraintLayout>(activity?.findViewById(R.id.lv_filter_root_layout)) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_note_list, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.notes_list_title)
        Log.d("ListData:- ", dataList.toString())
        dialog = SpotsDialog.Builder().setContext(context).build()
        riseFallSortBottomSheet(false , sortBottomSheet)
        riseFallFilterBottomSheet(false , filterBottomSheet)
        //Log.d("Instance ID", FirebaseInstanceId.getInstance().id)

        //Swipe To Refresh Data:-
        val swipeToRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeToRefresh.setOnRefreshListener {
            swipeToRefresh.isRefreshing = false
            dialog?.show()
            getAllNotes(this)
        }

        if (dataList?.size != 0)
            binding?.recyclerView?.apply {
                layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                adapter = notesAdapter
                addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
            }

        //Add New Note onClick event:-
        binding?.fab?.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_noteListFragment_to_createNotesFragment)
        }

        //API Call to Get all Data:-
        dialog?.show()
        getAllNotes(this@NoteListFragment)

        //Search API Hit:-
        binding?.searchNoteValue?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (!TextUtils.isEmpty(s.toString().trim())) {
                    binding?.recyclerView?.visibility = View.GONE
                    binding?.progressBar?.visibility = View.VISIBLE
                    GlobalScope.launch {
                            getAllSearchNotes(this@NoteListFragment, s.toString())
                    }
                } else {
                    dialog?.show()
                    getAllNotes(this@NoteListFragment)
                }
            }
        })

        //Sort API Hit:-
        binding?.tvSortBy?.setOnClickListener {
           riseFallSortBottomSheet(true , sortBottomSheet)
        }

        //Filter API Hit:-
        binding?.tvFilterBy?.setOnClickListener {
            riseFallFilterBottomSheet(true , filterBottomSheet)
        }

        //Sort Notes A To Z API Hit:-
        view.findViewById<TextView>(R.id.sortAToZ)?.setOnClickListener {
            riseFallSortBottomSheet(false , sortBottomSheet)
            binding?.recyclerView?.visibility = View.GONE
            dialog?.show()
            getAllSortedNotes(this@NoteListFragment, 1)
        }

        //Sort Notes Z To A API Hit:-
        view.findViewById<TextView>(R.id.sortZToA)?.setOnClickListener {
            riseFallSortBottomSheet(false , sortBottomSheet)
            binding?.recyclerView?.visibility = View.GONE
            dialog?.show()
            getAllSortedNotes(this@NoteListFragment, 2)
        }


        //Filter Today Note API Hit:-
        view.findViewById<TextView>(R.id.todayFilter).setOnClickListener {
            val (x , y) = selectedFilterDate("0")
            Log.d("startDate:- " , x)
            Log.d("endDate:- " , y)
            riseFallFilterBottomSheet(false , filterBottomSheet)
            binding?.recyclerView?.visibility = View.VISIBLE
            dialog?.show()
            getAllFilterNotes(this@NoteListFragment , x , y)
        }

        //Filter Yesterday Note API Hit:-
        view.findViewById<TextView>(R.id.yesterdayFilter).setOnClickListener {
            val (x , y) = selectedFilterDate("1")
            Log.d("startDate:- " , x)
            Log.d("endDate:- " , y)
            riseFallFilterBottomSheet(false , filterBottomSheet)
            binding?.recyclerView?.visibility = View.VISIBLE
            dialog?.show()
            getAllFilterNotes(this@NoteListFragment , x , y)
        }

        //Filter Weekly Note API Hit:-
        view.findViewById<TextView>(R.id.weeklyFilter).setOnClickListener {
            val (x , y) = selectedFilterDate("2")
            Log.d("startDate:- " , x)
            Log.d("endDate:- " , y)
            riseFallFilterBottomSheet(false , filterBottomSheet)
            binding?.recyclerView?.visibility = View.VISIBLE
            dialog?.show()
            getAllFilterNotes(this@NoteListFragment , x , y)
        }

        //Filter Monthly Note API Hit:-
        view.findViewById<TextView>(R.id.monthlyFilter).setOnClickListener {
            val (x , y) = selectedFilterDate("3")
            Log.d("startDate:- " , x)
            Log.d("endDate:- " , y)
            riseFallFilterBottomSheet(false , filterBottomSheet)
            binding?.recyclerView?.visibility = View.VISIBLE
            dialog?.show()
            getAllFilterNotes(this@NoteListFragment , x ,y)
        }
    }


    override fun onNotesGet(resp: MutableList<Note>?) {
        if (resp != null && resp.size > 0) {
            dataList = resp
            binding?.recyclerView?.apply {
                layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                adapter = notesAdapter
                addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
                notesAdapter.notifyDataSetChanged()
                binding?.recyclerView?.visibility = View.VISIBLE
            }
                binding?.progressBar?.visibility = View.GONE
                binding?.filterLayoutView?.visibility = View.VISIBLE
                binding?.tilNote?.visibility = View.VISIBLE
                //showSnackbar(activity!! , binding?.coordinatorLayout!!, getString(R.string.welcome_back) , R.color.colorGreen , 1)
            } else {
                binding?.filterLayoutView?.visibility = View.GONE
                binding?.tilNote?.visibility = View.GONE
                binding?.recyclerView?.visibility = View.GONE
                showSnackbar(
                    activity!!,
                    binding?.coordinatorLayout!!,
                    getString(R.string.notes_hint),
                    R.color.colorRed,
                    0
                )
            }
            dialog?.dismiss()
        }

        //Method to get Callback from Adapter and Decide Delete or Update based on Action:-
        private fun onAction(position: Int, action: Int) {
            if (position > -1) {
                when (action) {
                    101 -> {
                        dialog?.show()
                        removeNotePosition = position
                        deleteAllNotes(dataList?.get(position)?.noteId!!, this)
                    }

                    102 -> showUpdateNoteDialog(position)
                }

            } else
                Log.d("NoteListPosition:- ", position.toString())
        }

        override fun onNoteDelete(resp: ResponseData) {
            if (resp.responseCode == "200") {
                    Log.d("Delete Success:- ", resp.responseMessage!!)
                    if (removeNotePosition != -1) {
                        dataList?.removeAt(removeNotePosition)
                        notesAdapter.notifyItemRemoved(removeNotePosition)
                        notesAdapter.refreshNotesData(dataList)
                        if (dataList?.size == 0) {
                            binding?.filterLayoutView?.visibility = View.GONE
                            binding?.tilNote?.visibility = View.GONE
                            binding?.recyclerView?.visibility = View.GONE
                        }
                        dialog?.dismiss()
                        showSnackbar(activity!!, binding?.coordinatorLayout!!, resp.responseMessage!!, R.color.colorGreen, 1)
                    } else {
                        dialog?.dismiss()
                        showSnackbar(activity!!, binding?.coordinatorLayout!!, resp.responseMessage!!, R.color.colorRed, 0)
                        Log.d("NoteListPosition:- ", removeNotePosition.toString())
                    }
            }
        }

        //Below Method is to show Dialog box to update existing Note:-
        private fun showUpdateNoteDialog(notePosition: Int) {
            activity?.let {
                Dialog(it).apply {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    setContentView(R.layout.edit_note_data_layout)
                    setCancelable(false)

                    val noteET = findViewById<TextInputEditText>(R.id.enter_note_value)
                    val updateBTN = findViewById<Button>(R.id.updateButton)
                    val cancelBTN = findViewById<Button>(R.id.cancelButton)
                    noteET.setText(dataList?.get(notePosition)?.noteData?.replace("\"", ""))

                    cancelBTN.setOnClickListener {
                        dismiss()
                    }

                    updateBTN.setOnClickListener {
                        if (!TextUtils.isEmpty(noteET.text.toString())) {
                            dismiss()
                            dialog?.show()
                            updateNote(
                                dataList?.get(notePosition)?.noteId!!,
                                noteET.text.toString(),
                                this@NoteListFragment
                            )
                        } else
                            Log.d("ValidationError:- ", "Note Data required !!!!")
                    }

                }
            }?.show()
        }

        override fun onNoteUpdate(resp: MutableList<Note>?) {
            if (resp != null) {
                dataList = resp
                notesAdapter.refreshNotesData(dataList)
                dialog?.dismiss()
                showSnackbar(activity!!, binding?.coordinatorLayout!!, getString(R.string.notes_update_success), R.color.colorGreen, 1)
            } else {
                dialog?.dismiss()
                showSnackbar(activity!!, binding?.coordinatorLayout!!, getString(R.string.notes_update_failed), R.color.colorRed, 0)
            }
        }

    }

    private class NotesAdapter(var dataList: MutableList<Note>?, private val callback: (Int, Int) -> Unit)
        : RecyclerView.Adapter<NotesAdapter.MyViewHolder>() {

        private var binding : NotesListBinding?=null

        inner class MyViewHolder(notesListBinding: NotesListBinding) : RecyclerView.ViewHolder(notesListBinding.root) {
            init {
                notesListBinding.deleteButton.setOnClickListener {
                    callback(adapterPosition, 101)
                }

                notesListBinding.editButton.setOnClickListener {
                    callback(adapterPosition, 102)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.notes_list, parent, false)
            return MyViewHolder(binding!!)
        }

        override fun getItemCount(): Int {
            return dataList!!.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            binding?.notesText?.text = dataList?.get(position)?.noteData?.replace("\"", "")
            binding?.notesDate?.text = dataList?.get(position)?.noteTime?.replace("\"", "")

        }

        //Refresh Notes Data:-
        fun refreshNotesData(notesList: MutableList<Note>?) {
            this.dataList = notesList
            notifyDataSetChanged()
        }
    }
