package com.pharmnote.study;

import android.app.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

/** Reusable native menu with named actions, supporting text and code-drawn icons. */
final class CardMenu {
 static final class Item {
  final String key,title,detail;final CodeIcon.Kind icon;final Runnable action;
  Item(String key,CodeIcon.Kind icon,String title,String detail,Runnable action){this.key=key;this.icon=icon;this.title=title;this.detail=detail;this.action=action;}
 }
 static int dp(Activity a,float v){return (int)(v*a.getResources().getDisplayMetrics().density+.5f);}
 static GradientDrawable shape(Activity a,int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(a,radius));return d;}
 static Dialog show(Activity a,String title,String subtitle,Item... items){
  Dialog dialog=new Dialog(a);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout root=new LinearLayout(a);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(a,18),dp(a,18),dp(a,18),dp(a,14));root.setBackground(shape(a,0xFFFFFEF8,24));
  LinearLayout header=new LinearLayout(a);header.setGravity(Gravity.CENTER_VERTICAL);TextView heading=new TextView(a);heading.setText(title);heading.setTextSize(21);heading.setTextColor(0xFF263D35);heading.setTypeface(null,1);header.addView(heading,new LinearLayout.LayoutParams(0,-2,1));ImageButton close=new ImageButton(a);close.setImageDrawable(new CodeIcon(CodeIcon.Kind.PLUS,0xFF6B7E60));close.setRotation(45);close.setPadding(dp(a,13),dp(a,13),dp(a,13),dp(a,13));close.setBackground(shape(a,0xFFEDF2E7,24));close.setContentDescription("ปิดเมนู");close.setOnClickListener(v->dialog.dismiss());header.addView(close,new LinearLayout.LayoutParams(dp(a,44),dp(a,44)));root.addView(header);
  if(subtitle!=null&&!subtitle.isEmpty()){TextView hint=new TextView(a);hint.setText(subtitle);hint.setTextSize(12);hint.setTextColor(0xFF7F8D77);hint.setPadding(0,dp(a,6),0,dp(a,10));root.addView(hint);}
  LinearLayout actions=new LinearLayout(a);actions.setOrientation(LinearLayout.VERTICAL);ScrollView scroll=new ScrollView(a);scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);scroll.addView(actions);int height=Math.min(dp(a,items.length*88+4),(int)(a.getResources().getDisplayMetrics().heightPixels*.56));root.addView(scroll,new LinearLayout.LayoutParams(-1,height));
  int[] colors={0xFFEDF3E6,0xFFEAF2EE,0xFFF6EFE2,0xFFF0ECF6};
  for(int i=0;i<items.length;i++){Item item=items[i];LinearLayout card=new LinearLayout(a);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(a,12),dp(a,12),dp(a,12),dp(a,12));card.setMinimumHeight(dp(a,78));card.setTag("action:"+item.key);card.setContentDescription(item.title+". "+item.detail);card.setFocusable(true);card.setClickable(true);GradientDrawable background=shape(a,colors[i%colors.length],16);background.setStroke(dp(a,1),0xFFDCE5D5);card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x20315C4D),background,null));
   ImageView icon=new ImageView(a);icon.setImageDrawable(new CodeIcon(item.icon,0xFF315C4D));icon.setPadding(dp(a,10),dp(a,10),dp(a,10),dp(a,10));icon.setBackground(shape(a,0xB3FFFFFF,12));card.addView(icon,new LinearLayout.LayoutParams(dp(a,42),dp(a,42)));
   LinearLayout labels=new LinearLayout(a);labels.setOrientation(LinearLayout.VERTICAL);labels.setPadding(dp(a,12),0,dp(a,6),0);TextView name=new TextView(a);name.setText(item.title);name.setTextSize(15);name.setTextColor(0xFF294537);name.setTypeface(null,1);labels.addView(name);if(!item.detail.isEmpty()){TextView desc=new TextView(a);desc.setText(item.detail);desc.setTextSize(11);desc.setTextColor(0xFF78896E);desc.setPadding(0,dp(a,3),0,0);labels.addView(desc);}card.addView(labels,new LinearLayout.LayoutParams(0,-2,1));ImageView arrow=new ImageView(a);arrow.setImageDrawable(new CodeIcon(CodeIcon.Kind.RIGHT,0xFF8A9B7D));card.addView(arrow,new LinearLayout.LayoutParams(dp(a,18),dp(a,18)));card.setOnClickListener(v->{dialog.dismiss();item.action.run();});LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(a,4),0,dp(a,4));actions.addView(card,cp);
  }
  dialog.setContentView(root);Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams params=w.getAttributes();params.dimAmount=.38f;w.setAttributes(params);}dialog.show();if(w!=null)w.setLayout(Math.min(a.getResources().getDisplayMetrics().widthPixels-dp(a,28),dp(a,540)),WindowManager.LayoutParams.WRAP_CONTENT);return dialog;
 }
 private CardMenu(){}
}
