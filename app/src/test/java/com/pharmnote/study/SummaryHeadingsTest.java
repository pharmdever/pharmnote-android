package com.pharmnote.study;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class SummaryHeadingsTest {
 @Test public void duplicateThaiHeadingsHaveDistinctMatchingAnchors(){String source="# สรุป\n\n## **สูตรยา**\n\n### ช่วงรักษา\n\n## สูตรยา\n";List<SummaryHeadings.Entry> entries=SummaryHeadings.extract(source);assertEquals(4,entries.size());assertEquals("สูตรยา",entries.get(1).title);assertEquals("สูตรยา",entries.get(3).title);assertNotEquals(entries.get(1).id,entries.get(3).id);String html=RichSummary.document(source);for(SummaryHeadings.Entry e:entries){assertTrue(html.contains("id=\""+e.id+"\""));assertTrue(html.contains("href=\"#"+e.id+"\""));}assertEquals("สูตรยา",entries.get(2).context);}
 @Test public void fencedCodeAndTableTextAreNotInventedHeadings(){String source="Title\n=====\n\n```text\n# not a heading\n```\n\n| Item | Detail |\n| --- | --- |\n| # not a heading | value |\n\n## Actual heading\n";List<SummaryHeadings.Entry> entries=SummaryHeadings.extract(source);assertEquals(2,entries.size());assertEquals("Title",entries.get(0).title);assertEquals("Actual heading",entries.get(1).title);}
 @Test public void generatedContentsEscapesTitlesAndOnlyLinksLocally(){String html=SummaryHeadings.inlineContents("# <script>alert(1)</script>\n\n## [Title](https://example.org/)\n");assertFalse(html.contains("<script>"));assertFalse(html.contains("href=\"https://"));assertTrue(html.contains("href=\"#pn-heading-1\""));}
 @Test public void plainNotesHaveNoFalseContents(){assertTrue(SummaryHeadings.extract("ordinary text\nno Markdown headings").isEmpty());assertEquals("",SummaryHeadings.inlineContents("ordinary text"));}
}
