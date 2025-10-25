package com.example.projectjarvis.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL



object RealtimePreamble {

    fun getSystemContext(username: String, assistantName: String): String {
        return """
            Hello, I am $username.I have built you, you will always respect me and designate me with 'Sir' at start of the conversation only and never on the end.
             You are a very accurate and advanced AI assistant named $assistantName which has real-time up-to-date information from the internet and never say phrases like 'according to provided information' that people doubt you.
            *** Provide Answers In a Professional Way, make sure to add full stops, commas, question marks, and use proper grammar.***
            *** Just answer the question from the provided data in a professional way. ***
            """.trimIndent()
    }

}



object GoogleSearch {

    private const val API_KEY = "AIzaSyCyjAkuOzV_Yu9lT-CgKUktp-k7qiy0Zb4"
    private const val CX = "22aa4bbb583f84c93"

    suspend fun getSearchResults(query: String): String = withContext(Dispatchers.IO) {
        val urlString =
            "https://www.googleapis.com/customsearch/v1?q=${query.replace(" ", "+")}&key=$API_KEY&cx=$CX"

        return@withContext try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val items = json.optJSONArray("items")

            if (items == null || items.length() == 0) {
                return@withContext "No search results found for \"$query\"."
            }

            buildString {
                append("The search results for \"$query\" are:\n[start]\n")
                for (i in 0 until minOf(5, items.length())) {
                    val item = items.getJSONObject(i)
                    val title = item.optString("title")
                    val snippet = item.optString("snippet")
                    val link = item.optString("link")
                    append("${i + 1}. $title\n$snippet\n$link\n\n")
                }
                append("[end]")
            }
        } catch (e: Exception) {
            "Error fetching search results: ${e.localizedMessage}"
        }
    }
}