package com.pharmnote.study;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.ext.gfm.tables.TablesExtension;
import java.util.*;

/** Index real Markdown headings, not text that happens to contain a # in code or tables. */
final class SummaryHeadings {
 static final class Entry {
  final String id,title,context;final int level,index;
  Entry(int index,int level,String title,String context){this.index=index;this.id="pn-heading-"+index;this.level=level;this.title=title;this.context=context;}
  public String toString(){return title;}
 }
 static List<Entry> extract(String source){
  Node root=Parser.builder().extensions(Collections.singletonList(TablesExtension.create())).build().parse(RichSummary.normalize(source));
  ArrayList<Entry> entries=new ArrayList<>();ArrayList<Entry> parents=new ArrayList<>();
  root.accept(new AbstractVisitor(){public void visit(Heading heading){
   StringBuilder title=new StringBuilder();heading.accept(new AbstractVisitor(){public void visit(Text text){title.append(text.getLiteral());}public void visit(Code code){title.append(code.getLiteral());}public void visit(SoftLineBreak n){title.append(' ');}public void visit(HardLineBreak n){title.append(' ');}});
   while(!parents.isEmpty()&&parents.get(parents.size()-1).level>=heading.getLevel())parents.remove(parents.size()-1);
   String context=parents.isEmpty()?(entries.isEmpty()?"หัวข้อแรก":"ก่อนหน้า: "+entries.get(entries.size()-1).title):parents.get(parents.size()-1).title;
   String text=title.toString().trim();Entry entry=new Entry(entries.size(),heading.getLevel(),text.isEmpty()?"หัวข้อไม่มีชื่อ":text,context);entries.add(entry);parents.add(entry);
  }});
  return entries;
 }
 static String inlineContents(String source){List<Entry> entries=extract(source);if(entries.isEmpty())return "";StringBuilder html=new StringBuilder("<details class=contents id=pn-top><summary>สารบัญ · ").append(entries.size()).append(" หัวข้อ</summary><nav aria-label=สารบัญ>");for(Entry e:entries){html.append("<a class=toc-level-").append(e.level).append(" href=\"#").append(e.id).append("\">").append(escape(e.title)).append("</a>");}return html.append("</nav></details>").toString();}
 private static String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
 private SummaryHeadings(){}
}
