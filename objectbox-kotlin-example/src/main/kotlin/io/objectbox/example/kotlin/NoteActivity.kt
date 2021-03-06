package io.objectbox.example.kotlin

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener

import java.text.DateFormat
import java.util.Date

import io.objectbox.Box
import io.objectbox.query.Query

class NoteActivity : Activity() {

    private lateinit var editText: EditText
    private lateinit var addNoteButton: View

    private lateinit var notesBox: Box<Note>
    private lateinit var notesQuery: Query<Note>
    private lateinit var notesAdapter: NotesAdapter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        setUpViews()

        notesBox = (application as App).boxStore.boxFor(Note::class.java)

        // query all notes, sorted a-z by their text (http://greenrobot.org/objectbox/documentation/queries/)
        notesQuery = notesBox.query().order(Note_.text).build()
        updateNotes()
    }

    /** Manual trigger to re-query and update the UI. For a reactive alternative check [ReactiveNoteActivity].  */
    private fun updateNotes() {
        val notes = notesQuery.find()
        notesAdapter.setNotes(notes)
    }

    protected fun setUpViews() {
        val listView = findViewById(R.id.listViewNotes) as ListView
        listView.onItemClickListener = noteClickListener

        notesAdapter = NotesAdapter()
        listView.adapter = notesAdapter

        addNoteButton = findViewById(R.id.buttonAdd)
        addNoteButton.isEnabled = false

        editText = findViewById(R.id.editTextNote) as EditText
        editText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addNote()
                return@OnEditorActionListener true
            }
            false
        })
        editText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val enable = s.length != 0
                addNoteButton.isEnabled = enable
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })
    }

    fun onAddButtonClick(view: View) {
        addNote()
    }

    private fun addNote() {
        val noteText = editText.text.toString()
        editText.setText("")

        val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
        val comment = "Added on " + df.format(Date())

        val note = Note()
        note.setText(noteText)
        note.setComment(comment)
        note.setDate(Date())
        notesBox.put(note)
        Log.d(App.TAG, "Inserted new note, ID: " + note.getId())

        updateNotes()
    }

    internal var noteClickListener: OnItemClickListener = OnItemClickListener { parent, view, position, id ->
        val note = notesAdapter.getItem(position)
        notesBox.remove(note)
        Log.d(App.TAG, "Deleted note, ID: " + note.getId())

        updateNotes()
    }
}