package com.yuepeng.demo.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.util.AttributeSet
import com.yuepeng.demo.utils.hideKeyboard
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint: Paint = Paint().apply {
        color = Color.BLACK  // 默认网格线颜色
        strokeWidth = 2f  // 设置线条的宽度
        isAntiAlias = true  // 启用抗锯齿效果
    }

    private val rect: RectF = RectF()  // 用于复用的矩形对象

    val rowSize = 10  // 网格的行数
    val columnSize = 10  // 网格的列数
    var cellSize: Float = 0f  // 每个单元格的大小

    // 存储已点击的单元格
    val revealed = mutableSetOf<Pair<Int, Int>>()
    // 存储地雷的位置
    var mines = mutableSetOf<Pair<Int, Int>>()
    // 游戏状态，标记是否点击了地雷
    var gameOver = false
    var resultCallback: GameResultCallback? = null

    // 用于存储游戏的状态
    var totalSafeCells = 0  // 记录总的非地雷单元格数

    // 游戏结束回调接口
    interface GameResultCallback {
        fun onGameEnd()  // 游戏结束
        fun onGameComplete()  // 游戏通关
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 确保网格是正方形，取屏幕宽度和高度中的较小值
        val size = Math.min(w, h) // 获取屏幕的最大正方形边长
        cellSize = size / Math.max(rowSize, columnSize).toFloat() // 根据最大值计算每个格子的大小
    }

    // 随机生成地雷
    fun generateMines() {
        mines.clear()

        // 随机生成地雷数量，范围为 1 到最大格子数 - 1
        val numberOfMines = Random.nextInt(1, rowSize * columnSize)

        while (mines.size < numberOfMines) {  // 确保地雷数量为固定数量
            val x = Random.nextInt(0, rowSize)
            val y = Random.nextInt(0, columnSize)
            mines.add(Pair(x, y))
        }

        // 计算总的非地雷单元格数
        totalSafeCells = rowSize * columnSize - mines.size
    }

    // 重置游戏
    fun resetGame() {
        gameOver = false
        revealed.clear()
        generateMines()  // 重新生成地雷
        invalidate()  // 重新绘制视图
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 获取视图的宽高
        val gridWidth = width
        val gridHeight = height

        // 计算正方形网格的偏移量，使其居中
        val offsetX = (gridWidth - cellSize * columnSize) / 2
        val offsetY = (gridHeight - cellSize * rowSize) / 2

        // 绘制网格单元格
        for (i in 0 until rowSize) {
            for (j in 0 until columnSize) {
                // 计算每个单元格的位置
                val x = offsetX + i * cellSize
                val y = offsetY + j * cellSize

                // 判断该单元格是否已点击
                val isRevealed = revealed.contains(i to j)
                val isMine = mines.contains(i to j)

                // 根据单元格的状态决定颜色
                paint.color = when {
                    isRevealed && isMine -> Color.RED  // 地雷，显示红色
                    isRevealed -> Color.GREEN  // 非地雷，显示绿色
                    else -> Color.WHITE  // 未点击，显示白色
                }

                // 设置矩形区域并绘制单元格背景
                rect.set(x, y, x + cellSize, y + cellSize)
                canvas.drawRect(rect, paint)
            }
        }

        // 绘制网格线
        paint.color = Color.BLACK  // 设置网格线的颜色
        for (i in 0..rowSize) {
            val x = offsetX + i * cellSize
            val y = offsetY + i * cellSize

            // 绘制水平线
            canvas.drawLine(offsetX, y, offsetX + cellSize * columnSize, y, paint)
            // 绘制垂直线
            canvas.drawLine(x, offsetY, x, offsetY + cellSize * rowSize, paint)
        }

        // 在游戏结束时，显示所有地雷
        if (gameOver) {
            paint.color = Color.RED  // 设置为红色绘制地雷

            for (mine in mines) {
                val x = offsetX + mine.first * cellSize
                val y = offsetY + mine.second * cellSize
                // 设置矩形区域并绘制地雷位置
                rect.set(x, y, x + cellSize, y + cellSize)
                canvas.drawRect(rect, paint)  // 绘制地雷位置
            }
        }
    }

    // 处理用户点击事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        hideKeyboard(this.context) // 点击时候自动隐藏焦点键盘
        if (gameOver) return true  // 游戏结束时，不处理点击事件

        if (event.action == MotionEvent.ACTION_DOWN) {
            // 获取点击的坐标
            val x = event.x
            val y = event.y

            // 计算点击的网格单元的行列
            val col = ((x - (width - cellSize * columnSize) / 2) / cellSize).toInt()
            val row = ((y - (height - cellSize * rowSize) / 2) / cellSize).toInt()

            // 确保点击在合法的网格内
            if (col in 0 until columnSize && row in 0 until rowSize) {
                // 调用检查游戏结果的函数
                if (checkGameResult(col, row)) return true
            }
        }
        return true
    }

    // 检查游戏结果
    fun checkGameResult(col: Int, row: Int): Boolean {
        // 如果点击的格子是地雷，结束游戏
        if (Pair(col, row) in mines) {
            revealed.add(Pair(col, row))  // 显示点击的地雷
            resultCallback?.onGameEnd()
            gameOver = true  // 结束游戏
            invalidate()  // 重新绘制视图
            return true
        }

        // 否则标记为已点击
        revealed.add(Pair(col, row))

        // 检查游戏是否完成：如果揭示的非地雷单元格数量 == 总的非地雷单元格数量
        if (revealed.size == totalSafeCells) {
            gameOver = true  // 完成游戏
            resultCallback?.onGameComplete()
        }

        invalidate()  // 重新绘制视图
        return false
    }
}
