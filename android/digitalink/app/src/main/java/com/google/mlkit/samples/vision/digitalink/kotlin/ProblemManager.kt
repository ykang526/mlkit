package com.google.mlkit.samples.vision.digitalink.kotlin

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.samples.vision.digitalink.kotlin.data.KanaData
import java.io.File
import java.io.IOException
import java.lang.reflect.Type


class ProblemManager {
    private lateinit var hiragana: List<KanaData>
    private lateinit var katakana: List<KanaData>
    lateinit  var currP: KanaData
    private var problemIndex:Int = 0
    private var gson = Gson()
    fun onCreate(applicationContext : Context) {
        val hiraganaJson = getJsonDataFromAsset(applicationContext, "hiragana.json")
        val katakanaJson = getJsonDataFromAsset(applicationContext, "katakana.json")
        if (hiraganaJson != null && katakanaJson != null) {
            hiragana = makeKanaListFromJson(hiraganaJson)
            katakana = makeKanaListFromJson(katakanaJson)
        } else {
            Log.e("problem", "Error loading Katakana & Hiragana")
        }
        currP = hiragana[problemIndex]
    }
    private fun makeKanaListFromJson(json: String): List<KanaData> {
        val listType: Type = object : TypeToken<ArrayList<KanaData>>() {}.type
        return gson.fromJson(json, listType)
    }
    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
    fun nextQuestion(){
        if (problemIndex >= hiragana.size) {
            Log.i("problemManager", "last problem")
            return
        } else {
            problemIndex += 1
            currP = hiragana[problemIndex]
            Log.i("problemManager", "$problemIndex")
        }
        return
    }
    fun compareAnswer(userInput:String): Boolean {
        if(userInput == currP.kana) return true
        return false
    }
}