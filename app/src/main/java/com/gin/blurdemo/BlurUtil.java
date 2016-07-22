package com.gin.blurdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.gin.blurdemo.helper.FastBlurHelper;
import com.gin.blurdemo.helper.RenderScriptBlurHelper;


/**
 * Created by wanglc on 16/7/19.
 */
public class BlurUtil {
    private static Activity mActivity;
    private FrameLayout.LayoutParams mBackground;
    private boolean mRenderScript = true;
    private float mDownScaleFactor;
    private int mBlurRadius;
    private int mAnimationDuration = 300;
    private boolean isAnimation = false;
   
    private ImageView mBlurredBackgroundView;
   
    private BlurAsyncTask mBluringTask;

    /**
     * 禁止构造
     */
    private BlurUtil() {
    }
    /**
     * 单例持有器
     */
    private static final class InstanceHolder {
        private static final BlurUtil INSTANCE = new BlurUtil();
    }

    /**
     * 获得单例
     *
     * @return
     */
    public static BlurUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public BlurUtil activity(Activity activity) {
        this.mActivity = activity;
        return this;
    }
    /**
     * 支持动画效果
     *
     * @return
     */
    public BlurUtil animation(boolean isAnimation) {
        this.isAnimation = isAnimation;
        return this;
    }

    /**
     * 使用RenderScript
     * @param useRenderScript
     *
     * @return
     */
    public BlurUtil renderscript(boolean useRenderScript) {
        this.mRenderScript = useRenderScript;
        return this;
    }

    public BlurUtil blurRadius(int radius) {
        this.mBlurRadius = radius;
        return this;
    }


    public BlurUtil scaleFactor(float facotor) {
        this.mDownScaleFactor= facotor;
        return this;
    }

    private void removeBlurredView() {
        if (mBlurredBackgroundView != null) {
            ViewGroup parent = (ViewGroup) mBlurredBackgroundView.getParent();
            if (parent != null) {
                parent.removeView(mBlurredBackgroundView);
            }
            mBlurredBackgroundView = null;
        }
    }

    /**
     * 退出时必须调用，取消模糊背景
     * 
     */
    public void dismiss() {
        if (mBluringTask != null) {
            mBluringTask.cancel(true);
        }
        if (mBlurredBackgroundView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if(isAnimation) {
                    mBlurredBackgroundView.animate()//
                            .alpha(0f)//
                            .setDuration(mAnimationDuration)//
                            .setInterpolator(new AccelerateInterpolator())//
                            .setListener(new AnimatorListenerAdapter() {//
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            removeBlurredView();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            removeBlurredView();
                        }
                    }).start();
                }else{
                    removeBlurredView();
                }
            } else {
                removeBlurredView();
            }
        }
        mBluringTask = null;
        mActivity = null;
    }


    /**
     * 
     */
    public void show() {
        if (mBlurRadius <= 0) {
            mBlurRadius = 8;
        }
        if (mDownScaleFactor <= 0.0f) {
            mDownScaleFactor = 4.0f;
        }
        mActivity.getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mActivity.getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                mBluringTask = new BlurAsyncTask();
                mBluringTask.execute();
                return false;
            }
        });
    }

    /**
     * Blur the given bitmap and add it to the activity.
     *
     * @param bkg
     *         should be a bitmap of the background.
     * @param view
     *         background view.
     */
    private void blur(Bitmap bkg, View view) {
        //define layout params to the previous imageView in order to match its parent
        mBackground = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        //overlay used to build scaled preview and blur background
        Bitmap overlay;
        final int topOffset = 0;
        //        // evaluate bottom or right offset due to navigation bar.
        int bottomOffset = 0;
        int rightOffset = 0;
        //add offset to the source boundaries since we don't want to blur actionBar pixels
        Rect srcRect = new Rect(0, topOffset, bkg.getWidth() - rightOffset, bkg.getHeight() - bottomOffset);

        //in order to keep the same ratio as the one which will be used for rendering, also
        //add the offset to the overlay.
        double height = Math.ceil((view.getHeight() - topOffset - bottomOffset) / mDownScaleFactor);
        double width = Math.ceil(((view.getWidth() - rightOffset) * height / (view.getHeight() - topOffset - bottomOffset)));

        // Render script doesn't work with RGB_565
        if (mRenderScript) {
            overlay = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        } else {
            overlay = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.RGB_565);
        }
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || mActivity instanceof ActionBarActivity || mActivity instanceof AppCompatActivity) {
                //add offset as top margin since actionBar height must also considered when we display
                // the blurred background. Don't want to draw on the actionBar.
                mBackground.setMargins(0, 0, 0, 0);
                mBackground.gravity = Gravity.TOP;
            }
        } catch (NoClassDefFoundError e) {
            // no dependency to appcompat, that means no additional top offset due to actionBar.
            mBackground.setMargins(0, 0, 0, 0);
        }
        //scale and draw background view on the canvas overlay
        Canvas canvas = new Canvas(overlay);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);

        //build drawing destination boundaries
        final RectF destRect = new RectF(0, 0, overlay.getWidth(), overlay.getHeight());

        //draw background from source area in source background to the destination area on the overlay
        canvas.drawBitmap(bkg, srcRect, destRect, paint);

        //apply fast blur on overlay
        if (mRenderScript) {
            overlay = RenderScriptBlurHelper.doBlur(overlay, mBlurRadius, true, mActivity);
        } else {
            overlay = FastBlurHelper.doBlur(overlay, mBlurRadius, true);
        }
        //set bitmap in an image view for final rendering
        mBlurredBackgroundView = new ImageView(mActivity);
        mBlurredBackgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        BitmapDrawable drawable = new BitmapDrawable(mActivity.getResources(), overlay);
        mBlurredBackgroundView.setImageDrawable(drawable);
    }

    
    /**
     * Async task used to process blur out of ui thread
     */
    private class BlurAsyncTask extends AsyncTask<Void, Void, Void> {

        private Bitmap mBackground;
        private View mBackgroundView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mBackgroundView = mActivity.getWindow().getDecorView();

            //retrieve background view, must be achieved on ui thread since
            //only the original thread that created a view hierarchy can touch its views.

            Rect rect = new Rect();
            mBackgroundView.getWindowVisibleDisplayFrame(rect);
            mBackgroundView.destroyDrawingCache();
            mBackgroundView.setDrawingCacheEnabled(true);
            mBackgroundView.buildDrawingCache(true);
            mBackground = mBackgroundView.getDrawingCache(true);

            /**
             * After rotation, the DecorView has no height and no width. Therefore
             * .getDrawingCache() return null. That's why we  have to force measure and layout.
             */
            if (mBackground == null) {
                mBackgroundView.measure(View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY));
                mBackgroundView.layout(0, 0, mBackgroundView.getMeasuredWidth(), mBackgroundView.getMeasuredHeight());
                mBackgroundView.destroyDrawingCache();
                mBackgroundView.setDrawingCacheEnabled(true);
                mBackgroundView.buildDrawingCache(true);
                mBackground = mBackgroundView.getDrawingCache(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            //process to the blue
            if (!isCancelled()) {
                blur(mBackground, mBackgroundView);
            } else {
                return null;
            }
            //clear memory
            mBackground.recycle();
            return null;
        }

        @Override
        @SuppressLint("NewApi")
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mBackgroundView.destroyDrawingCache();
            mBackgroundView.setDrawingCacheEnabled(false);

            mActivity.getWindow().addContentView(mBlurredBackgroundView, BlurUtil.this.mBackground);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                if (isAnimation) {

                    mBlurredBackgroundView.setAlpha(0f);
                    mBlurredBackgroundView.animate()//
                            .alpha(1f)//
                            .setDuration(mAnimationDuration)//
                            .setInterpolator(new LinearInterpolator())
                            .start();
                }
            }
            mBackgroundView = null;
            mBackground = null;
        }
    }
}
