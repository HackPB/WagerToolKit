package com.Keepitsoft.wagertoolkit;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class PrivacyPolicy extends AppCompatActivity {
    WebView myWebView;

    public void myWebView () {
        myWebView = (WebView) findViewById(R.id.myWebView);
        myWebView.loadUrl("https://www.wagertool.com/wagertoolkit-privacy-policy");
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        myWebView.setWebChromeClient(new WebChromeClient());
        final ProgressBar mProgress = (ProgressBar) findViewById(R.id.mProgress);
        myWebView.setWebViewClient(new WebViewClient(){

            public boolean shouldOverrideUrlLoading (WebView view, String url){
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted (WebView view, String url, Bitmap favicon){
                mProgress.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished (WebView view, String url){
                mProgress.setVisibility(View.INVISIBLE);
                super.onPageFinished(view, url);
            }

        });

        myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >=19){
            myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else {
            myWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


    }


    @Override
    public void onBackPressed(){
        if (myWebView.canGoBack()){
            myWebView.goBack();
        }
    }


    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        myWebView();

    }
}
