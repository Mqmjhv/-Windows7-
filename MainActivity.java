package com.win7lite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// Windows 7 桌面：壁纸 + 任务栏 + 开始球 + 开始菜单
public class MainActivity extends Activity {
    private LinearLayout startMenu;
    private boolean menuOpen = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        FrameLayout root = new FrameLayout(this);

        // 壁纸（渐变模拟 win7 蓝天）
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(30, 90, 160), Color.rgb(120, 190, 240), Color.rgb(220, 240, 250)});
        root.setBackground(bg);

        // 桌面图标区
        LinearLayout desktop = new LinearLayout(this);
        desktop.setOrientation(LinearLayout.VERTICAL);
        desktop.setPadding(dp(20), dp(24), dp(20), dp(20));

        desktop.addView(makeIcon("打开文件夹", "📁", () -> openFolder()));
        desktop.addView(makeIcon("打开网页", "🌐", () -> openBrowser()));
        desktop.addView(makeIcon("命令提示符", "🖥️", () -> openTerminal()));

        FrameLayout.LayoutParams dlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        root.addView(desktop, dlp);

        // 开始菜单
        startMenu = new LinearLayout(this);
        startMenu.setOrientation(LinearLayout.VERTICAL);
        startMenu.setBackgroundColor(Color.argb(235, 232, 240, 248));
        startMenu.setVisibility(View.GONE);
        startMenu.setPadding(dp(10), dp(8), dp(10), dp(8));

        TextView mt = new TextView(this);
        mt.setText("开始菜单");
        mt.setTextSize(18);
        mt.setTextColor(Color.rgb(30, 60, 100));
        mt.setGravity(Gravity.CENTER);
        startMenu.addView(mt);

        startMenu.addView(menuItem("📁  打开文件夹", () -> { hideMenu(); openFolder(); }));
        startMenu.addView(menuItem("🌐  打开网页", () -> { hideMenu(); openBrowser(); }));
        startMenu.addView(menuItem("🖥️  命令提示符", () -> { hideMenu(); openTerminal(); }));

        FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, dp(260), Gravity.BOTTOM);
        mlp.bottomMargin = dp(56);
        root.addView(startMenu, mlp);

        // 任务栏
        LinearLayout taskbar = new LinearLayout(this);
        taskbar.setGravity(Gravity.CENTER_VERTICAL);
        taskbar.setBackgroundColor(Color.argb(220, 26, 59, 92));
        taskbar.setPadding(dp(8), dp(6), dp(16), dp(6));

        // 开始球按钮
        TextView startBtn = new TextView(this);
        startBtn.setText("⏻");
        startBtn.setTextSize(26);
        startBtn.setTextColor(Color.WHITE);
        startBtn.setGravity(Gravity.CENTER);
        startBtn.setPadding(dp(14), dp(6), dp(14), dp(6));
        startBtn.setBackgroundColor(Color.argb(200, 28, 110, 196));
        startBtn.setOnClickListener(v -> toggleMenu());
        taskbar.addView(startBtn);

        // 空白填充
        TextView spacer = new TextView(this);
        spacer.setText("");
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, 1, 1f);
        taskbar.addView(spacer, sp);

        // 时钟
        TextView clock = new TextView(this);
        clock.setText(now());
        clock.setTextColor(Color.WHITE);
        clock.setTextSize(13);
        taskbar.addView(clock);
        final java.util.Timer t = new java.util.Timer();
        t.schedule(new java.util.TimerTask() {
            @Override public void run() {
                runOnUiThread(() -> clock.setText(now()));
            }
        }, 1000, 1000);

        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        root.addView(taskbar, tlp);

        setContentView(root);
    }

    private void toggleMenu() {
        if (menuOpen) hideMenu(); else showMenu();
    }

    private void showMenu() {
        startMenu.setVisibility(View.VISIBLE);
        startMenu.setAlpha(0f);
        startMenu.animate().alpha(1f).setDuration(200).start();
        menuOpen = true;
    }

    private void hideMenu() {
        startMenu.animate().alpha(0f).setDuration(150).withEndAction(() -> startMenu.setVisibility(View.GONE)).start();
        menuOpen = false;
    }

    private View menuItem(String label, Runnable action) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(16);
        tv.setTextColor(Color.rgb(20, 40, 70));
        tv.setPadding(dp(10), dp(12), dp(10), dp(12));
        tv.setOnClickListener(v -> action.run());
        return tv;
    }

    private View makeIcon(String label, String emoji, Runnable action) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(8), dp(10), dp(8), dp(10));
        box.setOnClickListener(v -> action.run());

        TextView e = new TextView(this);
        e.setText(emoji);
        e.setTextSize(40);
        e.setGravity(Gravity.CENTER);
        box.addView(e);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(13);
        l.setTextColor(Color.WHITE);
        l.setGravity(Gravity.CENTER);
        l.setShadowLayer(4, 0, 0, Color.BLACK);
        box.addView(l);
        return box;
    }

    private void openFolder() {
        try {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(i, 100);
        } catch (Exception e) {
            // 部分设备不支持 ACTION_OPEN_DOCUMENT_TREE，退回普通文件选择
            try {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                startActivityForResult(i, 101);
            } catch (Exception e2) {
                Toast.makeText(this, "无法打开文件管理器", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openBrowser() {
        startActivity(new Intent(this, BrowserActivity.class));
    }

    private void openTerminal() {
        startActivity(new Intent(this, TerminalActivity.class));
    }

    private String now() {
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm");
        return f.format(new java.util.Date());
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
