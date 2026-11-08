package com.pharmnote.study;

import android.content.Context;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

final class LibraryStore {
 static synchronized JSONArray read(Context context)throws Exception{
  File file=new File(context.getFilesDir(),"library.json");if(!file.exists()&&!new File(file+".bak").exists())return new JSONArray();
  return new JSONArray(new String(new AtomicFile(file).readFully(),StandardCharsets.UTF_8));
 }
 static synchronized void write(Context context,JSONArray docs)throws Exception{
  AtomicFile file=new AtomicFile(new File(context.getFilesDir(),"library.json"));FileOutputStream stream=null;
  try{stream=file.startWrite();stream.write(docs.toString().getBytes(StandardCharsets.UTF_8));file.finishWrite(stream);}catch(Exception e){file.failWrite(stream);throw e;}
 }
 static synchronized JSONObject find(Context context,String id)throws Exception{JSONArray docs=read(context);for(int i=0;i<docs.length();i++){JSONObject d=docs.getJSONObject(i);if(d.optString("id").equals(id))return d;}throw new IOException("Document missing");}
 static synchronized void saveNote(Context context,String id,String mode,JSONObject note)throws Exception{
  JSONArray docs=read(context);boolean found=false;for(int i=0;i<docs.length();i++){JSONObject d=docs.getJSONObject(i);if(!id.equals(d.optString("id")))continue;JSONObject notes=d.optJSONObject("notes");if(notes==null)notes=new JSONObject();notes.put(mode,note);d.put("notes",notes);found=true;break;}if(!found)throw new IOException("Document missing");write(context,docs);
 }
 private LibraryStore(){}
}
