package com.easy_locater.realtimeloc

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout


class ImageTouchSlider : RelativeLayout, View.OnTouchListener {

    private var mContext: Context? = null

    private var mImage: ImageView? = null
    private var mScreenWidthInPixel: Int = 0
    private var mScreenWidthInDp: Int = 0
    private var mDensity: Float = 0.toFloat()

    private val mPaddingInDp = 15
    private val mPaddingInPixel: Int = 0

    private var mLengthOfSlider: Int = 0

    private var mOnImageSliderChangedListener: OnImageSliderChangedListener? = null

    interface OnImageSliderChangedListener {
        fun onChanged()
    }

    constructor(context: Context) : super(context) {
        mContext = context
        createView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        createView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        mContext = context
        createView()
    }

    fun createView() {
        val inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater!!.inflate(R.layout.image_touch_slider, this, true)

        mImage = findViewById(R.id.slider) as ImageView
        mImage!!.setOnTouchListener(this)

        val manager = mContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager!!.getDefaultDisplay()
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        mDensity = getResources().getDisplayMetrics().density
        val dpWidth = outMetrics.widthPixels / mDensity
        mScreenWidthInPixel = outMetrics.widthPixels
        mScreenWidthInDp = (mScreenWidthInPixel / mDensity).toInt()

        mLengthOfSlider = mScreenWidthInDp - mPaddingInDp * 2
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val layoutParams = v.layoutParams as LayoutParams
        val width = v.width
        val xPos = event.getRawX()

        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> if (xPos < mScreenWidthInPixel.toFloat() - width.toFloat() - mPaddingInDp * mDensity && xPos > mPaddingInDp * mDensity) {
                mOnImageSliderChangedListener!!.onChanged()
                layoutParams.leftMargin = xPos.toInt() - width / 2
                mImage!!.setLayoutParams(layoutParams)
            }
            MotionEvent.ACTION_UP -> {
            }
            else -> {
            }
        }// You can add some clicked reaction here.

        return true
    }

    fun setOnImageSliderChangedListener(listener: OnImageSliderChangedListener) {
        mOnImageSliderChangedListener = listener
    }

}