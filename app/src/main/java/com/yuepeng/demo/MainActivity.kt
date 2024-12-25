package com.yuepeng.demo

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yuepeng.demo.ui.GameView
import com.yuepeng.demo.ui.GameView.GameResultCallback
import com.yuepeng.demo.utils.hideKeyboard

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var inputRow: EditText
    private lateinit var inputCol: EditText
    private lateinit var gameStatus: TextView
    private lateinit var gameLabel: TextView
    private lateinit var revealButton: Button
    private lateinit var restartButton: Button

    private var gameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取视图中的组件
        gameView = findViewById(R.id.gameBoard)
        inputRow = findViewById(R.id.inputRow)
        inputCol = findViewById(R.id.inputCol)
        gameStatus = findViewById(R.id.game_status)
        gameLabel = findViewById(R.id.game_label)
        revealButton = findViewById(R.id.revealButton)
        restartButton = findViewById(R.id.restartButton)

        // 设置按钮点击事件
        revealButton.setOnClickListener {
            handleRevealButtonClick()
        }

        restartButton.setOnClickListener {
            restartGame()
        }
        gameView.resultCallback = object : GameResultCallback {
            override fun onGameEnd() {
                restartButton.visibility = View.VISIBLE
                revealButton.visibility = View.GONE
                gameStatus.visibility=View.VISIBLE
                gameStatus.text = "Game Over"
            }

            override fun onGameComplete() {
                gameStatus.visibility=View.VISIBLE
                revealButton.visibility = View.GONE
                restartButton.visibility = View.VISIBLE
                gameStatus.text = "Congratulations! You Win!"
            }
        }
        initGameBoard()
    }

    // 初始化游戏面板
    private fun initGameBoard() {
        // 初始化游戏状态
        gameStatus.text = ""
        restartButton.visibility = View.GONE
        gameStatus.visibility = View.INVISIBLE
        gameView.generateMines() //随机生成地雷开始游戏
    }

    private fun handleRevealButtonClick() {
        gameView.hideKeyboard(this)
        if (gameOver) {
            Toast.makeText(this, "游戏已结束，请点击重新开始", Toast.LENGTH_SHORT).show()
            return
        }


        // 获取用户输入的坐标
        val rowInput = inputRow.text.toString()
        val colInput = inputCol.text.toString()
        inputRow.text.clear()
        inputCol.text.clear()
        if (rowInput.isEmpty() || colInput.isEmpty()) {
            Toast.makeText(this, "请输入有效的坐标", Toast.LENGTH_SHORT).show()
            return
        }

        val row = rowInput.toInt() - 1
        val col = colInput.toInt() - 1

        if (row !in 0 until gameView.rowSize || col !in 0 until gameView.columnSize) {
            Toast.makeText(this, "坐标超出范围", Toast.LENGTH_SHORT).show()

            return
        }

        // 检查该位置是否是地雷
        gameView.checkGameResult(row, col)

    }

    private fun restartGame() {
        // 重置游戏状态
        gameView.hideKeyboard(this)
        gameOver = false
        gameView.resetGame()
        inputRow.text.clear()
        inputCol.text.clear()
        gameStatus.text = ""
        restartButton.visibility = View.GONE
        revealButton.visibility = View.VISIBLE
        gameStatus.visibility=View.INVISIBLE

    }


}
