AKASH MODZ Wingo — Android Studio project

অন্তর্ভুক্ত:
- মূল Wingo HTML UI ও prediction logic
- Android 13+ notification permission ও native notification channel
- নতুন SIGNAL, WIN, LOSS নোটিফিকেশন
- Display over other apps settings shortcut (এটি signal notification-এর জন্য দরকার নেই)
- অ্যাপ background-এ গেলে Foreground Data Sync Service-এ hidden WebView দিয়ে একই HTML engine চালানোর চেষ্টা
- অ্যাপ সামনে এলে service বন্ধ করে UI engine আবার চালু

APK build:
1) Android Studio-তে এই ফোল্ডার Open করুন।
2) Gradle Sync করুন (ইন্টারনেট লাগবে; Android SDK 34/35 ইনস্টল থাকতে হবে)।
3) Build > Build Bundle(s) / APK(s) > Build APK(s)।

ব্যবহার:
1) অ্যাপ খুলে Big/Small অথবা Red/Green নির্বাচন করুন।
2) Wingo 30 Sec বা Wingo 1 Min Start করুন।
3) 🔔 ALLOW চাপুন এবং Android notification permission Allow করুন।
4) অ্যাপ background-এ পাঠালে foreground service চলার চেষ্টা করবে; status notification দেখাবে।
5) নতুন signal এবং WIN/LOSS notification engine-এর fetch/update অনুযায়ী আসবে।

গুরুত্বপূর্ণ সীমাবদ্ধতা ও সতর্কতা:
- এটি Android Studio SOURCE PROJECT; এখানে compile করা APK নেই।
- Foreground service চালু থাকলেও Android/OEM battery management, network/CORS/API outage, force-stop, reboot, notification permission, এবং Android-এর dataSync foreground-service time limits-এর কারণে সবসময় 100% background delivery নিশ্চিত করা যায় না। Android 15+ এ dataSync foreground service-এর সময়সীমা প্রযোজ্য।
- Hidden WebView service একই HTML fetchHistory/proxy endpoints ও prediction code ব্যবহার করে; API/proxy পরিবর্তন বা ব্লক হলে prediction/notification বন্ধ হতে পারে।
- ফোন Settings থেকে Force stop করলে service বন্ধ হয়ে যাবে; ব্যবহারকারীকে অ্যাপ আবার খুলে Start করতে হবে। Reboot auto-start যুক্ত করা হয়নি।
- Display over other apps permission আলাদা এবং এই notification feature-এর জন্য প্রয়োজন নেই; অপ্রয়োজনীয় overlay ব্যবহার করা হচ্ছে না।
- Prediction accuracy নিশ্চিত নয়; কোনো signal জেতার নিশ্চয়তা নয়।


DAILY PASSWORD LOCK:
- App open korlei password screen ashbe.
- Password format: English month short name (uppercase) + day number + @#
- Examples: NOV2@#, NOV3@#, DEC15@#.
- App device-er local date/time use kore password calculate kore.
- Password protidin automatic change hoy.
- Ei simple date-based lock strong security noy; APK reverse-engineer ba phone date bodle bypass kora sombhob.
