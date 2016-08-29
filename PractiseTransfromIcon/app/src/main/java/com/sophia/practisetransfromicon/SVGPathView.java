package com.sophia.practisetransfromicon;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by user90 on 2016/8/29.
 */
public class SVGPathView extends SurfaceView implements SurfaceHolder.Callback {

    //动画起始Path数据
    private ArrayList<SVGUtil.FragmentPath> svgStartDataList;
    //动画结束时的Path数据
    private ArrayList<SVGUtil.FragmentPath> svgEndDataList;

    private SurfaceHolder surfaceHolder;
    //用于SurfaceView显示的对象
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    //view的宽高
    private int mWidth;
    private int mHeight;
    //SVG path 里面的数据中参考的宽高
    private int mViewWidth;
    private int mViewHeight;
    //绘制线条的宽度
    private int mPaintWidth;

    //用于等比缩放
    private float widthFactor;
    private float heightFactor;
    private int mPaintColor;

    boolean isAnim;


    public SVGPathView(Context context) {
        super(context);
        init();
    }


    public SVGPathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SVGPathView);
        //读取布局文件设置的起始Path数据和结束Path数据
        String svgStartPath = ta.getString(R.styleable.SVGPathView_svg_start_path);
        String svgEndPath = ta.getString(R.styleable.SVGPathView_svg_end_path);
        //如果二者有一个没有设置，就讲没有设置的那个设定为已经设置的数据
        if (svgStartPath == null && svgEndPath != null) {
            svgStartPath = svgEndPath;
        } else if (svgStartPath != null && svgEndPath == null) {
            svgEndPath = svgStartPath;
        }
        //读取布局文件的配置
        mViewWidth = ta.getInteger(R.styleable.SVGPathView_svg_view_width, -1);
        mViewHeight = ta.getInteger(R.styleable.SVGPathView_svg_view_height, -1);
        mPaintWidth = ta.getInteger(R.styleable.SVGPathView_svg_paint_width, 5);
        mPaintColor = ta.getColor(R.styleable.SVGPathView_svg_color, Color.BLACK);

        SVGUtil svgUtil = SVGUtil.getInstance();
        //将原始数据做预处理
        ArrayList<String> svgStartStrList = svgUtil.extractSvgData(svgStartPath);
        ArrayList<String> svgEndStrList = svgUtil.extractSvgData(svgEndPath);

        //将经过预处理后的path数据，转为FragmentPath列表
        svgStartDataList = svgUtil.strListToFragList(svgStartStrList);
        svgEndDataList = svgUtil.strListToFragList(svgEndStrList);


        ta.recycle();
        init();

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

        //保存当前的View宽高
        mWidth = width;
        mHeight = height;
        //如果没有设置Path的参考宽高，默认设置未view的宽高
        if (mViewWidth <= 0) {
            mViewWidth = width;
        }
        if (mViewHeight <= 0) {
            mViewHeight = height;
        }
        //计算放缩倍数
        widthFactor = 1.f * width / mViewWidth;
        heightFactor = 1.f * height / mViewHeight;
        //创建Bitmap对象，用于绘制到屏幕中
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        //讲画笔绘制线条的宽度设置为经过放缩后的宽度
        mPaint.setStrokeWidth(mPaintWidth * widthFactor);
        //清屏
        clearCanvas();
        //讲清屏结果绘制到屏幕
        invalidate();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    //初始化
    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(mPaintColor);
    }



    //清屏
    private void clearCanvas() {
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawRect(0, 0, mWidth, mHeight, mPaint);

    }

    @Override
    public void invalidate() {
        super.invalidate();
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    public void startTransfrom() {
        if (!isAnim) {
            isAnim = true;
            ValueAnimator va = ValueAnimator.ofFloat(0, 1f);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatorFactor = (float) animation.getAnimatedValue();
                    Path path = SVGUtil.getInstance().parseFragList(svgStartDataList, svgEndDataList, widthFactor, heightFactor, animatorFactor);
                    drawPath(path);
                }
            });

            va.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAnim = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    isAnim = false;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            va.setDuration(1000).start();
        }
    }

    //开始绘制
    public void drawPath(Path path) {
        clearCanvas();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mPaintColor);


        mCanvas.drawPath(path, mPaint);
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

}
