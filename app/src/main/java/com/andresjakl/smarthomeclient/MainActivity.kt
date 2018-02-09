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

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    // Tag that we will use as starting text of all log messages coming from this class
    private val TAG = MainActivity::class.java.simpleName

    // A Kotlin companion object can be compared to static in Java.
    // Make sure you define the values as const (compile-time constant) to optimize performance
    companion object {
        // TODO Update URL with your endpoint ID from Microsoft LUIS
        private const val ENDPOINT = "https://westus.api.cognitive.microsoft.com/luis/v2.0/apps/<ENTER_YOUR_APP_URL>"
        private const val PARAM_APPKEY = "subscription-key"
        // TODO Update app key with your key generated by Microsoft LUIS
        private const val APPKEY = ""
        private const val PARAM_QUERY = "q"

        // Keys for preserving text in the layout when rotating the screen
        private const val KEY_TV_RESULTS = "tv_results"
        private const val KEY_TV_TOP_INTENT = "tv_top_intent"
        private const val KEY_TV_TOP_ENTITY_TYPE = "tv_top_entity_type"
        private const val KEY_TV_TOP_ENTITY_VALUE = "tv_top_entity_value"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the appkey is defined - show a warning message otherwise and quit the app
        if (APPKEY.isEmpty()) {
            val errorDialog = AlertDialog.Builder(this).create()
            errorDialog.setMessage(getString(R.string.msg_no_appkey))
            // Configure so that the user can only click on OK and not cancel the dialog
            errorDialog.setCancelable(false)
            // Add a single OK button and quit the app when it's clicked
            errorDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_button_ok)) { _, _ ->  finish()}
            errorDialog.show()
        }

        // Restore previously saved instance state if available
        if (savedInstanceState != null) {
            tv_results.text = savedInstanceState.getString(KEY_TV_RESULTS)
            tv_top_intent.text = savedInstanceState.getString(KEY_TV_TOP_INTENT)
            tv_top_entity_type.text = savedInstanceState.getString(KEY_TV_TOP_ENTITY_TYPE)
            tv_top_entity_value.text = savedInstanceState.getString(KEY_TV_TOP_ENTITY_VALUE)
        }

        // Handle virtual keyboard Done button
        // Defined in layout file: android:imeOptions="actionDone"
        et_query_text.setOnEditorActionListener { tv, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {makeSearchQuery(); true}
                else -> false
            }
         }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        // Serialize all the text currently shown in the UI to the saved state,
        // so that it can be restored when the app resumes (e.g., when the screen is rotated)
        outState?.putString(KEY_TV_RESULTS, tv_results.text.toString())
        outState?.putString(KEY_TV_TOP_INTENT, tv_top_intent.text.toString())
        outState?.putString(KEY_TV_TOP_ENTITY_TYPE, tv_top_entity_type.text.toString())
        outState?.putString(KEY_TV_TOP_ENTITY_VALUE, tv_top_entity_value.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val selectedMenuItem = item?.itemId
        when (selectedMenuItem) {
            R.id.action_send_command ->  {
                // The "Send" menu item was tapped
                Log.d(TAG, "Menu item tapped")
                // Show an info message to the user
                Toast.makeText(this, getString(R.string.msg_menu_item_tapped), Toast.LENGTH_LONG).show()
                // Send the query to the LUIS service
                makeSearchQuery()
                // We handled this menu item tap
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Create a search query for the LUIS service and asynchronously execute it using the
     * WebQueryTask. The query text is retrieved from the EditText view in the UI.
     */
    private fun makeSearchQuery() {
        // Retrive entered text from EditText View
        val queryCommand = et_query_text.text.toString()
        if (queryCommand.isEmpty()) {
            // If no text was entered, inform the user
            Toast.makeText(this, getString(R.string.msg_enter_query), Toast.LENGTH_LONG).show()
            return
        }
        // Build the URL for the request to the LUIS service
        val queryUrl = buildUrl(queryCommand)
        Log.d(TAG, queryUrl.toString())
        // Start the web query task to submit the REST request to the service.
        // The result text is sent back to the handleWebResult function in this class.
        WebQueryTask({ resultString : String? -> handleWebResult(resultString)}).execute(queryUrl)
    }

    /**
     * Callback function for the result string of the web request.
     * The method expects this to be a JSON. This is parsed and relevant bits are assigned
     * to the debug UI.
     *
     * @param resultString the string returned from the LUIS web service, should be a JSON string.
     */
    private fun handleWebResult(resultString: String?) {
        if (resultString == null) {
            // Simple error handling: if no text was retrieved, show an error message on the UI
            tv_results.text = getString(R.string.msg_null_from_web_query)
            return
        }
        // Add the complete JSON response to the UI
        tv_results.text = resultString
        // Parse the JSON string to a JSON object
        val parsedJson : JSONObject
        try {
            parsedJson = JSONObject(resultString)
        } catch (e: Exception) {
            // In case parsing the JSON goes wrong, show an error message including the
            // string that we got
            tv_results.text = getString(R.string.error_json_parsing, e.toString(), resultString)
            return
        }
        // Retrieve the top scoring intent from the JSON response
        tv_top_intent.text = parsedJson.getJSONObject("topScoringIntent")?.getString("intent")
        // Get the array of entities
        val entitiesArray = parsedJson.getJSONArray("entities")
        if (entitiesArray != null && entitiesArray.length() > 0) {
            // If at least one entity is present, show its contents on the UI
            tv_top_entity_type.text = entitiesArray.getJSONObject(0)?.getString("type")
            tv_top_entity_value.text = entitiesArray.getJSONObject(0)?.getString("entity")
        } else {
            // No entity was received? Reset the UI
            tv_top_entity_type.text = null
            tv_top_entity_value.text = null
        }
    }

    /**
     * Build a Java-style URL based on the pre-configured endpoint, the app key and the
     * encoded query text entered by the user.
     *
     * @param queryText the text entered by the user. This method takes care of encoding
     * the text in a suitable way for GET requests.
     * @return the complete URL to be used for the request
     */
    private fun buildUrl(queryText : String) : URL {
        // Build Android Uri, appending the parameters
        val builtUri = Uri.parse(ENDPOINT).buildUpon()
                        .appendQueryParameter(PARAM_APPKEY, APPKEY)
                        .appendQueryParameter(PARAM_QUERY, queryText)
                        .build()

        // Network method requires Java URL -> Convert!
        return URL(builtUri.toString())
    }

}