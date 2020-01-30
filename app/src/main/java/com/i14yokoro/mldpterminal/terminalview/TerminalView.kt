package com.i14yokoro.mldpterminal.terminalview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.support.v4.text.HtmlCompat
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.i14yokoro.mldpterminal.EscapeSequence
import com.i14yokoro.mldpterminal.TerminalBuffer
import java.nio.charset.StandardCharsets
import kotlin.math.abs
import kotlin.math.ceil


/** TODO
 * カーソル移動
 * 入力処理
 */
class TerminalView : View{

    private var inputListener: InputListener? = null
    private var gestureListener: GestureListener? = null

    lateinit var termBuffer: TerminalBuffer

    var textSize: Int = 25

    private var terminalRenderer: TerminalRenderer = TerminalRenderer(textSize)
    var cursor = Cursor()
    lateinit var escapeSequence: EscapeSequence

    var screenColumnSize: Int = 0
    set(width) {
        field = width / terminalRenderer.fontWidth
    }
    var screenRowSize: Int = 0
    set(height) {
        field = (height-100) / terminalRenderer.fontHeight - 1
    }

    var oldY = 0

    private var isDisplaying = false        // 画面更新中はtrue

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)


    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = InputType.TYPE_NULL
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN
        return BaseInputConnection(this, false)
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        Log.e("TerminalView", "dispatch")

        if(gestureListener != null) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldY = event.rawY.toInt()
                    gestureListener?.onDown()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    gestureListener?.onMove()
                    if (oldY > event.rawY) {
                        scrollDown()
                    }
                    if (oldY < event.rawY) {
                        scrollUp()
                    }
                }
                else -> {
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val dispatchFirst = super.dispatchKeyEvent(event)

        if (event.action == KeyEvent.ACTION_UP){
            // 入力を通知
            if(inputListener != null) {
                inputListener?.onKey(event.unicodeChar.toChar())
            }
        }
        return dispatchFirst
    }

    override fun onDraw(canvas: Canvas) {
        if (!isDisplaying) {
            isDisplaying = true
            terminalRenderer.render(termBuffer, canvas, termBuffer.topRow, cursor.x, cursor.y, cursorIsInScreen())
            isDisplaying = false
        }
    }

    fun setInputListener(listener: InputListener){
        this.inputListener = listener
    }

    fun setGestureListener(listener: GestureListener){
        this.gestureListener = listener
    }

    fun scrollDown() {
        // TODO ここの処理はViewで
        if (termBuffer.totalLines > termBuffer.screenRowSize) {
            // 一番下の行までしか表示させない
            if (termBuffer.topRow + termBuffer.screenRowSize < termBuffer.totalLines) {
                //表示する一番上の行を１つ下に
                termBuffer.moveTopRow(1)
                if (cursorIsInScreen()) {
                    setEditable(true)
                    cursor.y = termBuffer.currentRow - termBuffer.topRow
                } else {
                    setEditable(false)
                }
                invalidate()
            }
        }
    }

    fun scrollUp() {
        // TODO ここの処理はViewで
        if (termBuffer.totalLines > termBuffer.screenRowSize) {
            //表示する一番上の行を１つ上に
            termBuffer.moveTopRow(-1)
            // カーソルが画面内にある
            if (cursorIsInScreen()) {
                setEditable(true)
                cursor.y = termBuffer.currentRow - termBuffer.topRow
            } else { //画面外
                setEditable(false)
            }
            invalidate()
        }
    }


    // TODO Viewに移動
    // エスケープシーケンスの処理
    fun ansiEscapeSequence(mode: Char, move: Int, hMove:Int) {

        when (mode) {
            'A' -> escapeSequence.moveUp(cursor, move)
            'B' -> escapeSequence.moveDown(cursor, move)
            'C' -> escapeSequence.moveRight(cursor, move)
            'D' -> escapeSequence.moveLeft(cursor, move)
            'E' -> escapeSequence.moveDownToRowLead(cursor, move)
            'F' -> escapeSequence.moveUpToRowLead(cursor, move)
            'G' -> escapeSequence.moveCursor(cursor, move)
            'H', 'f' -> escapeSequence.moveCursor(cursor, move, hMove)
            'J' -> escapeSequence.clearDisplay(cursor, move-1)
            'K' -> escapeSequence.clearRow(cursor, move-1)
            'S' -> escapeSequence.scrollNext(move)
            'T' -> escapeSequence.scrollBack(move)
            'm' -> {
                escapeSequence.selectGraphicRendition(move)
            }
            else -> {
            }
        }
    }

    // TODO Viewに移動
    // エスケープシーケンスの処理
    fun tecEscapeSequence(mode: Char) {

        when (mode) {
            'a' -> {
            }
            else -> {
            }
        }
    }

    fun cursorIsInScreen(): Boolean{
        return (termBuffer.topRow <= termBuffer.currentRow && termBuffer.currentRow <= termBuffer.topRow + termBuffer.screenRowSize - 1)
    }

    // TODO Viewに移動
    // 選択中の行番号を返す
    private val currentRow: Int
        get() = termBuffer.currentRow

    fun focusable() {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }

    // 画面の編集許可
    fun setEditable(editable: Boolean) {
        if (editable) {
            focusable()
        } else {
            isFocusable = false
        }
    }

    fun setDp(metrics: Float){
        terminalRenderer.titleBar = 20 * metrics.toInt()
    }

    inner class Cursor{
        var x: Int = 0
            set(x){
                field = if(x >= screenColumnSize){
                    screenColumnSize - 1
                } else {
                    if(x < 0){
                        0
                    } else {
                        x
                    }
                }
            }

        var y: Int = 0
            set(y) {
                field = if(y >= screenRowSize){
                    screenRowSize - 1
                } else {
                    if(y < 0){
                        0
                    } else {
                        y
                    }
                }
            }
    }

}