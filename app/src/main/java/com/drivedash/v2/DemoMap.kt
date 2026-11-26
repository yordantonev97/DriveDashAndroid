package com.drivedash.v2
import android.content.Context
import android.graphics.*
import android.view.View
class DemoMap(context: Context): View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    init { contentDescription = "Демонстрационна карта, не е реална навигация" }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        canvas.save(); canvas.scale(w/602f,h/354f)
        canvas.drawColor(Color.rgb(27,43,48))
        p.style=Paint.Style.FILL; p.color=Color.rgb(36,62,55)
        canvas.drawRoundRect(0f,0f,180f,85f,18f,18f,p)
        canvas.drawRoundRect(390f,205f,602f,354f,18f,18f,p)
        canvas.drawRoundRect(240f,0f,475f,73f,18f,18f,p)
        fun road(vararg xy: Float) {
            val path=Path().apply { moveTo(xy[0],xy[1]); for(i in 2 until xy.size step 2) lineTo(xy[i],xy[i+1]) }
            p.style=Paint.Style.STROKE; p.strokeCap=Paint.Cap.ROUND; p.strokeJoin=Paint.Join.ROUND
            p.strokeWidth=13f; p.color=Color.rgb(52,73,80); canvas.drawPath(path,p)
            p.strokeWidth=1.3f; p.color=Color.rgb(104,124,130); canvas.drawPath(path,p)
        }
        road(-20f,150f,125f,125f,280f,147f,610f,80f)
        road(-20f,246f,130f,213f,305f,240f,610f,186f)
        road(70f,-20f,125f,125f,130f,213f,171f,370f)
        road(256f,-20f,280f,147f,305f,240f,342f,370f)
        road(476f,-20f,443f,120f,460f,215f,510f,370f)
        road(-20f,321f,130f,287f,305f,310f,610f,276f)
        road(-20f,52f,125f,73f,277f,62f,610f,25f)
        val path=Path().apply { moveTo(160f,350f); lineTo(130f,213f); lineTo(125f,125f); lineTo(280f,147f); lineTo(443f,120f); lineTo(470f,20f) }
        p.style=Paint.Style.STROKE; p.strokeWidth=17f; p.color=Color.rgb(13,40,85); canvas.drawPath(path,p)
        p.strokeWidth=10f; p.color=Color.rgb(105,170,255); canvas.drawPath(path,p)
        p.style=Paint.Style.FILL; p.color=Color.WHITE; canvas.drawCircle(131f,220f,19f,p)
        p.color=Color.rgb(105,170,255); canvas.drawCircle(131f,220f,16f,p)
        p.color=Color.WHITE; val arrow=Path().apply { moveTo(131f,208f); lineTo(139f,229f); lineTo(131f,225f); lineTo(123f,229f); close() }; canvas.drawPath(arrow,p)
        p.color=Color.rgb(20,89,76); canvas.drawRoundRect(12f,12f,318f,75f,14f,14f,p)
        p.color=Color.WHITE; p.typeface=Typeface.create("sans-serif",Typeface.BOLD); p.textSize=22f; p.textAlign=Paint.Align.LEFT
        canvas.drawText("ДЕМО КАРТА",26f,38f,p)
        p.typeface=Typeface.DEFAULT; p.textSize=16f; canvas.drawText("Не е реална навигация",26f,60f,p)
        p.color=Color.rgb(17,28,39); canvas.drawRoundRect(12f,292f,590f,342f,14f,14f,p)
        p.color=Color.WHITE; p.textSize=19f; p.textAlign=Paint.Align.CENTER; canvas.drawText("Няма активен маршрут",301f,323f,p)
        canvas.restore()
    }
}
