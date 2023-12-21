package com.example.deckmemory

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit
import androidx.appcompat.app.AlertDialog


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var currentCardImageView: ImageView
    private lateinit var nextCardImageView: ImageView
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var selectButton: Button
    private lateinit var cardNumberInput: EditText
    private lateinit var timerTextView: TextView
    private var timerHandler: Handler = Handler()
    private var startTime: Long = 0
    private var running: Boolean = false
    private var correctCards: Int = 0

    private var currentCardIndex = 0
    private var originalCardSequence = mutableListOf<Int>()
    private var shuffledCardSequence = mutableListOf<Int>()
    private var selectedSubSet = mutableListOf<Int>()
    private var selectedCards = mutableListOf<Int>()

    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            if (running) {
                val millis = System.currentTimeMillis() - startTime
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
                timerHandler.postDelayed(this, 500)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentCardImageView = findViewById(R.id.currentCardImageView)
        nextCardImageView = findViewById(R.id.nextCardImageView)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        selectButton = findViewById(R.id.selectButton)
        cardNumberInput = findViewById(R.id.cardNumberInput)
        timerTextView = findViewById(R.id.timerTextView)

        startButton.setOnClickListener {
            running = true
            startTime = System.currentTimeMillis()
            timerHandler.postDelayed(timerRunnable, 0)
            initializeDeck()
            selectButton.visibility = View.GONE
        }

        nextButton.setOnClickListener {
            if (currentCardIndex < shuffledCardSequence.size - 1) {
                currentCardIndex++
                showCard(currentCardIndex)
            }
        }

        prevButton.setOnClickListener {
            if (currentCardIndex > 0) {
                currentCardIndex--
                showCard(currentCardIndex)
            }
        }

        stopButton.setOnClickListener {
            running = false
            shuffledCardSequence.shuffle()
            currentCardIndex = 0
            if (shuffledCardSequence.isNotEmpty()) {
                showCard(currentCardIndex)
            }
            selectButton.visibility = View.VISIBLE
        }

        selectButton.setOnClickListener {
            selectCard()
        }
    }

    private fun initializeDeck() {
        val cardNames = resources.getStringArray(R.array.card_names)
        originalCardSequence.clear()
        originalCardSequence.addAll(cardNames.map { resources.getIdentifier(it, "drawable", packageName) })

        val numberOfCards = cardNumberInput.text.toString().toIntOrNull() ?: originalCardSequence.size

        // Wähle zufällige Karten basierend auf der Anzahl aus
        selectedSubSet = originalCardSequence.shuffled().take(numberOfCards) as MutableList<Int>
        selectedSubSet.shuffled()
        // Mische die ausgewählten Karten
        shuffledCardSequence.clear()
        shuffledCardSequence.addAll(selectedSubSet)

        currentCardIndex = 0
        if (shuffledCardSequence.isNotEmpty()) {
            showCard(currentCardIndex)
        }
    }

    private fun showCard(index: Int) {
        Log.i("info", index.toString())
        if (shuffledCardSequence.size > 0) {
            currentCardImageView.setImageResource(shuffledCardSequence[index]) // Verwenden Sie den Index, um auf die shuffledCardSequence zuzugreifen
            if (index + 1 < shuffledCardSequence.size) {
                nextCardImageView.setImageResource(shuffledCardSequence[index + 1])
                nextCardImageView.visibility = View.VISIBLE
            } else {
                nextCardImageView.visibility = View.INVISIBLE // Verstecken Sie das ImageView, wenn es keine nächste Karte gibt
            }
        }
    }

    fun showResultDialog(isSuccess: Boolean) {
        val context: Context = this // Ihr aktueller Context (z.B. Activity)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(if (isSuccess) "Geschafft" else "Nicht geschafft")
        builder.setMessage(if (isSuccess) "Die Aufgabe wurde erfolgreich abgeschlossen." else "Die Aufgabe wurde nicht erfolgreich abgeschlossen.")

        // Hinzufügen des OK-Knopfes zum Schließen des Dialogs
        builder.setPositiveButton("OK") { dialog, id ->
            dialog.dismiss() // Schließt den Dialog
        }

        // Erstellen und Anzeigen des Dialogs
        val dialog: AlertDialog = builder.create()
        dialog.show()
        correctCards = 0
        selectedCards.clear()

    }

    private fun selectCard() {
        Log.i("select", currentCardIndex.toString())
        if (shuffledCardSequence.isNotEmpty()) {
            val selectedCard = shuffledCardSequence.removeAt(currentCardIndex)
            selectedCards.add(selectedCard)
            Log.i("selectedCard", selectedCard.toString())
            correctCards += 1
            checkSelection()
            // Aktualisieren Sie die Anzeige der Karten
            if (shuffledCardSequence.isNotEmpty()) {
                currentCardIndex = 0
                showCard(currentCardIndex)
            }
        }
    }

    private fun checkSelection() {
        Log.i("check", selectedCards.size.toString())
        Log.i("correctSize", correctCards.toString())
        for (i in 0..correctCards-1) {
            Log.i("[i]", i.toString())
            Log.i("selectedCards[i]", selectedCards[i].toString())
            Log.i("selectedSubset[i]", selectedSubSet[i].toString())
            if (selectedSubSet[i] != selectedCards[i]) {
                showResultDialog(false)
                Log.i("checkFailure", "Failure")
                return
            }
        }
        if (correctCards == selectedSubSet.size) {
            showResultDialog(true)
            Log.i("checkSuccess", "Success")
        }
    }
}
