package com.qbao.newim.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qbao.newim.qbim.R;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * Created by chenjian on 2017/9/25.
 */

public class NIMWebViewActivity extends NIM_ToolbarAct{

    WebView mWebView;
    SwipeRefreshLayout swipe;
    String url;
    TextView tvTitle;
    ProgressBar progressBar;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_webview);

        mWebView = (WebView)findViewById(R.id.web_view);
        swipe = (SwipeRefreshLayout)findViewById(R.id.swipe);
        swipe.setEnabled(false);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    @Override
    protected void setListener() {
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                processLogic(null);
            }
        });
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        url = getIntent().getStringExtra("url");
        if (!url.contains("http")) {
            url = "http://" + url;
        }
        mWebView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        progressBar = (ProgressBar) actionView.findViewById(R.id.progressBar);

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress != 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                tvTitle.setText(title);
            }
        });

        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
