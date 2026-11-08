package com.pharmnote.study;

import java.util.*;
import java.util.regex.*;

/** Only rearrange already-sanitized generated HTML; never rewrite clinical cell values. */
final class ResponsiveTables {
 static String render(String html){Matcher tables=Pattern.compile("<table>(.*?)</table>",Pattern.DOTALL).matcher(html);StringBuffer result=new StringBuffer();while(tables.find()){String original=tables.group();String body=tables.group(1);List<String> headers=cells(section(body,"thead"),"th");String replacement;
  if(headers.size()<=2){replacement="<div class=table-wrap><table class=table-two>"+body+"</table></div>";}
  else{StringBuilder cards=new StringBuilder("<section class=table-cards role=table>");Matcher rows=Pattern.compile("<tr>(.*?)</tr>",Pattern.DOTALL).matcher(section(body,"tbody"));int count=0;while(rows.find()){List<String> values=cells(rows.group(1),"td");if(values.size()!=headers.size()){cards.setLength(0);break;}cards.append("<div class=table-record role=rowgroup>");for(int i=0;i<headers.size();i++){cards.append("<div class=table-value role=row><div class=cell-label role=rowheader>").append(headers.get(i)).append("</div><div class=cell-content role=cell>").append(values.get(i)).append("</div></div>");}cards.append("</div>");count++;}replacement=cards.length()>0&&count>0?cards.append("</section>").toString():"<div class=table-wrap><table class=table-two>"+body+"</table></div>";}
  tables.appendReplacement(result,Matcher.quoteReplacement(replacement));}tables.appendTail(result);return result.toString();
 }
 private static String section(String html,String tag){Matcher m=Pattern.compile("<"+tag+">(.*?)</"+tag+">",Pattern.DOTALL).matcher(html);return m.find()?m.group(1):"";}
 private static List<String> cells(String html,String tag){ArrayList<String> values=new ArrayList<>();Matcher m=Pattern.compile("<"+tag+"(?:\\s[^>]*)?>(.*?)</"+tag+">",Pattern.DOTALL).matcher(html);while(m.find())values.add(m.group(1));return values;}
 private ResponsiveTables(){}
}
