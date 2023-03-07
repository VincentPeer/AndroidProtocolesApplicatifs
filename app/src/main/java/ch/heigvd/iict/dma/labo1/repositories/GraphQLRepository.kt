package ch.heigvd.iict.dma.labo1.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.heigvd.iict.dma.labo1.models.Author
import ch.heigvd.iict.dma.labo1.models.Book
import ch.heigvd.iict.dma.labo1.models.Measure
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
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

    data class ResponseData(val data: Data)
    data class Data(val findAllAuthors: List<Author>)

    private fun serverConnection() {
        scope.launch(Dispatchers.Default) {
            val connection = URL(httpsUrl).openConnection() as HttpURLConnection
            val query = "{\"query\": \"{findAllAuthors{id, name}}\"}"
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use {
                it.append(query)
            }
            val responseCode = connection.responseCode
            Log.d("GraphQLRepository", "Server response code $responseCode")
            val json: String
            connection.inputStream.bufferedReader(Charsets.UTF_8).use {
                json = it.readText()
            }

            val gson = Gson()
            val responseData = gson.fromJson(json, ResponseData::class.java)
            val authors = responseData.data.findAllAuthors
            _authors.postValue(authors)


        }
    }

    fun loadAllAuthorsList() {
        scope.launch(Dispatchers.Default) {
            val elapsed = measureTimeMillis {
                serverConnection()
                // TODO make the request to server
                // fill _authors LiveData with list of all authors
            }
            _requestDuration.postValue(elapsed)
        }
    }

    fun loadBooksFromAuthor(author: Author) {
        scope.launch(Dispatchers.Default) {
            val elapsed = measureTimeMillis {
                // TODO make the request to server
                // fill _books LiveData with list of book of the author
            }
            _requestDuration.postValue(elapsed)
        }
    }
}

