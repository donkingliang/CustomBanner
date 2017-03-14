package com.donkingliang.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.List;

public class CustomBanner<T> extends FrameLayout {

    private Context mContext;

    private ViewPager mBannerViewPager;
    private LinearLayout mIndicatorLayout;
    private BannerPagerAdapter<T> mAdapter;
    private ViewPagerScroller mScroller;
    private long mIntervalTime;

    private int mIndicatorSelectRes;
    private int mIndicatorUnSelectRes;
    private IndicatorGravity mIndicatorGravity = IndicatorGravity.CENTER_HORIZONTAL;

    private boolean isTurning;

    private OnPageClickListener mOnPageClickListener;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;

    private Handler mTimeHandler = new Handler();
    private Runnable mTurningTask = new Runnable() {
        @Override
        public void run() {
            if (isTurning && mBannerViewPager != null) {
                int page = mBannerViewPager.getCurrentItem() + 1;
                mBannerViewPager.setCurrentItem(page);
                mTimeHandler.postDelayed(mTurningTask, mIntervalTime);
            }
        }
    };

    public enum IndicatorGravity {
        LEFT, RIGHT, CENTER_HORIZONTAL
    }

    public CustomBanner(Context context) {
        super(context);
        init(context);
    }

    public CustomBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs);
        init(context);
    }

    public CustomBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(context, attrs);
        init(context);
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.custom_banner);
            int gravity = mTypedArray.getInt(R.styleable.custom_banner_indicatorGravity, 3);

            if (gravity == 1) {
                mIndicatorGravity = IndicatorGravity.LEFT;
            } else if (gravity == 2) {
                mIndicatorGravity = IndicatorGravity.RIGHT;
            } else if (gravity == 3) {
                mIndicatorGravity = IndicatorGravity.CENTER_HORIZONTAL;
            }
            mIndicatorSelectRes = mTypedArray.getResourceId(R.styleable.custom_banner_indicatorSelectRes, 0);
            mIndicatorUnSelectRes = mTypedArray.getResourceId(R.styleable.custom_banner_indicatorUnSelectRes, 0);
            mTypedArray.recycle();
        }
    }

    private void init(Context context) {
        mContext = context;
        addBannerViewPager(context);
        addIndicatorLayout(context);
    }

    private void addBannerViewPager(Context context) {
        mBannerViewPager = new ViewPager(context);
        mBannerViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetP) {
                if (!isMarginal(position) && mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageScrolled(getActualPosition(position), positionOffset, positionOffsetP);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (!isMarginal(position) && mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageSelected(getActualPosition(position));
                }
                updateIndicator();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                int position = mBannerViewPager.getCurrentItem();
                if (!isMarginal(position) && mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageScrollStateChanged(state);
                }
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (position == 0) {
                        mScroller.setSudden(true);
                        mBannerViewPager.setCurrentItem(mAdapter.getCount() - 2, true);
                        mScroller.setSudden(false);
                    } else if (position == mAdapter.getCount() - 1) {
                        mScroller.setSudden(true);
                        mBannerViewPager.setCurrentItem(1, true);
                        mScroller.setSudden(false);
                    }
                }
            }
        });

        replaceViewPagerScroll();
        this.addView(mBannerViewPager);
    }

    private void addIndicatorLayout(Context context) {
        mIndicatorLayout = new LinearLayout(context);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = analysisGravity(mIndicatorGravity);
        lp.setMargins(0, 0, 0, DensityUtils.dp2px(context, 8));
        mIndicatorLayout.setGravity(Gravity.CENTER);
        this.addView(mIndicatorLayout, lp);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (isTurning) {
            if (hasWindowFocus) {
                startTurning(mIntervalTime);
            } else {
                stopTurning();
                isTurning = true;
            }
        }
    }

    private boolean isMarginal(int position) {
        return position == 0 || position == getCount() + 1;
    }

    /**
     * 设置轮播图数据
     *
     * @param creator 创建和更新轮播图View的接口
     * @param data   轮播图数据
     * @return
     */
    public CustomBanner<T> setPages(ViewCreator<T> creator, List<T> data) {
        mAdapter = new BannerPagerAdapter<T>(mContext, creator, data);
        if (mOnPageClickListener != null) {
            mAdapter.setOnPageClickListener(mOnPageClickListener);
        }
        mBannerViewPager.setAdapter(mAdapter);
        if (data == null) {
            mIndicatorLayout.removeAllViews();
        } else {
            initIndicator(data.size());
        }
        setCurrentItem(0);
        updateIndicator();
        return this;
    }

    /**
     * 设置指示器资源
     *
     * @param selectRes   选中的效果资源
     * @param unSelectRes 未选中的效果资源
     * @return
     */
    public CustomBanner<T> setIndicatorRes(int selectRes, int unSelectRes) {
        mIndicatorSelectRes = selectRes;
        mIndicatorUnSelectRes = unSelectRes;
        updateIndicator();
        return this;
    }

    /**
     * 设置指示器方向
     *
     * @param gravity 指示器方向 左、中、右三种
     * @return
     */
    public CustomBanner<T> setIndicatorGravity(IndicatorGravity gravity) {
        if (mIndicatorGravity != gravity) {
            mIndicatorGravity = gravity;
            LayoutParams lp = (LayoutParams) mIndicatorLayout.getLayoutParams();
            lp.gravity = analysisGravity(gravity);
            mIndicatorLayout.setLayoutParams(lp);
        }
        return this;
    }

    public IndicatorGravity getIndicatorGravity() {
        return mIndicatorGravity;
    }

    private int analysisGravity(IndicatorGravity gravity) {
        if (gravity == IndicatorGravity.LEFT) {
            return Gravity.BOTTOM | Gravity.LEFT;
        } else if (gravity == IndicatorGravity.RIGHT) {
            return Gravity.BOTTOM | Gravity.RIGHT;
        } else {
            return Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        }
    }

    /**
     * 启动轮播
     *
     * @param intervalTime 轮播间隔时间
     * @return
     */
    public CustomBanner<T> startTurning(long intervalTime) {
        if (isTurning) {
            stopTurning();
        }
        isTurning = true;
        mIntervalTime = intervalTime;
        mTimeHandler.postDelayed(mTurningTask, mIntervalTime);
        return this;
    }

    /**
     * 停止轮播
     *
     * @return
     */
    public CustomBanner<T> stopTurning() {
        isTurning = false;
        mTimeHandler.removeCallbacks(mTurningTask);
        return this;
    }

    /**
     * 是否轮播
     *
     * @return
     */
    public boolean isTurning() {
        return isTurning;
    }

    /**
     * 获取轮播间隔时间
     *
     * @return
     */
    public long getIntervalTime() {
        return mIntervalTime;
    }

    public int getCount() {
        if (mAdapter == null || mAdapter.getCount() == 0) {
            return 0;
        }
        return mAdapter.getCount() - 2;
    }

    public CustomBanner setCurrentItem(int position) {
        if (position >= 0 && position < mAdapter.getCount()) {
            mBannerViewPager.setCurrentItem(position + 1);
        }
        return this;
    }

    public int getCurrentItem() {
        return getActualPosition(mBannerViewPager.getCurrentItem());
    }

    private int getActualPosition(int position) {
        if (mAdapter == null || mAdapter.getCount() == 0) {
            return -1;
        }

        if (position == 0) {
            return getCount() - 1;
        } else if (position == getCount() + 1) {
            return 0;
        } else {
            return position - 1;
        }
    }

    private void initIndicator(int count) {
        mIndicatorLayout.removeAllViews();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                ImageView imageView = new ImageView(mContext);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(DensityUtils.dp2px(mContext, 2), 0, DensityUtils.dp2px(mContext, 2), 0);
                mIndicatorLayout.addView(imageView, lp);
            }
        }
    }

    /**
     * 更新指示器
     */
    private void updateIndicator() {
        int count = mIndicatorLayout.getChildCount();
        int currentPage = getCurrentItem();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                ImageView view = (ImageView) mIndicatorLayout.getChildAt(i);
                if (i == currentPage) {
                    if (mIndicatorSelectRes != 0) {
                        view.setImageResource(mIndicatorSelectRes);
                    } else {
                        view.setImageBitmap(null);
                    }
                } else {
                    if (mIndicatorUnSelectRes != 0) {
                        view.setImageResource(mIndicatorUnSelectRes);
                    } else {
                        view.setImageBitmap(null);
                    }
                }
            }
        }
    }

    /**
     * 通过反射替换掉mBannerViewPager的mScroller属性。
     * 这样做是为了改变和控件ViewPager的滚动速度。
     */
    private void replaceViewPagerScroll() {
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            mScroller = new ViewPagerScroller(mContext,
                    new AccelerateInterpolator());
            field.set(mBannerViewPager, mScroller);
        } catch (Exception e) {
        }
    }

    /**
     * 设置轮播图的滚动速度
     *
     * @param scrollDuration
     */
    public CustomBanner<T> setScrollDuration(int scrollDuration) {
        mScroller.setScrollDuration(scrollDuration);
        return this;
    }

    public int getScrollDuration() {
        return mScroller.getScrollDuration();
    }

    public CustomBanner setOnPageChangeListener(ViewPager.OnPageChangeListener l) {
        mOnPageChangeListener = l;
        return this;
    }

    public CustomBanner<T> setOnPageClickListener(OnPageClickListener l) {
        if (mAdapter != null) {
            mAdapter.setOnPageClickListener(l);
        }

        mOnPageClickListener = l;
        return this;
    }

    public interface OnPageClickListener<T> {
        void onPageClick(int position, T t);
    }

    /**
     * 创建和更新轮播图View的接口
     *
     * @param <T>
     */
    public interface ViewCreator<T> {

        View createView(Context context, int position);

        void updateUI(Context context, View view, int position, T t);
    }
}
