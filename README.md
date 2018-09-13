# 仿QQ头像裁剪  
  
    

![github](https://github.com/zhongruiAndroid/CropBitmap/blob/master/app/src/main/res/drawable/clipbitmap2.gif "github")  

## [Demo.apk下载](https://raw.githubusercontent.com/zhongruiAndroid/CropBitmap/master/app/sampledata/app.apk "apk文件")
    

| 属性           | 类型      | 说明                                                                  |
|----------------|-----------|-----------------------------------------------------------------------|
| maskColor      | color | 遮罩层颜色(默认#60000000)                                |
| borderColor     | color | 裁剪框内部边框颜色(默认白色)                               |
| radius        | dimension     | 裁剪框圆角(默认为最大值，裁剪框高度的一半)                                                        |
| maxScale    | float     | 图片最大放大倍数(默认3)                                                      |
| doubleClickScale    | float | 双击图片放大倍数(默认1.8,最大值不超过maxScale)

  
  


#### 设置Bitmap
```java
LikeQQCropView likeView=findViewById(R.id.likeView);

//如果传入的bitmap过大,此方法有OOM的可能
likeView.setBitmap(Bitmap bitmap);

//以下方法很安全,做了防止OOM的压缩

/**设置压缩之后的宽和高*/
likeView.setBitmap(多参);

/**设置压缩之后的高度,宽度自适应*/
likeView.setBitmapForHeight(多参);

/**设置压缩之后的宽度,高度自适应*/
likeView.setBitmapForWidth(多参);

/**设置压缩的缩放倍数(偶数),图片缩小一半传2,缩小4倍传4*/
likeView.setBitmapForScale(多参);
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
likeView.setMaxScale(1);

/**设置双击图片放大倍数*/
likeView.setDoubleClickScale(1);

/**获取裁剪框宽度*/
likeView.getClipWidth();
```

<br/>
<br/>

| 版本号 | [ ![Download](https://api.bintray.com/packages/zhongrui/mylibrary/CropBitmap/images/download.svg) ](https://bintray.com/zhongrui/mylibrary/CropBitmap/_latestVersion) |
|--------|----|
  



```gradle
implementation 'com.github:MyCropBitmap:版本号看上面'
```
