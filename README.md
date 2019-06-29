
# 仿QQ头像裁剪  
 

   
    

![github](https://github.com/zhongruiAndroid/CropBitmap/blob/master/imagedirectory/clipbitmap2.gif "github")  

## [Demo.apk下载](https://raw.githubusercontent.com/zhongruiAndroid/CropBitmap/master/app/sampledata/app.apk "apk文件")
    

| 属性           | 类型      | 说明                                                                  |
|----------------|-----------|-----------------------------------------------------------------------|
| maskColor      | color | 遮罩层颜色(默认#60000000)                                |
| borderColor     | color | 裁剪框内部边框颜色(默认白色)                               |
| radius        | dimension     | 裁剪框圆角(默认为最大值，裁剪框高度的一半)                                                        |
| maxScale    | float     | 图片最大放大倍数(默认3)                                                      |
| doubleClickScale    | float | 双击图片放大倍数(默认1.8,最大值不超过maxScale)

  
```xml
<com.github.cropbitmap.LikeQQCropView
    android:id="@+id/likeView"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_weight="1"
    android:background="@color/white"
    />
```


#### 设置Bitmap
```java
LikeQQCropView likeView=findViewById(R.id.likeView);

//如果传入的bitmap过大,此方法有OOM的可能
likeView.setBitmap(Bitmap bitmap);

//以下方法很安全,做了防止OOM的压缩

/**设置压缩之后的宽和高*/
likeView.setBitmap(多参);

/**[推荐该方法]设置压缩之后的高度(宽度自适应)*/
likeView.setBitmapForHeight(多参);

/**[推荐该方法]设置压缩之后的宽度(高度自适应)*/
likeView.setBitmapForWidth(多参);

/**设置压缩的缩放倍数(偶数),图片缩小一半传2,缩小4倍传4*/
likeView.setBitmapForScale(多参);

/**setBitmap之后，千万要记得调用reset方法*/
/*注意!注意!注意!*/
likeView.reset();
```
#### 对Bitmap的操作
```java
/**水平翻转*/
likeView.horizontalFlip();

/**垂直翻转*/
likeView.verticalFlip();

/**垂直+水平翻转*/
likeView.verticalAndHorizontalFlip();

/**裁剪*/
likeView.clip();

/**图片位置重置*/
likeView.reset();

/**设置遮罩层*/
likeView.setMaskColor(color);

/**设置裁剪框内部边框颜色*/
likeView.setBorderColor(color);

/**设置裁剪框圆角*/
likeView.setRadius(radius);

/**设置图片最大放大倍数*/
likeView.setMaxScale(3);

/**设置双击图片放大倍数*/
likeView.setDoubleClickScale(1.8);

/**获取裁剪框宽度*/
likeView.getClipWidth();
```
<br/>

### 如果本库对您有帮助,还希望支付宝扫一扫下面二维码,你我同时免费获取奖励金(非常感谢 Y(^-^)Y)
![github](https://github.com/zhongruiAndroid/SomeImage/blob/master/image/small_ali.jpg?raw=true "github")  


| 最新版本号 | [ ![Download](https://api.bintray.com/packages/zhongrui/mylibrary/CropBitmap/images/download.svg) ](https://bintray.com/zhongrui/mylibrary/CropBitmap/_latestVersion) |
|--------|----|
  



```gradle
implementation 'com.github:MyCropBitmap:版本号看上面'
```  

#### 历史版本说明
- 1.0.4 解决图片缩放+翻转之后裁剪得到的bitmap宽高过大的问题
