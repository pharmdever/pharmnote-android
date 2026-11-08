package com.pharmnote.study;

import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.net.Uri;
import android.view.*;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ReaderSmokeTest {
 static final String ID="11111111-2222-4333-8444-555555555555";
 Context context;
 @Before public void fixture()throws Exception{
  context=InstrumentationRegistry.getInstrumentation().getTargetContext();
  PdfDocument pdf=new PdfDocument();try{PdfDocument.Page page=pdf.startPage(new PdfDocument.PageInfo.Builder(842,595,1).create());Canvas c=page.getCanvas();c.drawColor(Color.WHITE);Paint p=new Paint();p.setColor(0xFF234F43);p.setTextSize(26);c.drawText("LANDSCAPE GUIDELINE — READER QA",28,45,p);p.setTextSize(13);p.setColor(Color.BLACK);for(int row=0;row<30;row++){c.drawText("Section "+row+": complete source document, inspect detail at 300% zoom.",28,78+row*16,p);c.drawText("Monitoring and reference "+row,490,78+row*16,p);}pdf.finishPage(page);try(OutputStream out=new FileOutputStream(new File(context.getFilesDir(),ID+".pdf"))){pdf.writeTo(out);}}finally{pdf.close();}
  String source="# Breast cancer — study notes\n\n**Source-bound summary** · learning fixture\n\n## Regimen overview\n\n| Item | Guideline details |\n| --- | --- |\n| Population | Specified by the source PDF |\n| Monitoring | Review original pages |\n\n## หลายคอลัมน์\n\n| Regimen | Dose | Days | Route | Source |\n| --- | --- | --- | --- | --- |\n| A | 60 mg/m² | Day 1 | IV | PDF 75 |\n| B | 600 mg/m² | Days 1, 8, 15 | IV | PDF 75 |\n\n## ข้อควรระวัง\n\n> Verify doses and source references.\n\n"+new String(new char[6000]).replace("\0","Complete long note paragraph.\n\n")+"LONG_NOTE_TAIL";
  JSONObject note=new JSONObject().put("answer",source).put("topic","whole file").put("pages","1").put("reviewed",true);
  JSONObject d=new JSONObject().put("id",ID).put("title","Landscape guideline • QA").put("category","มะเร็งและเนื้องอก").put("org","Synthetic learning fixture").put("year","2026").put("favorite",true).put("notes",new JSONObject().put("regimen",note));LibraryStore.write(context,new JSONArray().put(d));
 }
 @Test public void landscapePdfPinchZoomAndFormattedLongNotes()throws Exception{
  try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
   scenario.onActivity(a->{try{a.open(LibraryStore.find(a,ID),0);}catch(Exception e){throw new AssertionError(e);}});
   AtomicBoolean ready=new AtomicBoolean();for(int i=0;i<100;i++){scenario.onActivity(a->ready.set(a.pdfView!=null&&a.pdfView.count()==1&&a.zoomLabel.getText().toString().equals("100%")));if(ready.get())break;SystemClock.sleep(150);}assertTrue("PDF did not become ready",ready.get());SystemClock.sleep(1200);
   AtomicReference<ZoomPdfView> view=new AtomicReference<>();scenario.onActivity(a->view.set(a.pdfView));pinch(view.get());SystemClock.sleep(1500);screenshot("qa-reader.png");scenario.onActivity(a->{int percent=Integer.parseInt(a.zoomLabel.getText().toString().replace("%",""));assertTrue("Pinch did not zoom",percent>150);a.pdfView.zoom(1.1f);});SystemClock.sleep(800);screenshot("qa-reader.png");
   scenario.onActivity(a->a.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE));SystemClock.sleep(1800);scenario.onActivity(a->assertTrue("Landscape should maximize PDF",a.readerFullscreen));screenshot("qa-landscape.png");
  }
  Intent intent=new Intent(context,StudyActivity.class).putExtra("document",ID).putExtra("mode","regimen");
  try(ActivityScenario<StudyActivity> scenario=ActivityScenario.launch(intent)){
   waitForSummary(scenario);assertSummaryIsPainted(scenario);screenshot("qa-summary.png");scenario.onActivity(a->a.preview.scrollTo(0,(int)(430*a.getResources().getDisplayMetrics().density)));SystemClock.sleep(500);screenshot("qa-summary-cards.png");scenario.onActivity(a->a.preview.scrollTo(0,0));
   for(int zoom:new int[]{100,160,180}){scenario.onActivity(a->a.preview.getSettings().setTextZoom(zoom));SystemClock.sleep(750);scenario.onActivity(a->assertTrue("Summary overflows horizontally at "+zoom+"%: "+a.preview.documentWidth()+" / "+a.preview.getWidth(),a.preview.documentWidth()<=a.preview.getWidth()+3));}
   scenario.onActivity(a->{a.preview.getSettings().setTextZoom(100);assertNull("AI send must not be on summary home",findText(a.getWindow().getDecorView(),"ส่ง PDF ทั้งไฟล์ให้ AI"));a.options();assertNotNull(findTag(a.currentMenu.getWindow().getDecorView(),"action:ai-tools"));assertNull("Sending AI should require the second menu",findTag(a.currentMenu.getWindow().getDecorView(),"action:send-pdf"));});SystemClock.sleep(400);screenshot("qa-summary-menu.png");
   scenario.onActivity(a->{findTag(a.currentMenu.getWindow().getDecorView(),"action:ai-tools").performClick();assertNotNull(findTag(a.currentMenu.getWindow().getDecorView(),"action:send-pdf"));assertNotNull(findTag(a.currentMenu.getWindow().getDecorView(),"action:copy-prompt"));});SystemClock.sleep(400);screenshot("qa-ai-tools.png");
   scenario.onActivity(a->{a.currentMenu.dismiss();try{assertTrue("Save long summary failed",a.save(false));JSONObject doc=LibraryStore.find(a,ID);assertTrue(doc.optBoolean("favorite"));assertTrue(doc.getJSONObject("notes").getJSONObject("regimen").getString("answer").endsWith("LONG_NOTE_TAIL"));}catch(Exception e){throw new AssertionError(e);}});
  }
 }
 void waitForSummary(ActivityScenario<StudyActivity> scenario){AtomicBoolean ready=new AtomicBoolean();for(int i=0;i<160;i++){scenario.onActivity(a->ready.set(a.previewLoaded&&a.preview.getContentHeight()>0));if(ready.get())break;SystemClock.sleep(100);}assertTrue("Formatted summary did not load",ready.get());SystemClock.sleep(500);}
 void assertSummaryIsPainted(ActivityScenario<StudyActivity> scenario){AtomicReference<Rect> bounds=new AtomicReference<>();scenario.onActivity(a->{int[] pos=new int[2];a.preview.getLocationOnScreen(pos);bounds.set(new Rect(pos[0]+20,pos[1]+15,pos[0]+a.preview.getWidth()-20,pos[1]+Math.min(a.preview.getHeight()-15,650)));});InstrumentationRegistry.getInstrumentation().waitForIdleSync();SystemClock.sleep(1000);Bitmap b=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();assertNotNull(b);int dark=0;Rect rect=bounds.get();for(int y=rect.top;y<Math.min(rect.bottom,b.getHeight());y+=4)for(int x=rect.left;x<Math.min(rect.right,b.getWidth());x+=4){int color=b.getPixel(x,y);if(Color.red(color)<160&&Color.green(color)<160&&Color.blue(color)<160)dark++;}b.recycle();assertTrue("Summary is laid out but no text was painted: "+dark,dark>40);}
 View findTag(View view,String tag){if(tag.equals(view.getTag()))return view;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){View found=findTag(group.getChildAt(i),tag);if(found!=null)return found;}}return null;}
 View findText(View view,String text){if(view instanceof android.widget.TextView&&text.equals(((android.widget.TextView)view).getText().toString()))return view;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){View found=findText(group.getChildAt(i),text);if(found!=null)return found;}}return null;}
 void screenshot(String name)throws Exception{Bitmap b=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();assertNotNull(b);ContentValues values=new ContentValues();values.put(MediaStore.Images.Media.DISPLAY_NAME,name);values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");values.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Pharmnote-QA");values.put(MediaStore.Images.Media.IS_PENDING,1);Uri uri=context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);assertNotNull(uri);try(OutputStream out=context.getContentResolver().openOutputStream(uri)){assertNotNull(out);b.compress(Bitmap.CompressFormat.PNG,100,out);}values.clear();values.put(MediaStore.Images.Media.IS_PENDING,0);context.getContentResolver().update(uri,values,null,null);b.recycle();}
 void pinch(View view){
  int[] loc=new int[2];view.getLocationOnScreen(loc);float cx=loc[0]+view.getWidth()/2f,cy=loc[1]+view.getHeight()/2f;
  float start=view.getWidth()*.10f,end=view.getWidth()*.40f;
  long down=SystemClock.uptimeMillis();MotionEvent.PointerProperties[] props=new MotionEvent.PointerProperties[2];for(int i=0;i<2;i++){props[i]=new MotionEvent.PointerProperties();props[i].id=i;props[i].toolType=MotionEvent.TOOL_TYPE_FINGER;}
  send(down,MotionEvent.ACTION_DOWN,1,props,cx,cy,start);send(down,MotionEvent.ACTION_POINTER_DOWN|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),2,props,cx,cy,start);
  for(int i=0;i<15;i++){send(down,MotionEvent.ACTION_MOVE,2,props,cx,cy,start+(end-start)*i/14);SystemClock.sleep(25);}
  send(down,MotionEvent.ACTION_POINTER_UP|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),2,props,cx,cy,end);send(down,MotionEvent.ACTION_UP,1,props,cx,cy,end);
 }
 void send(long down,int action,int count,MotionEvent.PointerProperties[] props,float x,float y,float distance){MotionEvent.PointerCoords[] coords=new MotionEvent.PointerCoords[count];for(int i=0;i<count;i++){coords[i]=new MotionEvent.PointerCoords();coords[i].x=x+(i==0?-distance:distance);coords[i].y=y;coords[i].pressure=1;coords[i].size=1;}MotionEvent e=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,count,props,coords,0,0,1,1,0,0,android.view.InputDevice.SOURCE_TOUCHSCREEN,0);InstrumentationRegistry.getInstrumentation().getUiAutomation().injectInputEvent(e,true);e.recycle();}
}
