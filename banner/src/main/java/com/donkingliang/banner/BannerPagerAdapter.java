package com.donkingliang.banner;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.List;

public class BannerPagerAdapter<T> extends PagerAdapter {

    private Context mContext;
    private List<T> mData;
    private CustomBanner.ViewCreator<T> mCreator;
    private CustomBanner.OnPageClickListener<T> mOnPageClickListener;

    private SparseArray<View> views = new SparseArray<>();

    public BannerPagerAdapter(Context context, CustomBanner.ViewCreator<T> creator, List<T> data) {
        mContext = context;
        mCreator = creator;
        mData = data;
    }

    @Override
    public int getCount() {
        return mData == null || mData.isEmpty() ? 0 : mData.size() + 2;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        //Warning：不要在这里调用removeView
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        final int item = getActualPosition(position);

        View view = views.get(position);

        if (view == null) {
            view = mCreator.createView(mContext, item);
            views.put(position, view);
        }

        //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
        ViewParent vp = view.getParent();
        if (vp != null) {
            ViewGroup parent = (ViewGroup) vp;
            parent.removeView(view);
        }

        final T t = mData.get(item);

        mCreator.updateUI(mContext, view, item, t);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPageClickListener != null) {
                    mOnPageClickListener.onPageClick(item, t);
                }
            }
        });

        container.addView(view);
        return view;
    }

    private int getActualPosition(int position) {
        if (position == 0) {
            return mData.size() - 1;
        } else if (position == getCount() - 1) {
            return 0;
        } else {
            return position - 1;
        }
    }

    public void setOnPageClickListener(CustomBanner.OnPageClickListener l) {
        mOnPageClickListener = l;
    }

}
