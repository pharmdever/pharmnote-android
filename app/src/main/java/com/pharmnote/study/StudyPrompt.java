package com.pharmnote.study;

final class StudyPrompt {
 static String create(String title,String year,String mode,String topic){
  if(!"regimen".equals(mode)&&!"explain".equals(mode))throw new IllegalArgumentException("Select a study mode");
  return "โหมดที่เลือก: "+("regimen".equals(mode)?"สรุป regimen":"อธิบายเนื้อหา")+"\nห้ามเปลี่ยนโหมดเอง\n"+
   "ข้อมูลชื่อ/ปีที่ผู้ใช้กรอก (ต้องตรวจจาก PDF จริง): "+title+" / "+year+"\n"+
   "ไฟล์แนบ: PDF ต้นฉบับทั้งไฟล์\nหัวข้อที่อยากเน้น/ขอบเขตเฉพาะ: "+(topic==null||topic.trim().isEmpty()?"ทั้งเอกสาร แบ่งทำตามหัวข้อหากยาว":topic.trim())+"\n\n"+PromptRules.TEXT;
 }
 private StudyPrompt(){}
}
