package ru.netology.statsview

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.utils.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
):View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes
) {
    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5).toFloat()
    private var colors = emptyList<Int>()

    private var rotationAngle = 0f
    private var rotationAnimator: ValueAnimator? = null

    init {

        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            colors = listOf(
                getColor(R.styleable.StatsView_color1,generateRandomColor()),
                getColor(R.styleable.StatsView_color2,generateRandomColor()),
                getColor(R.styleable.StatsView_color3,generateRandomColor()),
                getColor(R.styleable.StatsView_color4,generateRandomColor())
            )
        }
        startAnimation()
    }

    private fun startAnimation() {
        progressAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
        }

        rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                rotationAngle = it.animatedValue as Float
                invalidate()
            }
        }

        AnimatorSet().apply {
            playTogether(progressAnimator, rotationAnimator)
            start()
        }
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    private val countedData: List<Float>
        get() {
            if (data.isEmpty()) return emptyList()
            val sum = data.sum()
            if (sum == 0f) return List(data.size) { 0f }
            return data.map { it / sum }
        }

    private var radius = 0F
    private var center = PointF(0F,0F)
    private var oval = RectF(0F,0F,0F,0F,)
    private var progress = 0F
    private var progressAnimator: ValueAnimator? = null

    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        strokeWidth = this@StatsView.lineWidth
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        color = Color.BLACK
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return
        val counted = countedData
        if (counted.isEmpty()) return

        canvas.save()

        canvas.rotate(rotationAngle, center.x, center.y)

        var startAngle = -90F

        counted.forEachIndexed { index, datum ->
            val angle = datum * 360F
            paint.color = colors.getOrElse(index) {generateRandomColor()}
            canvas.drawArc(oval, startAngle, angle * progress, false, paint)
            startAngle += angle
        }

        startAngle = -90F
        counted.forEachIndexed { index, datum ->
            val angle = datum * 360F
            paint.color = colors.getOrElse(index) {generateRandomColor()}
            canvas.drawArc(oval, startAngle, Float.MIN_VALUE, false, paint)
            startAngle += angle
        }

        canvas.restore()

        canvas.drawText(
            "%.2f%%".format(counted.sum() * 100 * progress),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }
    private fun update() {
        progressAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F
        progressAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
        }.also {
            it.start()
        }
    }

    private fun generateRandomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}


