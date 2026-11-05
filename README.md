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

รุ่น release ใช้ package `com.pharmnote.study` และต้องใช้ signing key เดิมตลอด ถ้าเปลี่ยน key จะติดตั้งทับรุ่นเก่าไม่ได้

สร้าง key บนคอมพิวเตอร์ของคุณด้วย JDK (โปรแกรมจะถามรหัสผ่าน):

```sh
keytool -genkeypair -keystore pharmnote-release.jks -alias pharmnote -keyalg RSA -keysize 3072 -validity 10000
```

เก็บไฟล์ key และรหัสผ่านสำรองไว้ในที่ส่วนตัว ห้าม commit เข้า repo และห้ามโพสต์ key ใน issues/chat ที่เปิดเผย

สร้างข้อความ base64 ในเครื่อง แล้วคัดลอกเข้า GitHub Secret:

```sh
python -c "import base64,pathlib; pathlib.Path('pharmnote.base64.txt').write_text(base64.b64encode(pathlib.Path('pharmnote-release.jks').read_bytes()).decode())"
```

Repo > Settings > Secrets and variables > Actions > New repository secret:

| Secret | ค่า |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | ข้อความจาก pharmnote.base64.txt |
| `ANDROID_STORE_PASSWORD` | รหัสผ่าน keystore |
| `ANDROID_KEY_ALIAS` | pharmnote |
| `ANDROID_KEY_PASSWORD` | รหัสผ่าน key (มักเหมือน store password) |

ลบไฟล์ base64 ในเครื่องหลังตั้ง secret และสำรอง key จริงไว้ ถ้า GitHub จำกัด Actions ให้เปิดใช้งานก่อน Workflow ออก release ใช้ GITHUB_TOKEN ของงานเพื่อเขียน Releases

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

รุ่น release ต้องตั้ง environment variables ตาม `app/build.gradle`: ANDROID_KEYSTORE_PATH, ANDROID_STORE_PASSWORD, ANDROID_KEY_ALIAS, ANDROID_KEY_PASSWORD และ RELEASE_REPOSITORY=owner/repo

## สถานะการตรวจสอบ

ตรวจ XML, รูปแบบ workflow YAML, Python release metadata และความสอดคล้องเลขเวอร์ชันแล้ว สภาพแวดล้อมที่เตรียมโปรเจกต์ไม่มี Android SDK/Gradle/JDK compiler จึงยังไม่ได้ compile, ทดสอบ emulator หรือสร้าง APK จริง Workflow ต้องรันบน GitHub และผ่านก่อนดาวน์โหลด APK ได้
