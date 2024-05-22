# TurntableGame

## 介绍

`TurntableGame` 声明式构造转盘数据，实时动态变更，支持定义每个位置的样式，添加动画

## 效果图

### 大概演示
![image](https://github.com/zzechao/TurntableGame/blob/master/l0sbx-utjoe.gif)


### 动画库初始化

```kotlin
    val turn = findViewById<TurntableView>(R.id.view)
    turn.setting {
        photoLoader = object : (suspend (Any) -> Bitmap?) {
            override suspend fun invoke(p1: Any): Bitmap? {
                return if (p1 is Int) {
                    BitmapFactory.decodeResource(this@MainActivity.resources, p1)
                } else {
                    null
                }
            }
        }
    
        animatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
            //imageView.rotation = it.animatedValue as Float
        }
    
        //turntableBg = R.mipmap.bg03
        turntableNeedleBg = R.mipmap.bg02
        turntableNeedleIcon = R.mipmap.jiantou
        numberPart = 8
    
        partyChildBuild {
            partyChild = { index ->
                userViews.getOrNull(index) ?: LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_part_view, null).apply {
                    Log.i("zzc", "setting index:$index this:$this")
                    this.findViewById<TextView>(R.id.index).text = "$index"
                    this.setOnClickListener {
                        position = index
                        Log.i("zzc", "click index:$index it:$it")
                        it ?: return@setOnClickListener
                        userViews.add(it)
                        userSize++
                        this.findViewById<ImageView>(R.id.head).setImageResource(R.mipmap.head)
                        //Toast.makeText(this@MainActivity, "index:$index", Toast.LENGTH_LONG).show()
                    }
                }
            }
    
            centerChild = LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_center_part, null).apply {
                this.findViewById<TextView>(R.id.size).text = "x$giftSize"
                this.setOnClickListener {
                    this.findViewById<ImageView>(R.id.gift).setImageResource(R.mipmap.head)
                    giftSize++
                    this.findViewById<TextView>(R.id.size).text = "x$giftSize"
    
                    val start = turn.getPartyView(position)
                    val end = it
                    val startwidth = start?.measuredWidth ?: 0
                    val startheight = start?.measuredHeight ?: 0
                    val starttop = start?.top ?: 0
                    val startleft = start?.left ?: 0
    
                    val endwidth = end?.measuredWidth ?: 0
                    val endheight = end?.measuredHeight ?: 0
                    val endtop = end?.top ?: 0
                    val endleft = end?.left ?: 0
    
                    turn.showAnim(
                        PointF(startleft + startwidth / 2f, starttop + startheight / 2f),
                        PointF(endleft + endwidth / 2f, endtop + endheight / 2f)
                    ) {
                        BitmapFactory.decodeResource(this@MainActivity.resources, R.mipmap.head)
                    }
                }
            }
        }
    }
```