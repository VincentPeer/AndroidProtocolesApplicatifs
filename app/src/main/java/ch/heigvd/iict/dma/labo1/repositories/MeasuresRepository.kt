package ch.heigvd.iict.dma.labo1.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import ch.heigvd.iict.dma.labo1.models.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.system.measureTimeMillis

class MeasuresRepository(private val scope : CoroutineScope,
                         private val dtd : String = "https://mobile.iict.ch/measures.dtd",
                         private val httpUrl : String = "http://mobile.iict.ch/api",
                         private val httpsUrl : String = "https://mobile.iict.ch/api") {

    private val _measures = MutableLiveData(mutableListOf<Measure>())
    val measures = Transformations.map(_measures) { mList -> mList.toList() }

    private val _requestDuration = MutableLiveData(-1L)
    val requestDuration : LiveData<Long> get() = _requestDuration

    fun generateRandomMeasures(nbr: Int = 3) {
        addMeasures(Measure.getRandomMeasures(nbr))
    }

    fun resetRequestDuration() {
        _requestDuration.postValue(-1L)
    }

    fun addMeasure(measure: Measure) {
        addMeasures(listOf(measure))
    }

    fun addMeasures(measures: List<Measure>) {
        val l = _measures.value!!
        l.addAll(measures)
        _measures.postValue(l)
    }

    fun clearAllMeasures() {
        _measures.postValue(mutableListOf())
    }

    fun measuresToJson() : String {
        var listOfMeasure = Gson().toJson(measures.value)
        Log.d("Json measures sent", listOfMeasure)
        return listOfMeasure
    }

    fun sendMeasureToServer(encryption : Encryption, compression : Compression, networkType : NetworkType, serialisation : Serialisation) {
        scope.launch(Dispatchers.Default) {

            val url = when (encryption) {
                Encryption.DISABLED -> httpUrl
                Encryption.SSL -> httpsUrl
            }

            val elapsed = measureTimeMillis {
                Log.e("SendViewModel", "Implement me !!! Send measures to $url") //TODO
            }
            _requestDuration.postValue(elapsed)

            val connection = withContext(Dispatchers.IO) {
                URL(url).openConnection()
            } as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use {
                it.append(measuresToJson())
            }

            // On traite la réponse du service REST
            val responseCode = connection.responseCode
            val json : String
            Log.d("MeasuresRepository", "responseCode: $responseCode")
            connection.inputStream.bufferedReader(Charsets.UTF_8).use {
                json = it.readText()
            }
            //val measuresResponse = Gson().fromJson(json, mutableListOf<Measure>()::class.java)
            val measuresResponse = stringToArray(json, Array<Measure>::class.java)[0]

            // On met à jour les mesures avec le status
            val l = _measures.value!!
            for (i in 0 until l.size) {
                l[i].status = measuresResponse[i].status
            }

        }
    }

    // Trick to convert a string to an array of objects
    fun <T> stringToArray(s: String?, clazz: Class<Array<T>>?): MutableList<Array<T>> {
        val arr = Gson().fromJson(s, clazz)
        return Arrays.asList(arr)
    }
}
