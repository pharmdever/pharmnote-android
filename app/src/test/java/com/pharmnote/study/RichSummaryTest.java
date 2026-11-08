package com.pharmnote.study;
import org.junit.Test;
import static org.junit.Assert.*;
public class RichSummaryTest {
 @Test public void rendersHeadingsBoldTablesAndWarningColor(){String html=RichSummary.document("# ภาพรวม\n\n**ยาสำคัญ**\n\n## ข้อควรระวัง\n\n| รายการ | ข้อมูล |\n| --- | --- |\n| ยา | จาก PDF |\n");assertTrue(html.contains("<strong>ยาสำคัญ</strong>"));assertTrue(html.contains("<h2 class=warning>ข้อควรระวัง</h2>"));assertTrue(html.contains("<div class=table-scroll><table>"));}
 @Test public void rawHtmlAndDangerousLinksCannotBecomeActiveContent(){String html=RichSummary.body("<script>alert(1)</script>\n\n[unsafe](javascript:alert%281%29)\n\n![x](https://example.org/track.png)");assertFalse(html.contains("<script>"));assertFalse(html.contains("href=\"javascript:"));assertFalse(html.contains("<img"));}
 @Test public void acceptsOuterMarkdownFenceAndPreservesLongTail(){String longText="```markdown\n# Title\n\n"+"Long complete paragraph.\n\n".repeat(12000)+"TAIL_SENTINEL\n```";String html=RichSummary.body(longText);assertTrue(html.contains("<h1>Title</h1>"));assertTrue(html.contains("TAIL_SENTINEL"));assertFalse(html.contains("<pre><code>"));}
 @Test public void promptRequestsWholeFileAndNoGuessedDoses(){String p=StudyPrompt.create("Breast Cancer","2026","regimen","หน้า 7");assertTrue(p.contains("ทั้งเอกสาร"));assertTrue(p.contains("Markdown"));assertTrue(p.contains("หน้า 7"));assertTrue(p.contains("ไม่เติมขนาดยา"));}
}
