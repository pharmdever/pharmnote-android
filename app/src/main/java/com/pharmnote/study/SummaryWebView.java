package com.pharmnote.study;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;
final class SummaryWebView extends WebView {
 SummaryWebView(Context context){super(context);if(Build.VERSION.SDK_INT<=29)setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
 int documentWidth(){return computeHorizontalScrollRange();}
}
