package com.example.candy_crush

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.candy_crush.uiltel.OnSwipeListener
import java.util.Arrays.asList
import android.animation.ObjectAnimator
import android.content.Context
import android.os.CountDownTimer
import android.view.animation.LinearInterpolator
import android.widget.Toast
import android.widget.PopupWindow
import android.view.LayoutInflater
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {

    var candies = intArrayOf(
        R.drawable.bluecandy,
        R.drawable.greencandy,
        R.drawable.orangecandy,
        R.drawable.purplecandy,
        R.drawable.redcandy,
        R.drawable.yellowcandy,
    )

    var widthOfBlock :Int = 0
    var noOfBlock : Int = 8
    var widthOfScreen :Int = 0
    lateinit var candy : ArrayList<ImageView>
    var candyToBeDragged : Int = 0
    var candyToBeReplaced : Int = 0
    var notCandy :Int = R.drawable.transparent

    lateinit var mHandler: Handler
    private lateinit var scoreResult :TextView
    var score = 0
    var interval = 100L

    var positions = 0..63

    private lateinit var bestScoreResult: TextView
    private var bestScore = 0

    private lateinit var timerTextView: TextView // TextView to show the timer
    private var gameDuration: Long = 60000 // 60 seconds for the game duration
    private var countDownTimer: CountDownTimer? = null
    private lateinit var newGameButton: Button
    private lateinit var gameOverText: TextView



    // Declare a boolean variable to track the game state
    var isGameOngoing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameOverText = findViewById(R.id.gameOverText)
        newGameButton = findViewById(R.id.newGameButton)
        newGameButton.setOnClickListener {
            resetGame()
        }


        scoreResult = findViewById(R.id.score)

        val displayMetrics = resources.displayMetrics
        widthOfScreen = displayMetrics.widthPixels
        var heightOfScreen = displayMetrics.heightPixels

        widthOfBlock = widthOfScreen / noOfBlock

        timerTextView = findViewById(R.id.timerTextView)
        startGame()

        candy = ArrayList()
        createBoard()

        for (imageView in candy) {
            imageView.setOnTouchListener(
                object :OnSwipeListener(this) {
                    override fun onSwipeRight() {
                        super.onSwipeRight()

                        candyToBeDragged = imageView.id
                        candyToBeReplaced = candyToBeDragged + 1

                        if (positions.contains(candyToBeReplaced)) {
                            candyInterChacge()
                        }

                    }

                    override fun onSwipeLeft() {
                        super.onSwipeLeft()

                        candyToBeDragged = imageView.id
                        candyToBeReplaced = candyToBeDragged - 1

                        if (positions.contains(candyToBeReplaced)) {
                            candyInterChacge()
                        }

                    }

                    override fun onSwipeTop() {

                        candyToBeDragged = imageView.id
                        candyToBeReplaced = candyToBeDragged - noOfBlock

                        if (positions.contains(candyToBeReplaced)) {
                            candyInterChacge()
                        }

                    }

                    override fun onSwipeBottom() {

                        candyToBeDragged = imageView.id
                        candyToBeReplaced = candyToBeDragged + noOfBlock

                        if (positions.contains(candyToBeReplaced)) {
                            candyInterChacge()
                        }

                    }
                })
        }
        bestScoreResult = findViewById(R.id.bestscore)
        loadBestScore()
        mHandler = Handler()
        startRepeat()
    }

    private fun resetGame() {
        score = 0
        scoreResult.text = "0"

        shuffleCandies()
        startRepeat()
        startGame()
        newGameButton.visibility = View.INVISIBLE
        gameOverText.visibility = View.INVISIBLE

        resetBestScore()
    }

    private fun shuffleCandies() {
        val n = candy.size
        for (i in n - 1 downTo 1) {
            val j = (Math.random() * (i + 1)).toInt()
            // Swap candy[i] and candy[j]
            val temp = candy[i].tag as Int
            candy[i].setImageResource(candy[j].tag as Int)
            candy[i].setTag(candy[j].tag as Int)
            candy[j].setImageResource(temp)
            candy[j].setTag(temp)
        }
    }

    private fun resetBestScore() {
        saveBestScore(bestScore) // Save the updated best score in SharedPreferences
        bestScoreResult.text = "$bestScore" // Update the displayed best score
    }

    private fun startGame() {
        startCountDownTimer()
        // Reset or initialize other game states as needed
        isGameOngoing = true // Set the game as ongoing
    }

    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(gameDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "Time: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                timerTextView.text = "Time: 0s"
                endGame()
            }
        }.start()
    }

    private fun endGame() {
        // Check if the current score is higher than the best score
        if (score > bestScore) {
            bestScore = score
            saveBestScore(bestScore)
        }

        // Show the "Game Over" text
        gameOverText.visibility = View.VISIBLE

        // Lock the board when the game ends
        newGameButton.visibility = View.VISIBLE

        isGameOngoing = false
    }


    private fun saveBestScore(score: Int) {
        val sharedPreferences = getSharedPreferences("CandyCrushGame", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("BestScore", score)
        editor.apply()
    }

    private fun loadBestScore() {
        val sharedPreferences = getSharedPreferences("CandyCrushGame", MODE_PRIVATE)
        bestScore = sharedPreferences.getInt("BestScore", 0)
        bestScoreResult.text = "$bestScore"
    }

    private fun candyInterChacge() {
        // Check if the game is ongoing before swapping candies
        if (isGameOngoing) {
            var background: Int = candy.get(candyToBeReplaced).tag as Int
            var background1: Int = candy.get(candyToBeDragged).tag as Int

            // Check if the candies are different and adjacent
            if (background != background1 && areAdjacent(candyToBeDragged, candyToBeReplaced)) {
                candy.get(candyToBeDragged).setImageResource(background)
                candy.get(candyToBeReplaced).setImageResource(background1)

                candy.get(candyToBeDragged).setTag(background)
                candy.get(candyToBeReplaced).setTag(background1)
            }
        }
    }

    private fun areAdjacent(index1: Int, index2: Int): Boolean {
        // Check if the candies are adjacent (horizontally or vertically)
        return (index1 + 1 == index2 && index1 / noOfBlock == index2 / noOfBlock) || // Right
                (index1 - 1 == index2 && index1 / noOfBlock == index2 / noOfBlock) || // Left
                (index1 + noOfBlock == index2) || // Bottom
                (index1 - noOfBlock == index2)    // Top
    }

    private fun checkRowForMoreThanThree() {
        // Check if the game is ongoing before checking for matches
        if (isGameOngoing) {
            for (i in 0 until candy.size - 2) {
                var chosenCandy = candy[i].tag as Int
                var isBlank: Boolean = chosenCandy == notCandy
                var count = 1

                for (j in i + 1 until i + noOfBlock) {
                    if (j >= candy.size || chosenCandy != candy[j].tag as Int || isBlank) break
                    count++
                }

                if (count >= 3) {
                    for (k in i until i + count) {
                        if (k < candy.size) {
                            score += 1
                            scoreResult.text = "$score"
                            candy[k].setImageResource(notCandy)
                            candy[k].setTag(notCandy)
                        }
                    }
                }
            }
            moveDownCandies()
        }
    }

    private fun checkColumnForMoreThanThree() {
        // Check if the game is ongoing before checking for matches
        if (isGameOngoing) {
            for (i in 0 until candy.size - 2 * noOfBlock) {
                var chosenCandy = candy[i].tag as Int
                var isBlank: Boolean = chosenCandy == notCandy
                var count = 1

                for (j in 1..noOfBlock - 1) {
                    val index = i + j * noOfBlock
                    if (index >= candy.size || chosenCandy != candy[index].tag as Int || isBlank) break
                    count++
                }

                if (count >= 3) {
                    for (k in 0 until count) {
                        val index = i + k * noOfBlock
                        if (index < candy.size) {
                            score += 1
                            scoreResult.text = "$score"
                            candy[index].setImageResource(notCandy)
                            candy[index].setTag(notCandy)
                        }
                    }
                }
            }
            moveDownCandies()
        }
    }

    private fun moveDownCandies() {
        // Check if the game is ongoing before moving down candies
        if (isGameOngoing) {
            val firstRow = arrayOf(1, 2, 3, 4, 5, 6, 7)
            val list = asList(*firstRow)
            var delay = 0L

            for (i in 55 downTo 0) {
                mHandler.postDelayed({
                    if (candy[i + noOfBlock].tag as Int == notCandy) {
                        candy[i + noOfBlock].setImageResource(candy[i].tag as Int)
                        candy[i + noOfBlock].setTag(candy[i].tag as Int)

                        candy[i].setImageResource(notCandy)
                        candy[i].setTag(notCandy)
                    }

                    if (list.contains(i) && candy[i].tag == notCandy) {
                        val randomColor: Int = Math.abs(Math.random() * candies.size).toInt()
                        candy[i].setImageResource(candies[randomColor])
                        candy[i].setTag(candies[randomColor])
                    }
                }, delay)

                delay += 40
            }

            for (i in 0..7) {
                mHandler.postDelayed({
                    if (candy[i].tag as Int == notCandy) {
                        val randomColor: Int = Math.abs(Math.random() * candies.size).toInt()
                        candy[i].setImageResource(candies[randomColor])
                        candy[i].setTag(candies[randomColor])
                    }
                }, delay)
            }
        }
    }

    val repeatChecker :Runnable = object :Runnable {
        override fun run() {
            try {
                checkRowForMoreThanThree()
                checkColumnForMoreThanThree()
                moveDownCandies()
            }

            finally {
                mHandler.postDelayed(this, interval)
            }
        }
    }

    private fun startRepeat() {
        // Check if the game is ongoing before starting the repeat
        if (isGameOngoing) {
            repeatChecker.run()
        }
    }

    private fun createBoard() {
        val gridLayout = findViewById<GridLayout>(R.id.board)
        gridLayout.rowCount = noOfBlock
        gridLayout.columnCount = noOfBlock
        gridLayout.layoutParams.width = widthOfScreen
        gridLayout.layoutParams.height = widthOfScreen

        for (i in 0 until noOfBlock * noOfBlock) {
            val imageView = ImageView(this)

            imageView.id = i
            imageView.layoutParams = android.view.ViewGroup
                .LayoutParams(widthOfBlock, widthOfBlock)
            imageView.maxHeight = widthOfBlock
            imageView.maxWidth = widthOfBlock

            var random :Int = Math.floor(Math.random() * candies.size).toInt()

            imageView.setImageResource(candies[random])
            imageView.setTag(candies[random])

            candy.add(imageView)
            gridLayout.addView(imageView)
        }
    }
}
