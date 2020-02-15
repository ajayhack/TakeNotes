package com.example.takenotes.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.takenotes.R
import com.example.takenotes.databinding.FragmentCreateNotesBinding
import com.example.takenotes.modal.INoteSave
import com.example.takenotes.modal.ResponseData
import com.example.takenotes.modal.submitNotes
import com.google.firebase.iid.FirebaseInstanceId
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.text.SimpleDateFormat
import java.util.*


class CreateNotesFragment : Fragment() , INoteSave {

    private var binding: FragmentCreateNotesBinding? = null
    private var dateFormat = SimpleDateFormat("yyy-MM-dd", Locale.getDefault())
    private var calendar : Calendar = Calendar.getInstance()
    private lateinit var partImage : MultipartBody.Part
    private var uniqueID : String? = null
    private var dialog: AlertDialog? = null
    private var notificationToken : String? = null
    private var senderId : String? = "698666145515"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater , R.layout.fragment_create_notes, container , false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.create_note_title)

        GlobalScope.launch(Dispatchers.IO) {
            notificationToken = FirebaseInstanceId.getInstance().getToken(senderId!! , "FCM")
        }


        //Save Note to DataBase on Click of Action Done:-
        binding?.noteEditText?.setOnEditorActionListener { _, actionID, _ ->
            if(!TextUtils.isEmpty(binding?.noteEditText.toString().trim()) && actionID == EditorInfo.IME_ACTION_DONE){
                saveNotes()
                true
            }else {
                binding?.noteEditText?.error = getString(R.string.notes_field_must_not_be_empty)
                binding?.noteEditText?.requestFocus()
                false
            }
        }
    }

    //Method to Generate UniqueID for Image Saving:-
    private fun generateUniqueID(): String {
        calendar = Calendar.getInstance()
        uniqueID = dateFormat.format(calendar.time)
        return uniqueID!!
    }

    //Below Method is used to save Notes Data into DataBase:-
    private fun saveNotes(){
        dialog = SpotsDialog.Builder().setContext(context).build()
        if (notificationToken != null) {
            view?.clearFocus()
            submitNotes(binding?.noteEditText?.text.toString().trim() ,generateUniqueID() , notificationToken.toString(),  /*partImage ,*/ this)
        }
        //startActivityForResult(galleryIntent(), 125)
    }

    //Method to fetch Gallery Image:-
    private fun galleryIntent(): Intent {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        return galleryIntent
    }


    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && data != null){
            when(requestCode){
                125 ->{
                    val imageURI = data.data as Uri
                    val imageFile = File(FileUtils.getPath(activity , imageURI))
                    val imageBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imageFile.path), 1024, 768, false)
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, FileOutputStream(imageFile))
                    partImage = MultipartBody.Part.createFormData("noteImage", imageFile.absolutePath,
                        RequestBody.create(MediaType.parse("multipart/form-data"), imageFile))
                    dialog?.show()

                    if (notificationToken != null) {
                        submitNotes(binding?.noteEditText?.text.toString().trim() ,generateUniqueID() , notificationToken.toString(),  *//*partImage ,*//* this)
                    }
                }
            }
        }
    }*/

    override fun onNotesSave(resp: ResponseData) {
        if(resp.responseCode == "200"){
            dialog?.dismiss()
            Log.d("SaveSuccess:- " , resp.responseMessage!!)
            view?.let { Navigation.findNavController(it).navigate(R.id.action_createNotesFragment_to_noteListFragment) }
        }
    }
}
