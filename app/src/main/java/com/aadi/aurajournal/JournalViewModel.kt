package com.aadi.aurajournal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aadi.aurajournal.data.JournalEntry
import com.aadi.aurajournal.data.JournalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import com.aadi.aurajournal.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.json.JSONObject

class JournalViewModel(private val repository: JournalRepository): ViewModel() {


    //    user name
    private val _username= MutableStateFlow(repository.getUsername())
    val username=_username.asStateFlow()

    //AI insight staes
    private val _weeklySummary= MutableStateFlow("Give me a moment...")
    val weeklySummary=_weeklySummary.asStateFlow()

    private val _patterns= MutableStateFlow<List<String>>(emptyList())
    val patterns=_patterns.asStateFlow()

    private val _suggestedPrompt = MutableStateFlow("Thinking of a good prompt for you...")
    val suggestedPrompt = _suggestedPrompt.asStateFlow()


    private val _isInsightsLoading = MutableStateFlow(true)
    val isInsightsLoading = _isInsightsLoading.asStateFlow()

    private var lastAnalyzedEntriesHash: Int = 0


    //fun to update username
    fun updateUsername(newName: String){
        viewModelScope.launch {
            repository.saveUsername(newName)
            _username.value=newName
        }
    }
    //    convert Room FLow into stateflow for the Compose
    val allEntries: StateFlow<List<JournalEntry>> = repository.getEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    //fun to save entries
    fun saveEntry(content: String, entryId: Int = 0, mood: String? = null) {
        if (content.isNotBlank()) {
            viewModelScope.launch {
                val entry = JournalEntry(
                    id = entryId, // If 0, Room creates a new one. If >0, Room updates the existing one!
                    content = content,
                    mood = mood,
                    timeStamp = System.currentTimeMillis() // Update the timestamp on edit
                )
                repository.insert(entry)
            }
        }
    }

    //    delete
    fun deleteEntry(entry: JournalEntry){
        viewModelScope.launch {
            repository.delete(entry)
        }
    }



    //    dynamic AI prompt
    private val _prompt= MutableStateFlow("")
    val prompt=_prompt.asStateFlow()

    private val generativeModel= GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun generateNewPrompt(){
        viewModelScope.launch {
            _prompt.value="Thinking..."

            try {

                val promptText="You are a mindful journaling assistant to help uni students understand their feelings and this world and don't get overwhelmed by little things . Give me a single, deep, and reflective journaling prompt. Maximum 2 small sentences. Do not include any introductory or concluding text, just the prompt itself. "
                val response=generativeModel.generateContent(promptText)
                _prompt.value=response.text?:"what's on your mind "
            }catch (e: Exception){
                Log.e("GeminiError", "Failed to get prompt: ${e.message}", e)
                _prompt.value="What is one small thing that brought you peace today?"
            }
        }

    }


    //    fun to get insights
    suspend fun getInsights(forceRefresh: Boolean=false){
        viewModelScope.launch {
            _isInsightsLoading.value=true
        }
        //get latest entries from db (1 week entry)
        val entriesList=allEntries.first().take(7)
        // unique signature of exact entries
        val currentHash = entriesList.hashCode()

        if(!forceRefresh && currentHash==lastAnalyzedEntriesHash && _weeklySummary.value != "Give me a moment to reflect on your recent entries..."){
            return
        }
        _isInsightsLoading.value=true

        try {

            if (entriesList.isEmpty()){
                _weeklySummary.value = "You haven't written any entries yet. Start journaling to see your insights!"
                _patterns.value = emptyList()
                _suggestedPrompt.value = "How are you feeling right now?"
                _isInsightsLoading.value = false
            }
            //save the new hash to current
            lastAnalyzedEntriesHash=currentHash

            //format them into a single string and send to gemini
            val entriesText=entriesList.joinToString("\n"){
                "Mood: ${it.mood ?: "Neutral"}, Content: ${it.content}"
            }
            //json like prompt to be sent to gemini
            val promptText = """
                    You are an empathetic, highly observant journaling assistant. 
                    Read the following recent journal entries from the user:
                    $entriesText
                    
                    Analyze them and return exactly 3 things in strict JSON format:
                    1. "summary": A 2-sentence empathetic summary of their emotional arc.
                    2. "patterns": An array of exactly 2 strings containing interesting correlations (e.g., "You tend to feel...").
                    3. "prompt": A single actionable journaling prompt for tomorrow.
                    
                    Return ONLY valid JSON. Do not use markdown blocks like ```json.
                    Example format:
                    {
                        "summary": "...",
                        "patterns": ["...", "..."],
                        "prompt": "..."
                    }
                """.trimIndent()

            //calling gemini
            val response=generativeModel.generateContent(promptText)
            val rawText = response.text ?: "{}"
            val responseText = rawText.replace("```json", "").replace("```", "").trim()

            //  Parse the JSON and update the UI states
            val jsonObject = JSONObject(responseText)

            _weeklySummary.value = jsonObject.getString("summary")

            val patternsArray = jsonObject.getJSONArray("patterns")
            val extractedPatterns = mutableListOf<String>()
            for (i in 0 until patternsArray.length()) {
                extractedPatterns.add(patternsArray.getString(i))
            }
            _patterns.value = extractedPatterns

            _suggestedPrompt.value = jsonObject.getString("prompt")
        }catch (e: Exception){
            _weeklySummary.value = "I need a few more detailed entries to generate a clear summary. Keep writing!"
            _patterns.value = listOf("Pattern recognition will unlock as you log more entries.")
            _suggestedPrompt.value = "What is one small thing that brought you peace today?"
        }
        finally {
            _isInsightsLoading.value=false
        }

    }



}
// Factory to create the ViewModel since we need to pass the Repository into it
class JournalViewModelFactory(private val repository: JournalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JournalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
