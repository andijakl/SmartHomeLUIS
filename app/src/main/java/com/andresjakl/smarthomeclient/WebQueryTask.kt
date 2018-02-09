/*
 * MIT License
 *
 * Copyright (c) 2018 Andreas Jakl, https://www.andreasjakl.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.andresjakl.smarthomeclient

import android.os.AsyncTask
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class WebQueryTask (private val resultCallback: (String?) -> Unit) : AsyncTask<URL, Void, String?>() {
    override fun doInBackground(vararg params: URL): String? {
        // This part will be executed in a background thread
        // First, retrieve the actual query URL. By default, this is turned into an array,
        // so we retrieve the first URL contained (there should only be one)
        val queryUrl = params[0]
        var queryResults : String? = null
        try {
            // Call the web service with the query URL
            queryResults = getResponseFromHttpUrl(queryUrl)
        } catch (e: IOException){
            // We ignore the errors here and just print them to the debug output
            e.printStackTrace()
            // We simply set the web request error message to the result string.
            // The caller will figure out it's no JSON and use its generic error handling.
            // You could of course return a specific error message from here for a cleaner
            // separation.
            queryResults = e.toString()
        }
        return queryResults
    }

    override fun onPostExecute(result: String?) {
        // This method is executed on the main thread after doInBackground() finished.
        // Send the result string back to the callback
        // The callback function was provided as a function parameter, expecting a String?
        resultCallback(result)
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    private fun getResponseFromHttpUrl(url: URL): String? {
        val urlConnection = url.openConnection() as HttpURLConnection
        try {
            val inStream = urlConnection.inputStream

            val scanner = Scanner(inStream)
            // Using pattern \a (beginning of the stream) - force scanner to read
            // the entire contents of the stream into the next token string
            // -> buffers data, different data sizes are allowed, converts
            // from UTF-8 to UTF-16
            scanner.useDelimiter("\\A")

            val hasInput = scanner.hasNext()
            return if (hasInput) {
                scanner.next()
            } else {
                null
            }
        } finally {
            urlConnection.disconnect()
        }
    }
}