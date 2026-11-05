# Pharmnote for Android

แอป Android native สำหรับ Android 8.0 ขึ้นไป ไม่ใช้ WebView

## ใช้งาน

- นำเข้า PDF สูงสุด 50 MB คัดลอกมาเก็บในพื้นที่ส่วนตัวของแอป
- ตั้งชื่อ แยกหมวดโรค ระบุองค์กร/ปี ค้นหา และรายการโปรด
- อ่าน PDF ทีละหน้า หรือเปิดกับแอป PDF อื่นเพื่อขยายอ่าน
- เก็บคำอธิบาย / สรุป regimen คู่กับเอกสาร พร้อมหัวข้อ หน้าอ้างอิง และสถานะตรวจทาน
- คัดลอกคำถาม หรือแชร์คำถามพร้อม PDF ไปยังแอป AI ที่ติดตั้ง ผู้ใช้ตรวจว่าแอปปลายทางรับข้อความและไฟล์ครบก่อนส่ง
- นำคำตอบกลับมาวางและบันทึก แอปยังไม่เรียก AI API โดยตรง
- เมนู ⋮ > สำรอง ส่งออก ZIP ที่รวม PDF และ library.json (ยังไม่มีปุ่มนำเข้า ZIP ในแอป)
- เมนู ⋮ > ตรวจสอบอัปเดต เช็ก GitHub Releases แล้วเปิดหน้าดาวน์โหลด

PDF และสรุปเก็บในเครื่อง ไม่มีการส่งข้อมูลไป GitHub ระบบอัปเดตอ่านเฉพาะข้อมูล release โดยไม่ส่งเอกสาร ข้อมูลถูกลบเมื่อถอนแอป ควรสำรองก่อนถอน/เปลี่ยนเครื่อง

## สร้าง APK ทดสอบด้วย GitHub

นำไฟล์ในโฟลเดอร์นี้ทั้งหมด รวม `.github` ขึ้นที่ root ของ repo

1. ไป Actions > Build Android APK > Run workflow (หรือ push main)
2. รอ build ผ่าน แล้วดาวน์โหลด artifact `Pharmnote-debug-apk`
3. แตก ZIP และติดตั้ง `app-debug.apk` บนมือถือ Android

รุ่น debug ใช้ package `com.pharmnote.study.debug` เพื่อแยกจากรุ่นใช้งานจริง แต่ละ GitHub runner อาจใช้ debug signing key ต่างกัน จึงไม่รับประกันการติดตั้ง debug ทับรุ่นก่อน ใช้รุ่น release สำหรับข้อมูลใช้งานจริงและอัปเดตต่อเนื่อง

## ตั้งค่ารุ่นใช้งานจริงและ signing key ถาวร (ครั้งเดียว)

รุ่น release ใช้ package `com.pharmnote.study` และ signing key ถาวรที่มีลายนิ้วมือใน `signing-certificate.sha256` สคริปต์ตรวจใบรับรองก่อน build เพื่อป้องกันเปลี่ยน key โดยไม่ตั้งใจ เก็บ key สำรองไว้ส่วนตัว ห้ามอัปโหลด key หรือ bundle เข้า repo

1. เปิดไฟล์ **ANDROID_SIGNING_BUNDLE.txt** จาก ZIP สำรองส่วนตัวที่เตรียมให้ คัดลอกข้อความทั้งหมด
2. ไป Repo > Settings > Secrets and variables > Actions > New repository secret
3. ตั้งชื่อ **ANDROID_SIGNING_BUNDLE** วางข้อความเป็นค่า แล้วกด Add secret
4. ไป Actions > **Publish signed APK** > Run workflow > เลือก main > Run workflow
5. เมื่อผ่าน ไป Releases > v0.1.0 แล้วดาวน์โหลด **Pharmnote.apk** เพื่อติดตั้ง

ตั้งค่า Secret ครั้งเดียว ทุกรุ่นถัดไปใช้ Secret เดิม ZIP สำรองรวม key และรหัสผ่าน ผู้ที่มีไฟล์นี้สามารถลงนามแอปได้ จึงต้องเก็บเป็นส่วนตัว อย่าส่งค่า Secret ใน issue, chat หรือ commit

รุ่น release แยกจาก debug APK ที่แจกก่อนหน้า ใช้รุ่น release เป็นแอปหลัก ข้อมูลของ debug จะไม่ย้ายมาอัตโนมัติ ทุกรุ่น release ถัดไปที่ใช้ key เดิมและ versionCode สูงกว่าจะติดตั้งทับโดยเก็บข้อมูลเดิม

## ออกรุ่นใหม่และอัปเดต

1. แก้โค้ดและทดสอบ
2. เพิ่ม `VERSION_CODE` เป็นจำนวนเต็มที่สูงกว่าทุกรุ่นก่อนหน้า และเปลี่ยน `VERSION_NAME` ใน `version.properties` เช่น 2 / 0.2.0
3. commit และ push โค้ด
4. สร้าง tag ชื่อตรงกับ VERSION_NAME เช่น `v0.2.0` แล้ว push tag
5. Workflow **Publish signed APK** จะ build, ตรวจลายเซ็น และสร้าง GitHub Release พร้อม `Pharmnote.apk`, `update.json`, `SHA256SUMS.txt`
6. ผู้ใช้กดตรวจอัปเดตในแอป หรือรอแอปเช็กเมื่อเปิด (ไม่เกินวันละครั้ง) จากนั้นเปิด Releases ดาวน์โหลด APK และยืนยันติดตั้งทับใน Android โดยไม่ถอนรุ่นเดิม

ตัวอย่างคำสั่ง:

```sh
git tag v0.2.0
git push origin v0.2.0
```

คง applicationId, signing key และรูปแบบข้อมูลเดิมเพื่อให้ข้อมูลอยู่ครบ ถ้าเปลี่ยนรูปแบบข้อมูลในอนาคตต้องเพิ่ม migration ก่อนออก release ห้ามใช้ debug APK แทน release APK หรือสร้าง tag เดิมซ้ำเพื่อเขียนทับรุ่นที่ออกแล้ว

ตัวตรวจอัปเดตใช้ GitHub API โดยไม่ฝัง token จึงรองรับการตรวจอัตโนมัติเมื่อ repo ที่เผยแพร่ APK เป็น **public** ถ้า repo เป็น private ให้เปิดหน้า Releases จากแอป แล้วลงชื่อเข้า GitHub เพื่อดาวน์โหลดเอง แอปแจ้งข้อผิดพลาดได้โดยยังอ่าน PDF ออฟไลน์ได้ตามปกติ สามารถเปลี่ยน owner/repo ที่เมนู ⋮ > ตั้งค่าแหล่งอัปเดต GitHub

ไม่ติดตั้งเงียบหรือบังคับอัปเดต Android เป็นผู้ขออนุญาตติดตั้งจากแหล่งดาวน์โหลดและตรวจลายเซ็น APK ในระหว่างติดตั้ง

## Build บนคอมพิวเตอร์

เปิดด้วย Android Studio (JDK 17, Android SDK 35) หรือใช้ Gradle 8.9:

```sh
gradle :app:assembleDebug
```

รุ่น release ต้องตั้ง environment variables ตาม `app/build.gradle`: ANDROID_KEYSTORE_PATH, ANDROID_STORE_PASSWORD, ANDROID_KEY_ALIAS, ANDROID_KEY_PASSWORD และ RELEASE_REPOSITORY=owner/repo (GitHub workflow ตั้งค่าให้จาก Secret เดียว)

## สถานะการตรวจสอบ

Debug APK คอมไพล์และ build ผ่านบน GitHub Actions แล้ว ตรวจ XML, workflow YAML และ metadata แล้ว ส่วน permanent signing มีการตรวจ key และการปฏิเสธ key ที่ลายนิ้วมือไม่ตรง ยังไม่ได้ทดสอบแอปบนมือถือจริง Release แรกต้องตั้ง Secret และรัน workflow ให้ผ่านก่อนดาวน์โหลด
