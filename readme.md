# TurntableGame

## 介绍

`TurntableGame` 声明式构造转盘数据，实时动态变更，支持定义每个位置的样式，添加动画

## 效果图
### 大概演示
![image](https://github.com/zzechao/TurntableGame/blob/master/ex.gif)

## 用法
    根目录build.gradle中添加
    snapshot包
    maven("https://s01.oss.sonatype.org/service/local/repositories/snapshots/content/")
    release包
    maven("https://s01.oss.sonatype.org/service/local/repositories/releases/content/")

```groovy
    repositories {
        maven("https://s01.oss.sonatype.org/service/local/repositories/snapshots/content/")
        maven("https://s01.oss.sonatype.org/service/local/repositories/releases/content/")
    }
```

model build.gradle添加

```groovy
     implementation "io.github.zzechao:turntablelib:1.0.0-SNAPSHOT"
```

### 属性信息
```kotlin
    interface ITurntableBuilder {
        // 转盘背景
        var turntableBg: Int
    
        // 转盘中心背景
        var turntableNeedleBg: Int
    
        // 转盘指针图标
        var turntableNeedleIcon: Int
    
        // 转盘指针图标距离 中点 的上间距
        var turntableNeedleTop: Int
    
        // 转盘份数
        var numberPart: Int
    
        // 最少转动次数
        var mMinTimes: Int
    
        // 装懂时长
        var mDurationMillisecond: Long
    
        // 开始角度
        var startAngle: Float
    
        // 分割线颜色
        var dividingLineColor: Int
    
        // 分割线图标
        var dividingLineIcon: Int
    
        // 分割线高度
        var dividingLineSize: Float
    
        // 分割线长度
        var dividingLineWidth: Float
    
        // 是否显示分割线的序号
        var dividingNumberShow: Boolean
    
        // 插值器
        var interpolator: Interpolator
    
        // 图片加载类（图标或者图片加载返回上层构造）
        var photoLoader: (suspend (Any) -> Bitmap?)?
    
        // 插值器监听
        var animatorUpdateListener: AnimatorUpdateListener?
    
        // 每部分的样式构造
        fun partyChildBuild(child: IPartyChild.() -> Unit)
    
        // 构建
        fun build(finish: (() -> Unit)? = null)
    }
    
    /**
     * 每个item子布局view的绘制
     */
    interface IPartyChild {
        // 每部分view样式
        var partyChild: (Int) -> View?
    
        // 中心view样式
        var centerChild: View?
    }
```

### 开始转动
```kotlin
    /**
     * 开始转动
     * @param pos 转动位置
     * @param onStart 开始回调
     * @param onEnd 结束回调
     * @param onCancel 取消回调
     */
    fun startTurn(pos: Int, onStart: (() -> Unit)? = null, onEnd: (() -> Unit)? = null, onCancel: (() -> Unit)? = null) {}
```

#### 动画库初始化、设置例子，以及开始转动
```kotlin
    // 获取view
    val turn = findViewById<TurntableView>(R.id.view)
    // 转盘信息设置
    turn.setting {
        // 图片加载
        photoLoader = object : (suspend (Any) -> Bitmap?) { override suspend fun invoke(p1: Any): Bitmap? { return null } }
        // 插值器回调
        animatorUpdateListener = ValueAnimator.AnimatorUpdateListener {}
        // 转盘背景
        turntableBg = R.mipmap.bg03
        // 指针布局
        turntableNeedleBg = R.mipmap.bg02
        // 指针背景
        turntableNeedleIcon = R.mipmap.jiantou
        // 切割份数
        numberPart = 8
    
        // 每部分子view设置
        partyChildBuild {
            // 每部分view设置
            partyChild = { index:Int ->
                userViews.getOrNull(index) ?: LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_part_view, null)
            }
            
            // 中心view设置
            centerChild = LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_center_part, null)
        }
    }
    
    turn.startTurn(pos, onStart = {}, onEnd = {}, onCancel = {})
```