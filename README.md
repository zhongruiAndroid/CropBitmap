# 仿QQ头像裁剪
![github](https://github.com/zhongruiAndroid/Progress/blob/master/app/src/main/res/drawable/demo.gif "github")


| 属性           | 类型      | 说明                                                                  |
|----------------|-----------|-----------------------------------------------------------------------|
| maskColor      | color | 遮罩层颜色                                |
| borderColor     | color | 裁剪框内部边框颜色(默认白色)                               |
| radius        | dimension     | 裁剪框圆角(默认为最大值，裁剪框高度的一半)                                                        |
| maxScale    | float     | 图片最大放大倍数(默认3)                                                      |
| doubleClickScale    | float | 双击图片放大倍数(默认1.8,最大值不超过maxScale)


#### 设置Bitmap
```java
LikeQQCropView likeView=findViewById(R.id.likeView);

//如果传入的bitmap过大,此方法有OOM的可能
likeView.setBitmap(Bitmap bitmap)
//以下方法很安全,做了防止OOM的压缩
likeView.setBitmap(多参)
likeView.setBitmapForHeight(多参)
likeView.setBitmapForWidth(多参)
likeView.setBitmapForScale(多参)
```
#### 对Bitmap的操作
```java
/**水平翻转*/
likeView.horizontalFlip()
/**垂直翻转*/
likeView.verticalFlip()
/**垂直+水平翻转*/
likeView.verticalAndHorizontalFlip()
/**裁剪*/
likeView.clip()
/**图片位置重置*/
likeView.reset()
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
```

<br/>

[ ![Download](https://api.bintray.com/packages/zhongrui/mylibrary/MyProgress/images/download.svg) ](https://bintray.com/zhongrui/mylibrary/MyProgress/_latestVersion)<--版本号
<br/>
```gradle
compile 'com.github:MyProgress:版本号看上面'
```
