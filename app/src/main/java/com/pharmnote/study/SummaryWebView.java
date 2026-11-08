package com.pharmnote.study;
import android.content.Context;
import android.webkit.WebView;
final class SummaryWebView extends WebView {
 SummaryWebView(Context context){super(context);}
 int documentWidth(){return computeHorizontalScrollRange();}
}
