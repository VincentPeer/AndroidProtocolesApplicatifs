package ch.heigvd.iict.dma.labo1.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.heigvd.iict.dma.labo1.models.Author
import ch.heigvd.iict.dma.labo1.models.Book
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis

class GraphQLRepository(private val scope : CoroutineScope, private val httpsUrl : String = "https://mobile.iict.ch/graphql") {

    private val _working = MutableLiveData(false)
    val working : LiveData<Boolean> get() = _working

    private val _authors = MutableLiveData<List<Author>>(mutableListOf())
    val authors : LiveData<List<Author>> get() = _authors

    private val _books = MutableLiveData<List<Book>>(mutableListOf())
    val books : LiveData<List<Book>> get() = _books

    private val _requestDuration = MutableLiveData(-1L)
    val requestDuration : LiveData<Long> get() = _requestDuration

    fun resetRequestDuration() {
        _requestDuration.postValue(-1L)
    }

    private data class ResponseData1(val data: Data)
    private data class Data(val findAllAuthors: List<Author>)
    private data class ResponseData2(val data: Data2)
    private data class Data2(val findAuthorById: FindAuthorById)
    private data class FindAuthorById(val books: List<Book>)


    private suspend fun sendRequest(query: String):String = suspendCoroutine { continuation ->
        var json = ""
        val connection = URL(httpsUrl).openConnection() as HttpURLConnection
        connection.doOutput = true
        connection.requestMethod = "POST"
        connection.outputStream.bufferedWriter(Charsets.UTF_8).use {
            it.append(query)
        }
        val responseCode = connection.responseCode
        Log.d("GraphQLRepository", "Server response code $responseCode")
        connection.inputStream.bufferedReader(Charsets.UTF_8).use {
            json = it.readText()
        }

        continuation.resume(json)
    }

    fun loadAllAuthorsList() {
        scope.launch(Dispatchers.Default) {
            val query = "{\"query\": \"{findAllAuthors{id, name}}\"}"
            var json: String
            val elapsed = measureTimeMillis {
                json = sendRequest(query)
                // TODO make the request to server
                // fill _authors LiveData with list of all authors
            }
            _requestDuration.postValue(elapsed)
            val gson = Gson()
            val responseData = gson.fromJson(json, ResponseData1::class.java)
            val authors = responseData.data.findAllAuthors
            _authors.postValue(authors)
        }
    }



    fun loadBooksFromAuthor(author: Author) {
        scope.launch(Dispatchers.Default) {
            val query = "{\"query\": \"{findAuthorById(id: ${author.id}){books{id,title,publicationDate,authors{id,name}}}}\"}"
            var json = ""
            val elapsed = measureTimeMillis {
                json = sendRequest(query)
                // TODO make the request to server
                // fill _books LiveData with list of book of the author
            }
            _requestDuration.postValue(elapsed)
            val gson = Gson()
            val responseData = gson.fromJson(json, ResponseData2::class.java)
            val books = responseData.data.findAuthorById.books
            _books.postValue(books)
        }
    }
}

