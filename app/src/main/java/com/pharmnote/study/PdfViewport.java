package com.pharmnote.study;

/** Geometry in display pixels; PDF page dimensions are in PDF points. */
final class PdfViewport {
 float width=1,height=1,pageWidth=1,pageHeight=1,scale=1,x=0,y=0;
 boolean ready;
 float fitPage(){return Math.min(width/pageWidth,height/pageHeight);}
 float fitWidth(){return width/pageWidth;}
 void size(float w,float h){
  float oldWidth=width,oldHeight=height;
  float centerX=(oldWidth/2-x)/scale,centerY=(oldHeight/2-y)/scale;
  float relative=ready?scale/fitWidth():1;
  width=Math.max(1,w);height=Math.max(1,h);
  if(ready){scale=Math.max(fitPage(),Math.min(fitWidth()*12,fitWidth()*relative));x=width/2-centerX*scale;y=height/2-centerY*scale;clamp();}
 }
 void page(float w,float h){pageWidth=Math.max(1,w);pageHeight=Math.max(1,h);ready=true;resetWidth();}
 void resetWidth(){scale=fitWidth();x=0;y=(height-pageHeight*scale)/2;clamp();}
 void resetPage(){scale=fitPage();x=(width-pageWidth*scale)/2;y=(height-pageHeight*scale)/2;clamp();}
 void zoom(float factor,float focusX,float focusY){float next=Math.max(fitPage(),Math.min(fitWidth()*12,scale*factor));float ratio=next/scale;x=focusX-(focusX-x)*ratio;y=focusY-(focusY-y)*ratio;scale=next;clamp();}
 void pan(float dx,float dy){x+=dx;y+=dy;clamp();}
 void clamp(){float w=pageWidth*scale,h=pageHeight*scale;x=w<=width?(width-w)/2:Math.max(width-w,Math.min(0,x));y=h<=height?(height-h)/2:Math.max(height-h,Math.min(0,y));}
}
