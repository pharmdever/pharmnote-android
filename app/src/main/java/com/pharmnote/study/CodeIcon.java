package com.pharmnote.study;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

/** Small, density-independent interface icons drawn from paths; no font glyph icons. */
final class CodeIcon extends Drawable {
 enum Kind {BACK,MORE,READ,EDIT,COPY,PASTE,IMPORT,EXPORT,INFO,AI,SEND,SAVE,PLUS,MINUS,STAR,FIT,FULLSCREEN,ROTATE,DELETE,UPDATE,SETTINGS,DOWN,RIGHT,FONT_SMALL,FONT_LARGE,CONTENTS}
 private final Kind kind;private int color;private boolean filled;
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 CodeIcon(Kind kind,int color){this.kind=kind;this.color=color;}
 CodeIcon filled(boolean value){filled=value;return this;}
 void color(int value){color=value;invalidateSelf();}
 private void line(Canvas c,float x,float y,float a,float b){c.drawLine(x,y,a,b,p);}
 private void path(Canvas c,float... coords){Path path=new Path();path.moveTo(coords[0],coords[1]);for(int i=2;i<coords.length;i+=2)path.lineTo(coords[i],coords[i+1]);c.drawPath(path,p);}
 private void box(Canvas c,float l,float t,float r,float b){c.drawRoundRect(l,t,r,b,2,2,p);}
 public void draw(Canvas c){c.save();Rect bounds=getBounds();float scale=Math.min(bounds.width(),bounds.height())/24f;c.translate(bounds.left+(bounds.width()-24*scale)/2,bounds.top+(bounds.height()-24*scale)/2);c.scale(scale,scale);p.setColor(color);p.setStrokeWidth(1.8f);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
  switch(kind){
   case BACK:path(c,14,5,7,12,14,19);break;
   case RIGHT:path(c,9,6,15,12,9,18);break;
   case DOWN:path(c,6,9,12,15,18,9);break;
   case MORE:p.setStyle(Paint.Style.FILL);for(int y=5;y<=19;y+=7)c.drawCircle(12,y,1.6f,p);break;
   case READ:path(c,12,6,8,4,3,4,3,19,8,19,12,21,16,19,21,19,21,4,16,4,12,6,12,21);break;
   case EDIT:path(c,4,17,3,21,7,20,20,7,17,4,4,17);line(c,14,7,17,10);break;
   case COPY:box(c,8,7,20,21);path(c,15,3,4,3,4,16);break;
   case PASTE:box(c,5,5,19,21);p.setStyle(Paint.Style.FILL);c.drawRoundRect(9,3,15,7,1,1,p);p.setStyle(Paint.Style.STROKE);line(c,9,11,15,11);line(c,9,15,15,15);break;
   case IMPORT:case EXPORT:path(c,5,8,5,21,19,21,19,8);if(kind==Kind.IMPORT){path(c,8,9,12,13,16,9);line(c,12,3,12,13);}else{path(c,8,7,12,3,16,7);line(c,12,3,12,14);}break;
   case INFO:c.drawCircle(12,12,9,p);line(c,12,11,12,17);p.setStyle(Paint.Style.FILL);c.drawCircle(12,7,1,p);break;
   case AI:path(c,12,3,14.5f,9.5f,21,12,14.5f,14.5f,12,21,9.5f,14.5f,3,12,9.5f,9.5f,12,3);break;
   case SEND:path(c,3,3,21,12,3,21,6,12,3,3);line(c,6,12,21,12);break;
   case SAVE:path(c,4,3,17,3,21,7,21,21,3,21,3,3,4,3);box(c,7,13,17,21);path(c,7,3,7,9,16,9,16,3);break;
   case PLUS:line(c,12,5,12,19);line(c,5,12,19,12);break;
   case MINUS:line(c,5,12,19,12);break;
   case STAR:Path star=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float r=i%2==0?9:4;float x=12+(float)Math.cos(a)*r,y=12+(float)Math.sin(a)*r;if(i==0)star.moveTo(x,y);else star.lineTo(x,y);}star.close();if(filled)p.setStyle(Paint.Style.FILL);c.drawPath(star,p);break;
   case FIT:box(c,6,3,18,21);line(c,2,12,9,12);path(c,6,9,9,12,6,15);line(c,15,12,22,12);path(c,18,9,15,12,18,15);break;
   case FULLSCREEN:path(c,3,9,3,3,9,3);path(c,15,3,21,3,21,9);path(c,21,15,21,21,15,21);path(c,9,21,3,21,3,15);break;
   case ROTATE:box(c,7,4,17,20);path(c,3,8,3,3,8,3);path(c,21,16,21,21,16,21);break;
   case DELETE:path(c,4,6,20,6);path(c,8,6,8,3,16,3,16,6);path(c,6,6,7,21,17,21,18,6);line(c,10,10,10,17);line(c,14,10,14,17);break;
   case UPDATE:c.drawArc(4,4,20,20,40,280,false,p);path(c,16,3,21,5,18,10);break;
   case SETTINGS:c.drawCircle(12,12,4,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;line(c,12+(float)Math.cos(a)*8,12+(float)Math.sin(a)*8,12+(float)Math.cos(a)*10,12+(float)Math.sin(a)*10);}c.drawCircle(12,12,8,p);break;
   case CONTENTS:for(int y=6;y<=18;y+=6){p.setStyle(Paint.Style.FILL);c.drawCircle(4,y,1.2f,p);p.setStyle(Paint.Style.STROKE);line(c,8,y,21,y);}break;
   case FONT_SMALL:case FONT_LARGE:path(c,3,19,9,5,15,19);line(c,6,13,12,13);line(c,17,12,23,12);if(kind==Kind.FONT_LARGE)line(c,20,9,20,15);break;
  }c.restore();
 }
 public void setAlpha(int a){p.setAlpha(a);invalidateSelf();}public void setColorFilter(ColorFilter filter){p.setColorFilter(filter);invalidateSelf();}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
 static void attach(TextView view,Kind kind,int color,int size){CodeIcon icon=new CodeIcon(kind,color);icon.setBounds(0,0,size,size);view.setCompoundDrawables(icon,null,null,null);view.setCompoundDrawablePadding(size/3);}
 static void tint(TextView view,int color){for(Drawable d:view.getCompoundDrawables())if(d instanceof CodeIcon)((CodeIcon)d).color(color);}
 static Kind fromLabel(String s){if(s.equals("⋮"))return Kind.MORE;if(s.contains("▾"))return Kind.DOWN;if(s.startsWith("‹"))return Kind.BACK;if(s.startsWith("⛶"))return Kind.FULLSCREEN;if(s.equals("−"))return Kind.MINUS;if(s.equals("＋"))return Kind.PLUS;if(s.contains("ก่อนหน้า"))return Kind.BACK;if(s.contains("ถัดไป"))return Kind.RIGHT;if(s.contains("พอดี"))return Kind.FIT;if(s.contains("＋"))return Kind.PLUS;if(s.contains("โปรด")||s.equals("☆")||s.equals("★"))return Kind.STAR;if(s.contains("เปิดอ่าน")||s.contains("อ่านสรุป")||s.contains("อธิบาย"))return Kind.READ;if(s.contains("ข้อมูล")||s.contains("รายละเอียด"))return Kind.INFO;if(s.contains("บันทึก"))return Kind.SAVE;if(s.contains("คัดลอก"))return Kind.COPY;if(s.contains("วางข้อความ"))return Kind.PASTE;if(s.contains("นำเข้า"))return Kind.IMPORT;if(s.contains("Regimen"))return Kind.READ;if(s.contains("หมวด"))return Kind.DOWN;return null;}
 static String label(String s){return s.replace("⋮","").replace("‹","").replace("›","").replace("⛶","").replace("↗","").replace("✧","").replace("＋","").replace("−","").replace("☆","").replace("★","").replace("▾","").trim();}
}
