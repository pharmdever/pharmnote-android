package com.pharmnote.study;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension;
import java.util.*;
import java.util.regex.*;

/** Markdown is data: raw HTML is escaped and JavaScript is never required. */
final class RichSummary {
 static String normalize(String source){
  String s=source==null?"":source.trim();int newline=s.indexOf('\n');
  if(newline>=0&&s.endsWith("```")){String first=s.substring(0,newline).trim();if(first.equals("```markdown")||first.equals("```md")||first.equals("```"))return s.substring(newline+1,s.length()-3).trim();}
  return s;
 }
 static String body(String source){
  List<org.commonmark.Extension> extensions=Collections.singletonList(TablesExtension.create());
  Parser parser=Parser.builder().extensions(extensions).build();
  HtmlRenderer renderer=HtmlRenderer.builder().extensions(extensions).escapeHtml(true).sanitizeUrls(true).build();
  String html=renderer.render(parser.parse(normalize(source)));
  html=ResponsiveTables.render(html);
  Pattern heading=Pattern.compile("<h([1-6])>(.*?)</h\\1>",Pattern.DOTALL);Matcher m=heading.matcher(html);StringBuffer out=new StringBuffer();
  while(m.find()){String text=m.group(2).toLowerCase(Locale.ROOT);boolean warning=text.contains("ระวัง")||text.contains("ข้อห้าม")||text.contains("warning")||text.contains("contraindication");m.appendReplacement(out,Matcher.quoteReplacement("<h"+m.group(1)+(warning?" class=warning":"")+">"+m.group(2)+"</h"+m.group(1)+">"));}m.appendTail(out);
  return out.toString().replaceAll("<img\\b[^>]*>","<span class=image-note>[รูปภาพภายนอกไม่ถูกโหลด]</span>");
 }
 static String document(String source){return "<!doctype html><html lang=th><head><meta charset=utf-8><meta name=viewport content='width=device-width,initial-scale=1'><meta http-equiv=Content-Security-Policy content=\"default-src 'none'; style-src 'unsafe-inline'\"><style>"+
  "*{box-sizing:border-box;min-width:0}html,body{width:100%;margin:0}body{padding:16px 16px 40px;background:#fffef9;color:#263d35;font:18px/1.8 sans-serif;overflow-wrap:anywhere;word-wrap:break-word}h1,h2,h3,h4,h5,h6{color:#315c4d;line-height:1.5;margin:1.35em 0 .55em;font-weight:700;overflow-wrap:anywhere}h1{font-size:1.55em;border-bottom:2px solid #dce7d3;padding-bottom:.4em}h2{font-size:1.25em;background:#eef4e7;padding:.4em .6em;border-radius:10px}h3{font-size:1.12em}h4,h5,h6{font-size:1em}.warning{color:#965032;background:#fff2df}p{margin:.6em 0 1em}strong{font-weight:700;color:#183f32}em{color:#657857}ul,ol{padding-left:1.35em}li{padding:.15em 0}blockquote{margin:1em 0;padding:.2em .8em;border-left:4px solid #a4b78a;background:#f0f4e9;color:#55684b;border-radius:0 8px 8px 0}hr{border:0;border-top:1px solid #dce5d6;margin:1.5em 0}a{color:#286c57;text-decoration:underline;overflow-wrap:anywhere}code{font-size:.88em;background:#eef1e9;padding:.12em .3em;border-radius:4px;white-space:pre-wrap;word-break:break-word}pre{max-width:100%;padding:12px;background:#eef1e9;border-radius:10px;line-height:1.6;white-space:pre-wrap;word-break:break-word;overflow-wrap:anywhere}pre code{padding:0}.table-wrap{width:100%;margin:1em 0;border:1px solid #dce5d6;border-radius:12px}table{border-collapse:collapse;table-layout:fixed;width:100%;max-width:100%;font-size:.92em;line-height:1.75}th,td{padding:10px 11px;border:1px solid #e1e7da;text-align:left;vertical-align:top;white-space:normal;word-break:break-word;overflow-wrap:anywhere}.table-two th:first-child,.table-two td:first-child{width:38%}th{background:#e9f0df;color:#284d3e;font-weight:700}tr:nth-child(even){background:#f6f8f0}.table-cards{display:block;width:100%;margin:1em 0}.table-record{width:100%;border:1px solid #dce5d6;border-radius:14px;padding:4px 14px;margin:0 0 12px;background:#f6f8f0}.table-value{padding:10px 0;border-bottom:1px solid #e1e7da}.table-value:last-child{border-bottom:0}.cell-label{font-size:.82em;line-height:1.5;color:#617650;font-weight:700;margin-bottom:4px}.cell-content{font-size:1em;line-height:1.75;overflow-wrap:anywhere;word-break:break-word}.image-note{font-size:.85em;color:#86947c}</style></head><body>"+body(source)+"</body></html>";}
 private RichSummary(){}
}
