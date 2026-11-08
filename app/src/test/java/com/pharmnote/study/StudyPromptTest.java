package com.pharmnote.study;
import org.junit.Test;
import static org.junit.Assert.*;
public class StudyPromptTest {
 @Test public void selectedModeAndReviewSafeguardsAreIncluded(){String p=StudyPrompt.create("Guideline","2026","regimen","renal impairment");assertTrue(p.startsWith("โหมดที่เลือก: สรุป regimen"));for(String item:new String[]{"footnote","คำแนะนำหลัก","ข้อยกเว้น","ข้อกำหนดเมื่อเลือกใช้","renal impairment","ห้ามเดา","IF/THEN/AND/OR"})assertTrue(item,p.contains(item));assertFalse(p.contains("[เลือกหนึ่งอย่างให้ชัด"));}
 @Test public void explanationModeDoesNotSilentlyBecomeRegimen(){String p=StudyPrompt.create("Guideline","2026","explain","");assertTrue(p.startsWith("โหมดที่เลือก: อธิบายเนื้อหา"));}
 @Test(expected=IllegalArgumentException.class) public void unknownModeIsRejected(){StudyPrompt.create("Guideline","2026","auto","");}
}
