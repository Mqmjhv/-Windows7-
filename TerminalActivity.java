package com.win7lite;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// 命令提示符（cmd 风格，可拖动 + 可缩放窗口）· 内部运行真正的 Linux shell
public class TerminalActivity extends Activity {
    private TextView output;
    private ScrollView scroll;
    private EditText input;

    private FrameLayout window;       // 可定位的窗口根容器
    private LinearLayout titleBar;    // 标题栏（用于拖动）

    // 拖动状态
    private float lastTouchX, lastTouchY;
    // 缩放状态
    private boolean resizing = false;
    private float lastResizeX, lastResizeY;
    private int winW, winH;           // 当前窗口宽高

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        FrameLayout screen = new FrameLayout(this);
        screen.setBackgroundColor(Color.argb(60, 0, 0, 0)); // 半透明遮罩，突出窗口

        // ==== 窗口容器（可拖动/缩放）====
        window = new FrameLayout(this);
        window.setBackgroundColor(Color.rgb(60, 60, 60)); // 窗口边框

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        // ---- 标题栏 ----
        titleBar = new LinearLayout(this);
        titleBar.setGravity(Gravity.CENTER_VERTICAL);
        titleBar.setBackgroundColor(Color.rgb(0, 84, 153));
        titleBar.setPadding(dp(8), dp(6), dp(4), dp(6));
        titleBar.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (!resizing) handleDrag(e);
                return true;
            }
        });

        TextView title = new TextView(this);
        title.setText("C:\\Windows\\system32\\cmd.exe");
        title.setTextColor(Color.WHITE);
        title.setTextSize(13);
        title.setTypeface(Typeface.DEFAULT);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, dp(32), 1f);
        tp.gravity = Gravity.CENTER_VERTICAL;
        titleBar.addView(title, tp);

        TextView minBtn = makeBtn("—");
        minBtn.setOnClickListener(v -> finish());
        titleBar.addView(minBtn);
        TextView maxBtn = makeBtn("▢");
        maxBtn.setOnClickListener(v -> toggleMaximize());
        titleBar.addView(maxBtn);
        TextView closeBtn = makeBtn("✕");
        closeBtn.setOnClickListener(v -> finish());
        titleBar.addView(closeBtn);
        content.addView(titleBar);

        // ---- 终端主体 ----
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setBackgroundColor(Color.BLACK);
        body.setPadding(dp(2), dp(2), dp(2), dp(2));

        scroll = new ScrollView(this);
        output = new TextView(this);
        output.setTextColor(Color.rgb(192, 192, 192));
        output.setTypeface(Typeface.MONOSPACE);
        output.setTextSize(13);
        output.setPadding(dp(8), dp(8), dp(8), dp(8));
        output.setText("Microsoft Windows [版本 6.1.7601]\n版权所有 (c) 2009 Microsoft Corporation。保留所有权利。\n\nC:\\Users\\Administrator> \n");
        scroll.addView(output);
        body.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // 输入行
        LinearLayout inRow = new LinearLayout(this);
        inRow.setGravity(Gravity.CENTER_VERTICAL);
        inRow.setBackgroundColor(Color.BLACK);
        inRow.setPadding(dp(6), dp(2), dp(6), dp(6));

        TextView prompt = new TextView(this);
        prompt.setText("C:\\Users\\Administrator> ");
        prompt.setTextColor(Color.rgb(192, 192, 192));
        prompt.setTypeface(Typeface.MONOSPACE);
        prompt.setTextSize(13);
        inRow.addView(prompt);

        input = new EditText(this);
        input.setTextColor(Color.rgb(192, 192, 192));
        input.setTypeface(Typeface.MONOSPACE);
        input.setTextSize(13);
        input.setBackgroundColor(Color.BLACK);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        inRow.addView(input, new LinearLayout.LayoutParams(0, dp(40), 1f));

        input.setOnEditorActionListener((v, actionId, event) -> {
            runCmd(input.getText().toString());
            return true;
        });
        input.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                runCmd(input.getText().toString());
                return true;
            }
            return false;
        });

        body.addView(inRow);
        content.addView(body);
        window.addView(content);

        // ---- 右下角缩放手柄 ----
        TextView resizeHandle = new TextView(this);
        resizeHandle.setText("▦");
        resizeHandle.setTextSize(18);
        resizeHandle.setTextColor(Color.rgb(160, 160, 160));
        resizeHandle.setGravity(Gravity.CENTER);
        resizeHandle.setPadding(dp(6), dp(6), dp(6), dp(6));
        FrameLayout.LayoutParams rhp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.END);
        resizeHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent e) {
                handleResize(e);
                return true;
            }
        });
        window.addView(resizeHandle, rhp);

        // 初始窗口大小（约屏幕 85% 宽、70% 高）
        winW = (int)(getResources().getDisplayMetrics().widthPixels * 0.9f);
        winH = (int)(getResources().getDisplayMetrics().heightPixels * 0.72f);
        FrameLayout.LayoutParams wlp = new FrameLayout.LayoutParams(winW, winH);
        wlp.setMargins(dp(10), dp(40), dp(10), dp(40));
        screen.addView(window, wlp);

        // 点击遮罩空白处关闭窗口
        screen.setOnClickListener(v -> finish());

        setContentView(screen);
    }

    private TextView makeBtn(String txt) {
        TextView t = new TextView(this);
        t.setText(txt);
        t.setTextSize(15);
        t.setTextColor(Color.WHITE);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(11), dp(2), dp(11), dp(2));
        return t;
    }

    private void toggleMaximize() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) window.getLayoutParams();
        int fullW = getResources().getDisplayMetrics().widthPixels;
        int fullH = getResources().getDisplayMetrics().heightPixels;
        if (winW >= (int)(fullW * 0.98f) && winH >= (int)(fullH * 0.98f)) {
            // 还原
            winW = (int)(fullW * 0.9f);
            winH = (int)(fullH * 0.72f);
            lp.setMargins(dp(10), dp(40), dp(10), dp(40));
        } else {
            winW = (int)(fullW * 0.98f);
            winH = (int)(fullH * 0.98f);
            lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        }
        lp.width = winW;
        lp.height = winH;
        window.setLayoutParams(lp);
    }

    private void handleDrag(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = e.getRawX();
                lastTouchY = e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = e.getRawX() - lastTouchX;
                float dy = e.getRawY() - lastTouchY;
                window.setTranslationX(window.getTranslationX() + dx);
                window.setTranslationY(window.getTranslationY() + dy);
                lastTouchX = e.getRawX();
                lastTouchY = e.getRawY();
                break;
        }
    }

    private void handleResize(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                resizing = true;
                lastResizeX = e.getRawX();
                lastResizeY = e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = e.getRawX() - lastResizeX;
                float dy = e.getRawY() - lastResizeY;
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) window.getLayoutParams();
                int minW = dp(180), minH = dp(200);
                winW = Math.max(minW, winW + (int) dx);
                winH = Math.max(minH, winH + (int) dy);
                lp.width = winW;
                lp.height = winH;
                window.setLayoutParams(lp);
                lastResizeX = e.getRawX();
                lastResizeY = e.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resizing = false;
                break;
        }
    }

    private void runCmd(String cmdInput) {
        final String cmd = cmdInput.trim();
        append("C:\\Users\\Administrator> " + cmd + "\n");
        input.setText("");
        if (cmd.isEmpty()) return;

        if (cmd.equals("exit")) {
            append("(已退出命令提示符，返回桌面)\n");
            finish();
            return;
        }
        if (cmd.equals("help")) { showHelp(); return; }
        if (cmd.equals("ver")) {
            append("\nMicrosoft Windows [版本 6.1.7601]\n");
            return;
        }
        if (cmd.equals("cls") || cmd.equals("clear")) { output.setText(""); return; }
        if (cmd.equals("cd") || cmd.equals("cd \\") || (cmd.startsWith("cd ") && cmd.length() > 3)) {
            append("当前目录: C:\\Users\\Administrator（示意，未切换）\n");
            return;
        }

        final String linux = translate(cmd);
        if (linux == null) {
            append("'" + firstWord(cmd) + "' 不是内部或外部命令，也不是可运行的程序或批处理文件。\n");
            return;
        }

        new Thread(() -> {
            try {
                Process p = new ProcessBuilder("sh", "-c", linux).start();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) append(line + "\n");
                }
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) append(line + "\n");
                }
                p.waitFor();
            } catch (Exception e) {
                append("执行出错: " + e.getMessage() + "\n");
            }
        }).start();
    }

    private String translate(String cmd) {
        String word = firstWord(cmd);
        String rest = cmd.length() > word.length() ? cmd.substring(word.length()).trim() : "";
        switch (word) {
            case "dir": return "ls -la" + (rest.isEmpty() ? "" : " " + rest);
            case "ls": case "pwd": case "cat": case "echo": case "uname":
            case "whoami": case "id": case "date": case "df": case "ps": case "free":
                return cmd;
            case "type": return "cat" + (rest.isEmpty() ? "" : " " + rest);
            case "del": case "erase": return "rm" + (rest.isEmpty() ? "" : " " + rest);
            case "md": case "mkdir": return "mkdir" + (rest.isEmpty() ? "" : " " + rest);
            case "rd": case "rmdir": return "rmdir" + (rest.isEmpty() ? "" : " " + rest);
            case "copy": return "cp" + (rest.isEmpty() ? "" : " " + rest);
            case "move": case "ren": case "rename": return "mv" + (rest.isEmpty() ? "" : " " + rest);
            case "ipconfig": return "ifconfig 2>/dev/null || ip addr";
            case "ping": return "ping -c 4" + (rest.isEmpty() ? "" : " " + rest);
            case "tasklist": return "ps -ef";
            case "systeminfo": return "uname -a";
            case "hostname": return "getprop ro.product.model";
            case "wmic": return "cat /proc/cpuinfo | head -20";
            default: return null;
        }
    }

    private String firstWord(String s) {
        String t = s.trim();
        int i = t.indexOf(' ');
        return i < 0 ? t : t.substring(0, i);
    }

    private void showHelp() {
        append("Windows 7 风格命令提示符 · 支持命令：\n");
        append("  dir       列出目录       ipconfig  查看网络\n");
        append("  type      查看文件       ping      网络探测\n");
        append("  del       删除文件       tasklist  进程列表\n");
        append("  md/rd     建/删目录      systeminfo 系统信息\n");
        append("  copy/move 复制/移动      whoami    当前用户\n");
        append("  echo      输出文本       hostname  设备名\n");
        append("  ver       版本           cls       清屏\n");
        append("  exit      退出           help      帮助\n");
        append("\n(Linux 命令如 ls/cat/pwd/uname 也可直接使用)\n");
    }

    private void append(String s) {
        runOnUiThread(() -> {
            output.append(s);
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        });
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}