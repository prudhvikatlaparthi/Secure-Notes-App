package com.pru.notes.activities

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pru.notes.R
import com.pru.notes.adapters.NoteAdapter
import com.pru.notes.data.Note
import com.pru.notes.help.Constants.startActivityDelayTimeMilliS
import com.pru.notes.viewmodels.NoteViewModel
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_note_list.*
import kotlinx.android.synthetic.main.tool_bar.*
import java.util.*

class NotesListActivity : BaseActivity() {

    private lateinit var mainLayout: CoordinatorLayout
    private lateinit var noteViewModel: NoteViewModel

    companion object {
        const val ADD_NOTE_REQUEST = 1
        const val EDIT_NOTE_REQUEST = 2
    }

    override fun initialize() {
        mainLayout = myInflater.inflate(R.layout.activity_note_list, null) as CoordinatorLayout
        mainLayout.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
        llBody.addView(mainLayout)

        tb_title.text = "All Notes"

        buttonAddNote.setOnClickListener {
            startAddEditNoteActivity(null)
        }

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)

        var adapter = NoteAdapter()

        recycler_view.adapter = adapter

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        noteViewModel.getAllNotes().observe(this, Observer<List<Note>> {
            if (it.size == 0) {
                recycler_view.visibility = View.GONE
                no_data_view.visibility = View.VISIBLE
            } else {
                recycler_view.visibility = View.VISIBLE
                no_data_view.visibility = View.GONE
                adapter.submitList(it)
            }
        })

        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                noteViewModel.delete(adapter.getNoteAt(viewHolder.adapterPosition))
                showSnackMessage("Note Deleted!")
            }
        }
        ).attachToRecyclerView(recycler_view)

        adapter.setOnItemClickListener(object : NoteAdapter.OnItemClickListener {
            override fun onItemClick(note: Note) {
                startAddEditNoteActivity(note)
            }
        })
    }

    private fun startAddEditNoteActivity(note: Note?) {
        val handler = Handler()
        handler.postDelayed({
            if (note == null) {
                startActivityForResult(
                    Intent(this, AddEditNoteActivity::class.java),
                    ADD_NOTE_REQUEST
                )
            } else {
                val intent = Intent(baseContext, AddEditNoteActivity::class.java)
                intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.id)
                intent.putExtra(AddEditNoteActivity.EXTRA_DESCRIPTION, note.notesDescription)
                intent.putExtra(AddEditNoteActivity.EXTRA_IMAGE, note.noteImage)
                intent.putExtra(AddEditNoteActivity.EXTRA_IMAGE_SIGN, note.notePaint)
                startActivityForResult(
                    intent,
                    EDIT_NOTE_REQUEST
                )
            }
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }, startActivityDelayTimeMilliS)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.delete_all_notes -> {
                noteViewModel.deleteAllNotes()
                showSnackMessage("All notes deleted!")
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_NOTE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val img = data.getByteArrayExtra(AddEditNoteActivity.EXTRA_IMAGE)
                val sign = data.getByteArrayExtra(AddEditNoteActivity.EXTRA_IMAGE_SIGN)
                val newNote = Note(
                    data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION),
                    Date(data.getLongExtra(AddEditNoteActivity.EXTRA_MODIFIED_DATE, 0)),
                    img, sign
                )
                noteViewModel.insert(newNote)
            }

        } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val img = data.getByteArrayExtra(AddEditNoteActivity.EXTRA_IMAGE)
                val sign = data.getByteArrayExtra(AddEditNoteActivity.EXTRA_IMAGE_SIGN)
                val updateNote = Note(
                    data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION),
                    Date(data.getLongExtra(AddEditNoteActivity.EXTRA_MODIFIED_DATE, 0)),
                    img, sign
                )
                updateNote.id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1)
                noteViewModel.update(updateNote)
            }
        }
    }

    override fun onButtonNoClick(from: String) {

        super.onButtonNoClick(from)
    }

    override fun onButtonYesClick(from: String) {

        super.onButtonYesClick(from)
    }
}


