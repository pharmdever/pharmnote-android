package com.pharmnote.study;

import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.*;
import java.util.*;
import java.util.regex.*;

/** Color only explicitly labelled text; never infer a drug, dose or contraindication. */
final class SemanticHighlights {
 private static final Pattern TAG=Pattern.compile("\\[\\[(ยา|ขนาดยา|ข้อห้าม|ข้อควรระวัง|ติดตาม|ยังไม่ยืนยัน|drug|dose|contra|caution|monitor|uncertain)\\s*:\\s*([^\\[\\]\\r\\n]+?)\\]\\]",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
 private static final class Badge extends CustomNode {
  final String kind,label,value;
  Badge(String kind,String label,String value){this.kind=kind;this.label=label;this.value=value;}
 }
 private static String kind(String label){switch(label.toLowerCase(Locale.ROOT)){case "ยา":case "drug":return "drug";case "ขนาดยา":case "dose":return "dose";case "ข้อห้าม":case "contra":return "contra";case "ข้อควรระวัง":case "caution":return "caution";case "ติดตาม":case "monitor":return "monitor";default:return "uncertain";}}
 private static String label(String kind){switch(kind){case "drug":return "ยา";case "dose":return "ขนาดยา";case "contra":return "ข้อห้าม";case "caution":return "ข้อควรระวัง";case "monitor":return "ติดตาม";default:return "ยังไม่ยืนยัน";}}
 private static boolean protectedContext(Node node){for(Node n=node.getParent();n!=null;n=n.getParent())if(n instanceof Link||n instanceof Image||n instanceof Code||n instanceof FencedCodeBlock||n instanceof IndentedCodeBlock)return true;return false;}
 static void apply(Node root){root.accept(new AbstractVisitor(){public void visit(Text node){if(protectedContext(node))return;String value=node.getLiteral();Matcher m=TAG.matcher(value);int end=0;boolean found=false;while(m.find()){String content=m.group(2).trim();if(content.isEmpty())continue;found=true;if(m.start()>end)node.insertBefore(new Text(value.substring(end,m.start())));String kind=kind(m.group(1));node.insertBefore(new Badge(kind,label(kind),content));end=m.end();}if(found){if(end<value.length())node.insertBefore(new Text(value.substring(end)));node.unlink();}}});}
 static NodeRenderer renderer(HtmlNodeRendererContext context){return new NodeRenderer(){public Set<Class<? extends Node>> getNodeTypes(){return Collections.<Class<? extends Node>>singleton(Badge.class);}public void render(Node node){Badge badge=(Badge)node;HtmlWriter out=context.getWriter();out.tag("span",Collections.singletonMap("class","semantic semantic-"+badge.kind));out.tag("span",Collections.singletonMap("class","semantic-label"));out.text(badge.label+": ");out.tag("/span");out.tag("strong",Collections.singletonMap("class","semantic-value"));out.text(badge.value);out.tag("/strong");out.tag("/span");}};}
 static String plainLabels(String source){Matcher m=TAG.matcher(source);StringBuffer out=new StringBuffer();while(m.find())m.appendReplacement(out,Matcher.quoteReplacement(label(kind(m.group(1)))+": "+m.group(2).trim()));m.appendTail(out);return out.toString();}
 static String legend(){return "<details class=semantic-key><summary>สัญลักษณ์สีในสรุป</summary><div><span class='semantic semantic-drug'>ยา</span> <span class='semantic semantic-dose'>ขนาดยา</span> <span class='semantic semantic-contra'>ข้อห้าม</span> <span class='semantic semantic-caution'>ข้อควรระวัง</span> <span class='semantic semantic-monitor'>ติดตาม</span> <span class='semantic semantic-uncertain'>ยังไม่ยืนยัน</span></div></details>";}
 static String css(){return ".semantic{display:inline;padding:.08em .3em;border-radius:5px;box-decoration-break:clone;-webkit-box-decoration-break:clone;white-space:normal;overflow-wrap:anywhere;word-break:break-word;line-height:1.95}.semantic-label{font-size:.77em;font-weight:600}.semantic .semantic-value{color:inherit;font-weight:700}.semantic-drug{color:#215b73;background:#e7f2f7}.semantic-dose{color:#67418c;background:#f1eafa}.semantic-contra{color:#99383f;background:#fbecee}.semantic-caution{color:#82520f;background:#fbf0dc}.semantic-monitor{color:#376244;background:#e8f3e8}.semantic-uncertain{color:#535d6b;background:#edf0f4}.semantic-key{padding:9px 12px;margin:0 0 15px;border:1px solid #dfe6dc;border-radius:10px;background:#f6f8f1}.semantic-key summary{font-size:.85em;color:#6b7a60;font-weight:600;cursor:pointer}.semantic-key div{margin-top:9px;line-height:2.15;font-size:.85em}";}
 private SemanticHighlights(){}
}
