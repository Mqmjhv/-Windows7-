package com.win7lite;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

// 内嵌简易浏览器
public class BrowserActivity extends Activity {
    private WebView web;
    private EditText urlBar;
    private TextView titleBar;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // 顶部标题栏
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackgroundColor(Color.rgb(28, 110, 196));
        top.setPadding(dp(6), dp(4), dp(6), dp(4));

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextSize(24);
        back.setTextColor(Color.WHITE);
        back.setPadding(dp(10), dp(2), dp(10), dp(2));
        back.setOnClickListener(v -> { if (web.canGoBack()) web.goBack(); });
        top.addView(back);

        TextView fwd = new TextView(this);
        fwd.setText("→");
        fwd.setTextSize(24);
        fwd.setTextColor(Color.WHITE);
        fwd.setPadding(dp(10), dp(2), dp(10), dp(2));
        fwd.setOnClickListener(v -> { if (web.canGoForward()) web.goForward(); });
        top.addView(fwd);

        urlBar = new EditText(this);
        urlBar.setSingleLine(true);
        urlBar.setText("https://www.baidu.com");
        urlBar.setTextColor(Color.BLACK);
        urlBar.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(0, dp(40), 1f);
        top.addView(urlBar, up);
        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadUrl(urlBar.getText().toString());
                return true;
            }
            return false;
        });

        TextView go = new TextView(this);
        go.setText("前往");
        go.setTextSize(16);
        go.setTextColor(Color.WHITE);
        go.setPadding(dp(10), dp(6), dp(10), dp(6));
        go.setOnClickListener(v -> loadUrl(urlBar.getText().toString()));
        top.addView(go);

        root.addView(top);

        // WebView
        web = new WebView(this);
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onReceivedTitle(WebView v, String t) {
                if (t != null && titleBar != null) titleBar.setText(t);
            }
        });
        root.addView(web, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        loadUrl("https://www.baidu.com");
    }

    private void loadUrl(String s) {
        String u = s.trim();
        if (u.isEmpty()) return;
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "https://" + u;
        }
        urlBar.setText(u);
        hideKeyboard();
        web.loadUrl(u);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(urlBar.getWindowToken(), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && web.canGoBack()) {
            web.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (web != null) web.destroy();
        super.onDestroy();
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}