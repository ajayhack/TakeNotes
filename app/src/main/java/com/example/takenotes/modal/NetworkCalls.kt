package com.example.takenotes.modal

import android.content.Context
import android.util.Log
import android.view.Gravity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


//const val BASE_URL = "http://192.168.1.16:8080/"
const val BASE_URL = "http://10.222.222.205:8080/"
const val AddNotes = "createNote"
const val ReadNotes = "readNotes"
const val UpdateNote = "updateNote"
const val DeleteNotes = "deleteNote"
const val SearchNotes = "searchNote"
const val SortNotes = "sortNote"
const val FilterNotes = "filterNote"

lateinit var retroFit: Retrofit
fun getClient(): Retrofit {

    val client: OkHttpClient = OkHttpClient.Builder().apply {
        //if(BuildConfig.DEBUG)
        addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        readTimeout(1, TimeUnit.MINUTES)
        connectTimeout(2, TimeUnit.MINUTES)
        writeTimeout(1, TimeUnit.MINUTES)
    }.build()
    retroFit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    return retroFit
}
private val webservice = getClient().create(ApiClient::class.java)

internal interface ApiClient {
    @Multipart
    @POST(AddNotes)
    fun saveNotesData(@Part("noteData") noteData: String? , @Part("noteTime") noteTime: String? ,
                      @Part("notificationToken") notificationToken : String?/*, @Part file : MultipartBody.Part*/): Call<ResponseData>

    @GET(ReadNotes)
    fun getNotesData(): Call<MutableList<Note>>

    @Multipart
    @POST(UpdateNote)
    fun updateNoteById(@Part("noteId") noteId: Int , @Part("noteData") noteData : String): Call<MutableList<Note>>

    @POST(DeleteNotes)
    fun deleteNote(@Query("noteId") noteData: Int?): Call<ResponseData>

    @POST(SearchNotes)
    fun searchNote(@Query("note_data") note_data: String?): Call<MutableList<Note>>

    @POST(SortNotes)
    fun sortNotes(@Query("sortType") sortType: Int?): Call<MutableList<Note>>

    @POST(FilterNotes)
    fun filterNotes(@Query("startDate") sortType: String? , @Query("endDate") endDate : String): Call<MutableList<Note>>
}

//Below Method is used to Save all Note Data:-
fun submitNotes(noteData: String? , noteTime : String , notificationToken : String,  /*build : MultipartBody.Part ,*/ callBack : INoteSave){

    webservice.saveNotesData(noteData , noteTime , notificationToken /*,  build*/).enqueue(object : Callback<ResponseData>{
        override fun onResponse(call: Call<ResponseData>, response: retrofit2.Response<ResponseData>) {
            callBack.onNotesSave(response.body() as ResponseData)
        }

        override fun onFailure(call: Call<ResponseData>, t: Throwable) {
           Log.d("Failure:- " , t.message!!)
        }
    })
}

//Below Method is used to Get all Note Data:-
fun getAllNotes(callBack : INoteGetData){

    webservice.getNotesData().enqueue(object : Callback<MutableList<Note>>{
        override fun onResponse(call: Call<MutableList<Note>>, response: retrofit2.Response<MutableList<Note>>) {
            val notesData = response.body()
            callBack.onNotesGet(notesData)
        }

        override fun onFailure(call: Call<MutableList<Note>>, t: Throwable) {
           Log.d("Failure:- " , t.message!!)
        }
    })
}

//Below Method is used to Get all Searched Note Data:-
fun getAllSearchNotes(callBack : INoteGetData , searchText : String){

    webservice.searchNote(searchText).enqueue(object : Callback<MutableList<Note>>{
        override fun onResponse(call: Call<MutableList<Note>>, response: retrofit2.Response<MutableList<Note>>) {
            val notesData = response.body()
            callBack.onNotesGet(notesData)
        }

        override fun onFailure(call: Call<MutableList<Note>>, t: Throwable) {
           Log.d("Failure:- " , t.message!!)
        }
    })
}


//Below Method is used to Get all Sorted Note Data:-
fun getAllSortedNotes(callBack : INoteGetData , sortType : Int){

    webservice.sortNotes(sortType).enqueue(object : Callback<MutableList<Note>>{
        override fun onResponse(call: Call<MutableList<Note>>, response: retrofit2.Response<MutableList<Note>>) {
            val notesData = response.body()
            callBack.onNotesGet(notesData)
        }

        override fun onFailure(call: Call<MutableList<Note>>, t: Throwable) {
           Log.d("Failure:- " , t.message!!)
        }
    })
}

//Below Method is used to Get all Filter Note Data:-
fun getAllFilterNotes(callBack : INoteGetData , startDate : String , endDate: String){

    webservice.filterNotes(startDate , endDate).enqueue(object : Callback<MutableList<Note>>{
        override fun onResponse(call: Call<MutableList<Note>>, response: retrofit2.Response<MutableList<Note>>) {
            val notesData = response.body()
            callBack.onNotesGet(notesData)
        }

        override fun onFailure(call: Call<MutableList<Note>>, t: Throwable) {
           Log.d("Failure:- " , t.message!!)
        }
    })
}

//Below Method is used to Get all Note Data:-
fun updateNote(noteId : Int , noteData : String , callBack : INoteUpdateData){

    webservice.updateNoteById(noteId , noteData).enqueue(object : Callback<MutableList<Note>>{
        override fun onResponse(call: Call<MutableList<Note>>, response: retrofit2.Response<MutableList<Note>>) {
            val notesData = response.body()
            callBack.onNoteUpdate(notesData)
        }

        override fun onFailure(call: Call<MutableList<Note>>, t: Throwable) {
           Log.d("Failure:- " , t.message!!)
        }
    })
}

//Below Method is used to Delete all Note Data:-
fun deleteAllNotes(noteId : Int , callBack : INoteDelete){

    webservice.deleteNote(noteId).enqueue(object : Callback<ResponseData>{
        override fun onResponse(call: Call<ResponseData>, response: retrofit2.Response<ResponseData>) {
            response.body()?.let { callBack.onNoteDelete(it) }
        }

        override fun onFailure(call: Call<ResponseData>, t: Throwable) {
           Log.d("Failure:- " , t.message!!)
        }
    })
}

//Submit Notes Response Modal Class:-
class ResponseData{
    var responseCode : String? = null
    var responseMessage : String? = null
    var noteId : Int = 0
}

//Read Notes Response Modal Class:-
class Note{
    var noteId : Int = 0
    var noteData : String? = null
    var noteTime : String? = null
}
//Interface for Save Note Data:-
interface INoteSave{
    fun onNotesSave(resp: ResponseData)
}

//Interface for Delete Note Data:-
interface INoteDelete{
    fun onNoteDelete(resp: ResponseData)
}

//Interface for Get Note Data:-
interface INoteGetData{
    fun onNotesGet(resp: MutableList<Note>?)
}

//Interface for Update Note Data:-
interface INoteUpdateData{
    fun onNoteUpdate(resp: MutableList<Note>?)
}

//Show SnackBar:-
fun showSnackbar(context: Context , coordinatorLayout: CoordinatorLayout , msg : String , colorCode : Int , holdTime : Int){
    var mSnackBar : Snackbar?=null
    mSnackBar = if(holdTime == 0)
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG)
    else
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_SHORT)
    mSnackBar.view.setBackgroundColor(ContextCompat.getColor(context, colorCode))
    val params = mSnackBar.view.layoutParams as CoordinatorLayout.LayoutParams
    params.gravity = Gravity.TOP
    mSnackBar.view.layoutParams = params
    mSnackBar.show()
}