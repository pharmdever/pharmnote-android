package com.pharmnote.study;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;
import android.webkit.ValueCallback;
import org.json.JSONObject;

final class SummaryWebView extends WebView {
 SummaryWebView(Context context){super(context);if(Build.VERSION.SDK_INT<=29)setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
 int documentWidth(){return computeHorizontalScrollRange();}
 /** Host-generated navigation only. Note HTML stays escaped, network blocked, CSP script-src none.
  * No interface is exposed to page content; scripting is switched back off after the host call. */
 void navigateToHeading(String id,ValueCallback<Boolean> done){
  if(id==null||!id.matches("pn-heading-[0-9]+")){done.onReceiveValue(false);return;}
  String script="(function(){var e=document.getElementById("+JSONObject.quote(id)+");if(!e)return false;var old=document.querySelector('.pn-current');if(old)old.classList.remove('pn-current');e.classList.add('pn-current');e.scrollIntoView({block:'start'});return true;})()";
  getSettings().setJavaScriptEnabled(true);
  try{evaluateJavascript(script,result->{getSettings().setJavaScriptEnabled(false);done.onReceiveValue("true".equals(result));});}
  catch(RuntimeException e){getSettings().setJavaScriptEnabled(false);done.onReceiveValue(false);}
 }
}
