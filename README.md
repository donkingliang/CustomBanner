# CustomBanner
Android轮播图控件，支持任何View的轮播，而不仅仅是图片(ImageView)，完全自定义的轮播指示器和轮播图样式，使用非常简单。下面是CustomBanner具体使用讲解：
### 1、引入依赖
在Project的build.gradle在添加以下代码
```
  allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```
在Module的build.gradle在添加以下代码
```
  dependencies {
            compile 'com.github.donkingliang:CustomBanner:1.1.0'
    }
```
### 2、编写布局
```xml
	<!-- 设置普通指示器 -->
	<com.donkingliang.banner.CustomBanner 
	    xmlns:app="http://schemas.android.com/apk/res-auto"
	    android:id="@+id/banner"
	    android:layout_width="match_parent"
	    android:layout_height="180dp"
	    app:indicatorStyle="ORDINARY"  //指示器类型 普通指示器
	    app:indicatorGravity="CENTER" //指示器的位置 有左。中、右三个值
	    app:indicatorSelectRes="@drawable/shape_point_select" //指示器的选中的样式
	    app:indicatorUnSelectRes="@drawable/shape_point_unselect" //指示器的未选中的样式
	    app:indicatorInterval="5dp"/> //指示器的点的间隔


	<!-- 设置数字指示器 -->
	<com.donkingliang.banner.CustomBanner
	    xmlns:app="http://schemas.android.com/apk/res-auto"
	    android:id="@+id/banner"
	    android:layout_width="match_parent"
	    android:layout_height="180dp"
	    app:indicatorStyle="NUMBER" //指示器类型 数字指示器
	    app:indicatorGravity="RIGHT"/> //指示器的位置 有左。中、右三个值


	<!-- 设置没有指示器 -->
	<com.donkingliang.banner.CustomBanner
	    xmlns:app="http://schemas.android.com/apk/res-auto"
	    android:id="@+id/banner"
	    android:layout_width="match_parent"
	    android:layout_height="180dp"
	    app:indicatorStyle="NONE"/> //指示器类型 没有指示器
```
### 3、CustomBanner的方法使用 
#### 1）、设置数据
```java
mBanner.setPages(new CustomBanner.ViewCreator<String>() {
    @Override
    public View createView(Context context, int position) {
        //这里返回的是轮播图的项的布局 支持任何的布局
        //position 轮播图的第几个项
        ImageView imageView = new ImageView(context);
        return imageView;
    }

    @Override
    public void updateUI(Context context, View view, int position, String data) {
     //在这里更新轮播图的UI
     //position 轮播图的第几个项
     //view 轮播图当前项的布局 它是createView方法的返回值
     //data 轮播图当前项对应的数据
     Glide.with(context).load(data).into((ImageView) view);
    }
}, beans);
```
轮播图的布局支持任何的布局，轮播图的数据类型也是支持任何的数据类型，这里只是用ImageView和String举例而已。
#### 2）、其他方法的使用
```java
//设置指示器类型，有普通指示器(ORDINARY)、数字指示器(NUMBER)和没有指示器(NONE)三种类型。
//这个方法跟在布局中设置app:indicatorStyle是一样的
mBanner.setIndicatorStyle(CustomBanner.IndicatorStyle.ORDINARY);

//设置两个点图片作为翻页指示器，只有指示器为普通指示器(ORDINARY)时有用。
//这个方法跟在布局中设置app:indicatorSelectRes、app:indicatorUnSelectRes是一样的。
//第一个参数是指示器的选中的样式，第二个参数是指示器的未选中的样式。
mBanner.setIndicatorRes(R.drawable.shape_point_select,R.drawable.shape_point_unselect)；
      
//设置指示器的指示点间隔，只有指示器为普通指示器(ORDINARY)时有用。
//这个方法跟在布局中设置app:indicatorInterval是一样的。
mBanner.setIndicatorInterval(20)

//设置指示器的方向。
//这个方法跟在布局中设置app:indicatorGravity是一样的。
mBanner.setIndicatorGravity(CustomBanner.IndicatorGravity.CENTER)

//设置轮播图自动滚动轮播，参数是轮播图滚动的间隔时间
//轮播图默认是不自动滚动的，如果不调用这个方法，轮播图将不会自动滚动。
mBanner.startTurning(3600);

//停止轮播图的自动滚动
mBanner.stopTurning();

//设置轮播图的滚动速度
mBanner.setScrollDuration(500);

//设置轮播图的点击事件
mBanner.setOnPageClickListener(new CustomBanner.OnPageClickListener<String>() {
    @Override
    public void onPageClick(int position, String str) {
     //position 轮播图的第几个项
     //str 轮播图当前项对应的数据
    }
});
```
以上是CustomBanner的主要常用方法，更多方法请查看源码。
#### 3）、CustomBanner的很多方法都支持方法连缀，比如以下的方法可以这样调用。
```java
 mBanner.setPages(参数, 参数).setIndicatorRes(参数, 参数).setIndicatorGravity(参数).startTurning(参数);
```
### 效果图 
![](https://github.com/donkingliang/CustomBanner/blob/master/%E6%BC%94%E7%A4%BA.gif) 

在我的博客中对该轮播图控件的实现和核心代码有详细的分析讲解，欢迎大家访问[我的博客](http://blog.csdn.net/u010177022)  
[Android轮播图控件的实现详解](http://blog.csdn.net/u010177022/article/details/61931488)
