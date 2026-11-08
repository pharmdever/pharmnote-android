package com.pharmnote.study;

import android.content.Context;
import android.graphics.*;
import android.graphics.pdf.PdfRenderer;
import android.os.*;
import android.view.*;
import java.io.File;
import java.util.concurrent.*;

/** Render only the visible viewport at native resolution, even at high zoom. */
final class ZoomPdfView extends View {
 interface Listener {void pageChanged(int page,int count);void zoomChanged(int percent);void error(String message);}
 private final PdfViewport viewport=new PdfViewport();
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final Handler main=new Handler(Looper.getMainLooper());
 private final Listener listener;
 private final ScaleGestureDetector pinch;
 private final GestureDetector gestures;
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private PdfRenderer renderer;
 private ParcelFileDescriptor descriptor;
 private Bitmap overview,detail;
 private float detailScale,detailX,detailY;
 private int detailViewW,detailViewH;
 private volatile boolean closed;
 private volatile int generation,sequence;
 private int page,count;
 private boolean loading=true;
 private final Runnable renderLater=this::requestDetail;
 ZoomPdfView(Context context,File file,int initial,Listener listener){
  super(context);this.listener=listener;page=initial;setFocusable(true);setBackgroundColor(0xFFE6EBE3);
  pinch=new ScaleGestureDetector(context,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
   public boolean onScale(ScaleGestureDetector d){viewport.zoom(d.getScaleFactor(),d.getFocusX(),d.getFocusY());changed();return true;}
   public void onScaleEnd(ScaleGestureDetector d){scheduleDetail(0);}
  });
  gestures=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
   public boolean onDown(MotionEvent e){return true;}
   public boolean onScroll(MotionEvent a,MotionEvent b,float dx,float dy){if(!pinch.isInProgress()){viewport.pan(-dx,-dy);changed();}return true;}
   public boolean onDoubleTap(MotionEvent e){if(viewport.scale<viewport.fitWidth()*1.8f)viewport.zoom(viewport.fitWidth()*3/viewport.scale,e.getX(),e.getY());else viewport.resetWidth();changed();return true;}
  });
  worker.execute(()->{try{descriptor=ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);renderer=new PdfRenderer(descriptor);int pages=renderer.getPageCount();main.post(()->{if(closed)return;count=pages;setPage(Math.max(0,Math.min(initial,pages-1)));});}catch(Exception e){main.post(()->{if(!closed)listener.error("เปิด PDF ไม่ได้ ไฟล์อาจมีรหัสผ่านหรือเสียหาย");});}});
 }
 int page(){return page;} int count(){return count;}
 void setPage(int target){
  if(closed||count==0)return;page=Math.max(0,Math.min(target,count-1));int ticket=++generation;++sequence;loading=true;viewport.ready=false;clearBitmaps();invalidate();listener.pageChanged(page,count);final int index=page;
  worker.execute(()->{if(closed||ticket!=generation)return;Bitmap thumb=null;try(PdfRenderer.Page p=renderer.openPage(index)){
   int pw=p.getWidth(),ph=p.getHeight();float sample=Math.min(900f/pw,1400f/ph);int w=Math.max(1,Math.round(pw*sample)),h=Math.max(1,Math.round(ph*sample));thumb=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);thumb.eraseColor(Color.WHITE);Matrix transform=new Matrix();transform.setScale((float)w/pw,(float)h/ph);p.render(thumb,null,transform,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);final Bitmap ready=thumb;
   main.post(()->{if(closed||ticket!=generation){ready.recycle();return;}overview=ready;loading=false;viewport.page(pw,ph);changed();});
  }catch(Exception|OutOfMemoryError e){if(thumb!=null&&!thumb.isRecycled())thumb.recycle();main.post(()->{if(!closed&&ticket==generation){loading=false;listener.error("แสดงหน้านี้ไม่ได้ ลองเปิดด้วยแอป PDF อื่น");invalidate();}});}});
 }
 void zoom(float factor){if(!viewport.ready)return;viewport.zoom(factor,getWidth()/2f,getHeight()/2f);changed();}
 void fitWidth(){if(!viewport.ready)return;viewport.resetWidth();changed();}
 void fitPage(){if(!viewport.ready)return;viewport.resetPage();changed();}
 private void changed(){if(viewport.ready){int percent=Math.round(viewport.scale/viewport.fitWidth()*100);listener.zoomChanged(percent);setContentDescription("PDF หน้า "+(page+1)+" จาก "+count+" ซูม "+percent+" เปอร์เซ็นต์ ใช้สองนิ้วซูมและลากเพื่อเลื่อน");}invalidate();scheduleDetail(160);}
 private void scheduleDetail(long delay){main.removeCallbacks(renderLater);if(viewport.ready&&!closed)main.postDelayed(renderLater,delay);}
 protected void onSizeChanged(int w,int h,int oldw,int oldh){viewport.size(w,h);changed();}
 public boolean onTouchEvent(MotionEvent e){if(closed)return false;getParent().requestDisallowInterceptTouchEvent(true);pinch.onTouchEvent(e);gestures.onTouchEvent(e);if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL){scheduleDetail(0);performClick();}return true;}
 public boolean performClick(){super.performClick();return true;}
 protected void onDraw(Canvas canvas){
  super.onDraw(canvas);canvas.drawColor(0xFFE6EBE3);
  if(overview!=null&&!overview.isRecycled()){RectF pageRect=new RectF(viewport.x,viewport.y,viewport.x+viewport.pageWidth*viewport.scale,viewport.y+viewport.pageHeight*viewport.scale);canvas.drawBitmap(overview,null,pageRect,paint);}
  if(detail!=null&&!detail.isRecycled()){
   float ratio=viewport.scale/detailScale;canvas.save();canvas.translate(viewport.x-detailX*ratio,viewport.y-detailY*ratio);canvas.scale(ratio,ratio);canvas.drawBitmap(detail,null,new RectF(0,0,detailViewW,detailViewH),paint);canvas.restore();
  }
  if(loading){paint.setColor(0xFF61755A);paint.setTextSize(16*getResources().getDisplayMetrics().scaledDensity);paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("กำลังเตรียมหน้า PDF…",getWidth()/2f,getHeight()/2f,paint);}
 }
 private void requestDetail(){
  if(closed||!viewport.ready||getWidth()<1||getHeight()<1)return;
  final int ticket=generation,seq=++sequence,index=page,vw=getWidth(),vh=getHeight();
  final float scale=viewport.scale,x=viewport.x,y=viewport.y;
  float sample=(float)Math.min(1,Math.sqrt(3200000.0/((double)vw*vh)));final int bw=Math.max(1,Math.round(vw*sample)),bh=Math.max(1,Math.round(vh*sample));
  worker.execute(()->{if(closed||ticket!=generation||seq!=sequence)return;Bitmap result=null;try(PdfRenderer.Page p=renderer.openPage(index)){
   result=Bitmap.createBitmap(bw,bh,Bitmap.Config.ARGB_8888);result.eraseColor(0xFFE6EBE3);Canvas paper=new Canvas(result);Paint white=new Paint();white.setColor(Color.WHITE);paper.drawRect(x*bw/vw,y*bh/vh,(x+p.getWidth()*scale)*bw/vw,(y+p.getHeight()*scale)*bh/vh,white);Matrix matrix=new Matrix();matrix.setValues(new float[]{scale*bw/vw,0,x*bw/vw,0,scale*bh/vh,y*bh/vh,0,0,1});p.render(result,null,matrix,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);final Bitmap ready=result;
   main.post(()->{if(closed||ticket!=generation||seq!=sequence){ready.recycle();return;}if(detail!=null)detail.recycle();detail=ready;detailScale=scale;detailX=x;detailY=y;detailViewW=vw;detailViewH=vh;invalidate();});
  }catch(Exception|OutOfMemoryError e){if(result!=null&&!result.isRecycled())result.recycle();}});
 }
 private void clearBitmaps(){if(overview!=null){overview.recycle();overview=null;}if(detail!=null){detail.recycle();detail=null;}}
 void close(){if(closed)return;closed=true;++generation;++sequence;main.removeCallbacks(renderLater);clearBitmaps();worker.execute(()->{if(renderer!=null)renderer.close();if(descriptor!=null)try{descriptor.close();}catch(Exception ignored){}});worker.shutdown();}
}
