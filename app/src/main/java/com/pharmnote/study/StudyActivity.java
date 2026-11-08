package com.pharmnote.study;

import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.util.AtomicFile;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/** Full-screen long-form editor and offline formatted reading surface. */
public class StudyActivity extends Activity {
 private final int GREEN=0xFF315C4D,INK=0xFF263D35;
 private LinearLayout root,editor,editTools,readTools;
 private EditText answer;
 private TextView status;
 private CheckBox reviewed;
 private WebView preview;
 private String id,mode,topic="",pages="",savedState="";
 private JSONObject document,previousNote;
 private AtomicFile draft;
 private boolean reading,destroyed;
 private int textZoom=100,renderTicket;
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
 LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
 TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextColor(INK);t.setTextSize(size);t.setPadding(0,dp(5),0,dp(5));return t;}
 GradientDrawable shape(int color){GradientDrawable bg=new GradientDrawable();bg.setColor(color);bg.setCornerRadius(dp(12));return bg;}
 Button button(String name,boolean primary,Runnable action){Button b=new Button(this);b.setText(name);b.setTextSize(13);b.setAllCaps(false);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(dp(48));b.setMinimumHeight(dp(48));b.setPadding(dp(10),dp(6),dp(10),dp(6));b.setTextColor(primary?Color.WHITE:GREEN);b.setElevation(0);b.setStateListAnimator(null);b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x20315C4D),shape(primary?GREEN:0xFFEDF3E6),null));b.setOnClickListener(v->action.run());return b;}
 void addButton(LinearLayout parent,String title,Runnable action){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);p.setMargins(dp(2),dp(3),dp(2),dp(3));parent.addView(button(title,false,action),p);}
 void toast(String message){Toast.makeText(this,message,Toast.LENGTH_LONG).show();}
 public void onCreate(Bundle state){
  super.onCreate(state);getWindow().getDecorView().setOnApplyWindowInsetsListener((v,in)->{v.setPadding(in.getSystemWindowInsetLeft(),in.getSystemWindowInsetTop(),in.getSystemWindowInsetRight(),in.getSystemWindowInsetBottom());return in.consumeSystemWindowInsets();});
  id=getIntent().getStringExtra("document");mode="regimen".equals(getIntent().getStringExtra("mode"))?"regimen":"explain";
  try{document=LibraryStore.find(this,id);JSONObject notes=document.optJSONObject("notes");previousNote=notes==null?null:notes.optJSONObject(mode);if(previousNote==null)previousNote=new JSONObject();topic=previousNote.optString("topic");pages=previousNote.optString("pages");}catch(Exception e){toast("ไม่พบ Guideline นี้ในคลัง");finish();return;}
  File drafts=new File(getFilesDir(),"note-drafts");drafts.mkdirs();draft=new AtomicFile(new File(drafts,id+"-"+mode+".json"));
  root=column();root.setPadding(dp(14),dp(8),dp(14),dp(8));root.setBackgroundColor(0xFFF6F8F3);root.setFocusableInTouchMode(true);setContentView(root);
  LinearLayout header=row();header.addView(button("‹",false,this::onBackPressed),new LinearLayout.LayoutParams(dp(48),dp(48)));LinearLayout heading=column();heading.setPadding(dp(9),0,dp(4),0);TextView title=text(mode.equals("regimen")?"สรุป Regimen":"อธิบายและสรุป",19);title.setTypeface(null,1);heading.addView(title);TextView source=text(document.optString("title"),11);source.setMaxLines(1);source.setEllipsize(android.text.TextUtils.TruncateAt.END);heading.addView(source);header.addView(heading,new LinearLayout.LayoutParams(0,-2,1));header.addView(button("⋮",false,this::options),new LinearLayout.LayoutParams(dp(48),dp(48)));root.addView(header);
  root.addView(button("ส่ง PDF ทั้งไฟล์ให้ AI  ↗",true,this::aiOptions),new LinearLayout.LayoutParams(-1,dp(48)));
  LinearLayout tabs=row();addButton(tabs,"อ่านสรุป",()->showTab(true));addButton(tabs,"แก้ไข / วางข้อความ",()->showTab(false));root.addView(tabs);
  editTools=row();addButton(editTools,"วางข้อความ",this::paste);addButton(editTools,"นำเข้าไฟล์",this::importText);addButton(editTools,"รายละเอียด",this::details);root.addView(editTools);
  readTools=row();addButton(readTools,"A−",()->changeZoom(-10));addButton(readTools,"A＋",()->changeZoom(10));addButton(readTools,"คัดลอกทั้งหมด",this::copyAll);root.addView(readTools);
  FrameLayout area=new FrameLayout(this);root.addView(area,new LinearLayout.LayoutParams(-1,0,1));editor=column();answer=new EditText(this);answer.setTextSize(16);answer.setTextColor(INK);answer.setHintTextColor(0xFF95A08B);answer.setHint("วางคำตอบจาก AI ได้ทั้งชุด\n\nรองรับ # หัวข้อ, **ตัวหนา**, รายการ และตาราง Markdown\n\nกด ‘อ่านสรุป’ เพื่อดูรูปแบบที่จัดไว้ให้");answer.setGravity(Gravity.TOP);answer.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);answer.setPadding(dp(14),dp(13),dp(14),dp(13));answer.setBackground(shape(Color.WHITE));answer.setLineSpacing(dp(4),1);answer.setVerticalScrollBarEnabled(true);answer.setSaveEnabled(false);answer.setText(previousNote.optString("answer"));editor.addView(answer,new LinearLayout.LayoutParams(-1,-1));area.addView(editor,new FrameLayout.LayoutParams(-1,-1));
  preview=new WebView(this);preview.setBackgroundColor(0xFFFFFEF9);WebSettings settings=preview.getSettings();settings.setJavaScriptEnabled(false);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setBlockNetworkLoads(true);settings.setDomStorageEnabled(false);settings.setSupportZoom(true);settings.setBuiltInZoomControls(true);settings.setDisplayZoomControls(false);settings.setDefaultTextEncodingName("UTF-8");preview.setWebViewClient(new WebViewClient(){public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){Uri uri=request.getUrl();if("https".equals(uri.getScheme())||"http".equals(uri.getScheme()))new AlertDialog.Builder(StudyActivity.this).setMessage("เปิดลิงก์ภายนอกจากสรุปนี้ในเบราว์เซอร์?").setNegativeButton("ยกเลิก",null).setPositiveButton("เปิด",(d,w)->{try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(Exception e){toast("เปิดลิงก์ไม่ได้");}}).show();return true;}});area.addView(preview,new FrameLayout.LayoutParams(-1,-1));
  status=text("",11);status.setTextColor(0xFF849278);root.addView(status);reviewed=new CheckBox(this);reviewed.setText("ตรวจทานกับ PDF ต้นฉบับแล้ว");reviewed.setTextSize(12);reviewed.setTextColor(GREEN);reviewed.setChecked(previousNote.optBoolean("reviewed"));root.addView(reviewed);root.addView(button("บันทึกสรุป",true,()->save(false)),new LinearLayout.LayoutParams(-1,dp(48)));
  savedState=snapshot().toString();boolean restored=false;
  try{JSONObject unsaved=new JSONObject(new String(draft.readFully(),StandardCharsets.UTF_8));topic=unsaved.optString("topic");pages=unsaved.optString("pages");answer.setText(unsaved.optString("answer"));reviewed.setChecked(unsaved.optBoolean("reviewed"));restored=dirty();}catch(FileNotFoundException ignored){}catch(Exception e){toast("อ่านร่างที่บันทึกไว้ไม่สำเร็จ แต่สรุปเดิมยังอยู่");}
  answer.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){if(reviewed.isChecked())reviewed.setChecked(false);updateStatus();}public void afterTextChanged(Editable e){}});reviewed.setOnCheckedChangeListener((b,c)->updateStatus());textZoom=getPreferences(MODE_PRIVATE).getInt("textZoom",100);preview.getSettings().setTextZoom(textZoom);showTab(!answer.getText().toString().trim().isEmpty()&&!restored);if(restored)toast("คืนข้อความจากร่างที่ยังไม่ได้บันทึกแล้ว");root.requestFocus();getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
 }
 JSONObject snapshot(){JSONObject n=new JSONObject();try{n.put("topic",topic).put("answer",answer.getText().toString()).put("pages",pages).put("reviewed",reviewed.isChecked());}catch(Exception ignored){}return n;}
 boolean dirty(){return answer!=null&&!snapshot().toString().equals(savedState);}
 void updateStatus(){if(status!=null)status.setText(answer.length()+" ตัวอักษร  ·  "+(dirty()?"มีการแก้ไข ยังไม่บันทึก":"บันทึกแล้ว")+(reading?"  ·  "+textZoom+"%":""));}
 void hideKeyboard(){View focus=getCurrentFocus();if(focus!=null)((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focus.getWindowToken(),0);root.requestFocus();}
 void showTab(boolean read){reading=read;editor.setVisibility(read?View.GONE:View.VISIBLE);preview.setVisibility(read?View.VISIBLE:View.GONE);readTools.setVisibility(read?View.VISIBLE:View.GONE);editTools.setVisibility(read?View.GONE:View.VISIBLE);if(read){hideKeyboard();render();}updateStatus();}
 void render(){final String source=answer.getText().toString();final int ticket=++renderTicket;worker.execute(()->{try{String html=RichSummary.document(source.trim().isEmpty()?"# ยังไม่มีสรุป\n\nส่ง PDF ทั้งไฟล์ให้ AI แล้วนำคำตอบกลับมาวางที่แท็บ **แก้ไข / วางข้อความ** ได้เลย":source);runOnUiThread(()->{if(!destroyed&&ticket==renderTicket)preview.loadDataWithBaseURL("https://pharmnote.invalid/",html,"text/html","UTF-8",null);});}catch(Exception e){runOnUiThread(()->{if(!destroyed)toast("แสดงรูปแบบไม่สำเร็จ ข้อความต้นฉบับยังอยู่ในแท็บแก้ไข");});}});}
 void changeZoom(int delta){textZoom=Math.max(80,Math.min(180,textZoom+delta));preview.getSettings().setTextZoom(textZoom);getPreferences(MODE_PRIVATE).edit().putInt("textZoom",textZoom).apply();updateStatus();}
 String prompt(){return StudyPrompt.create(document.optString("title"),document.optString("year"),mode,topic);}
 void aiOptions(){hideKeyboard();new AlertDialog.Builder(this).setTitle("ส่ง PDF ทั้งไฟล์ไปถาม AI").setMessage("แนบไฟล์ต้นฉบับทั้งไฟล์ พร้อมคำถามที่ขอคำตอบเป็น Markdown สำหรับจัดหัวข้อ ตัวหนา และตาราง\n\nหลังเลือกแอป AI ให้ตรวจว่า PDF แนบครบก่อนส่ง แล้วคัดลอกคำตอบกลับมาวางที่นี่").setPositiveButton("ส่ง PDF + คำถาม",(d,w)->sharePdf()).setNeutralButton("คัดลอกคำถาม",(d,w)->copy(prompt(),"คัดลอกคำถามแล้ว กรุณาแนบ PDF ทั้งไฟล์ในแอป AI")).setNegativeButton("ปิด",null).show();}
 void sharePdf(){saveDraft();Uri uri=Uri.parse("content://"+getPackageName()+".pdf/"+id+".pdf");Intent intent=new Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM,uri).putExtra(Intent.EXTRA_TEXT,prompt()).putExtra(Intent.EXTRA_SUBJECT,document.optString("title")).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);intent.setClipData(ClipData.newUri(getContentResolver(),"Guideline PDF ทั้งไฟล์",uri));try{startActivity(Intent.createChooser(intent,"เลือก ChatGPT หรือแอป AI ที่ติดตั้ง"));}catch(Exception e){toast("ส่งไฟล์ไม่สำเร็จ ใช้คัดลอกคำถามและแนบ PDF ในแอป AI เอง");}}
 void copy(String source,String success){try{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Pharmnote",source));toast(success);}catch(Exception e){toast("คัดลอกไม่ได้ ข้อความอาจยาวมาก ให้ใช้ส่งออกไฟล์ .md แทน");}}
 void copyAll(){copy(answer.getText().toString(),"คัดลอกข้อความ Markdown ทั้งหมดแล้ว");}
 void paste(){try{ClipData clip=((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).getPrimaryClip();if(clip==null||clip.getItemCount()==0){toast("ยังไม่มีข้อความในคลิปบอร์ด");return;}CharSequence content=clip.getItemAt(0).coerceToText(this);if(content==null){toast("คลิปบอร์ดไม่มีข้อความ");return;}acceptText(content.toString());}catch(Exception e){toast("อ่านคลิปบอร์ดไม่ได้ ลองกดค้างในช่องข้อความแล้วเลือกวาง");}}
 void acceptText(String incoming){if(incoming.isEmpty()){toast("ไม่มีข้อความให้นำเข้า");return;}if(answer.length()==0){answer.setText(incoming);showTab(false);return;}new AlertDialog.Builder(this).setTitle("นำเข้า "+incoming.length()+" ตัวอักษร").setItems(new String[]{"ต่อท้ายข้อความเดิม","แทนที่ข้อความเดิม"},(d,i)->{if(i==0)answer.append("\n\n"+incoming);else answer.setText(incoming);showTab(false);}).setNegativeButton("ยกเลิก",null).show();}
 void importText(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"text/plain","text/markdown","text/x-markdown"}).addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,11);}
 void exportText(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/markdown").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,document.optString("title").replaceAll("[\\\\/:*?\"<>|]","_")+"-"+mode+".md");startActivityForResult(i,12);}
 protected void onActivityResult(int request,int result,Intent intent){super.onActivityResult(request,result,intent);if(result!=RESULT_OK||intent==null||intent.getData()==null)return;Uri uri=intent.getData();final String source=answer.getText().toString();worker.execute(()->{try{if(request==11){String incoming;try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()){if(in==null)throw new IOException();byte[] buf=new byte[8192];int n,total=0;while((n=in.read(buf))!=-1){total+=n;if(total>2*1024*1024)throw new IOException();out.write(buf,0,n);}incoming=new String(out.toByteArray(),StandardCharsets.UTF_8);if(incoming.startsWith("\uFEFF"))incoming=incoming.substring(1);}final String text=incoming;runOnUiThread(()->{if(!destroyed)acceptText(text);});}else if(request==12){try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException();out.write(source.getBytes(StandardCharsets.UTF_8));}runOnUiThread(()->{if(!destroyed)toast("ส่งออก Markdown ทั้งหมดแล้ว");});}}catch(Exception e){runOnUiThread(()->{if(!destroyed)toast(request==11?"นำเข้าไม่ได้ ใช้ไฟล์ UTF-8 .md/.txt ขนาดไม่เกิน 2 MB":"ส่งออกไฟล์ไม่สำเร็จ");});}});}
 void details(){LinearLayout form=column();form.setPadding(dp(20),dp(8),dp(20),0);form.addView(text("หัวข้อที่อยากเน้น (เว้นว่างได้)",12));EditText topicInput=new EditText(this);topicInput.setText(topic);topicInput.setHint("เช่น การเลือกยา / regimen ที่สนใจ");form.addView(topicInput);form.addView(text("หน้า / ตารางอ้างอิง",12));EditText refs=new EditText(this);refs.setText(pages);refs.setHint("เช่น PDF หน้า 20–25, ตาราง 3");form.addView(refs);new AlertDialog.Builder(this).setTitle("รายละเอียดสรุป").setView(form).setNegativeButton("ยกเลิก",null).setPositiveButton("ใช้ข้อมูลนี้",(d,w)->{topic=topicInput.getText().toString();pages=refs.getText().toString();updateStatus();}).show();}
 void options(){new AlertDialog.Builder(this).setTitle("จัดการสรุป").setItems(new String[]{"รายละเอียด / หน้าอ้างอิง","คัดลอกข้อความทั้งหมด","ส่งออกเป็นไฟล์ Markdown (.md)","คัดลอกคำถามสำหรับ AI"},(d,i)->{if(i==0)details();if(i==1)copyAll();if(i==2)exportText();if(i==3)copy(prompt(),"คัดลอกคำถามแล้ว");}).show();}
 boolean save(boolean exit){if(answer.getText().toString().trim().isEmpty()){toast("เพิ่มคำตอบหรือบันทึกก่อน");return false;}try{JSONObject note=new JSONObject(previousNote.toString());JSONObject values=snapshot();for(String key:new String[]{"topic","answer","pages","reviewed"})note.put(key,values.get(key));note.put("format","markdown").put("updated",System.currentTimeMillis());LibraryStore.saveNote(this,id,mode,note);previousNote=note;savedState=snapshot().toString();draft.delete();updateStatus();toast("บันทึกสรุปทั้งหมดแล้ว");if(reading)render();if(exit)finish();return true;}catch(Exception e){toast("บันทึกไม่สำเร็จ ข้อความยังอยู่ กรุณาส่งออกสำรอง");return false;}}
 void saveDraft(){if(draft==null||answer==null||!dirty())return;FileOutputStream stream=null;try{stream=draft.startWrite();stream.write(snapshot().toString().getBytes(StandardCharsets.UTF_8));draft.finishWrite(stream);}catch(Exception e){draft.failWrite(stream);}}
 protected void onPause(){saveDraft();super.onPause();}
 public void onBackPressed(){if(!dirty()){finish();return;}hideKeyboard();new AlertDialog.Builder(this).setTitle("มีข้อความที่ยังไม่บันทึก").setMessage("บันทึกเป็นสรุป หรือเก็บเป็นร่างแล้วกลับมาแก้ภายหลังได้").setPositiveButton("บันทึกแล้วออก",(d,w)->save(true)).setNegativeButton("เก็บร่างแล้วออก",(d,w)->{saveDraft();finish();}).setNeutralButton("กลับไปแก้",null).show();}
 protected void onDestroy(){destroyed=true;renderTicket++;worker.shutdownNow();if(preview!=null){preview.stopLoading();preview.destroy();}super.onDestroy();}
}
