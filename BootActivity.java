package com.win7lite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

// Windows 7 风格开机动画：四色光点汇聚成 Windows 徽标
public class BootActivity extends Activity {
    private int[] colors = {Color.rgb(242,80,34), Color.rgb(127,186,0), Color.rgb(0,164,239), Color.rgb(255,185,0)};
    private float progress = 0f;
    private Handler handler = new Handler();
    private Runnable ticker;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        final BootView view = new BootView();
        setContentView(view);

        ticker = new Runnable() {
            @Override public void run() {
                progress += 0.02f;
                view.invalidate();
                if (progress < 1.0f) {
                    handler.postDelayed(this, 24);
                } else {
                    // 动画结束，进入桌面
                    handler.postDelayed(new Runnable() {
                        @Override public void run() {
                            startActivity(new Intent(BootActivity.this, MainActivity.class));
                            finish();
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    }, 300);
                }
            }
        };
        handler.postDelayed(ticker, 200);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (handler != null && ticker != null) handler.removeCallbacks(ticker);
    }

    class BootView extends View {
        Paint p = new Paint();
        BootView(){ super(BootActivity.this); }
        @Override protected void onDraw(Canvas c) {
            c.drawColor(Color.BLACK);
            float cx = getWidth()/2f, cy = getHeight()/2f;
            float r = Math.min(getWidth(), getHeight()) * 0.16f;
            p.setAntiAlias(true);
            // 四个光点从远处向中心汇聚
            for (int i=0; i<4; i++) {
                double ang = Math.PI/4 + i*Math.PI/2; // 45,135,225,315
                float dist = (1-progress) * r * 3f;
                float x = cx + (float)(Math.cos(ang)*dist);
                float y = cy + (float)(Math.sin(ang)*dist);
                float sr = r * 0.5f * (0.4f + 0.6f*progress);
                p.setColor(colors[i]);
                p.setShadowLayer(20, 0, 0, colors[i]);
                c.drawCircle(x, y, sr, p);
            }
            p.clearShadowLayer();
            // 底部 "正在启动" 文字
            p.setColor(Color.WHITE);
            p.setTextSize(32);
            p.setTextAlign(Paint.Align.CENTER);
            c.drawText("正在启动 Windows 7", cx, cy + r*2.2f, p);
        }
    }
}
