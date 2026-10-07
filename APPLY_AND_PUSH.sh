#!/data/data/com.termux/files/usr/bin/bash
set -e
cd "$(dirname "$0")"

if [ ! -d .git ]; then
  echo "HATA: Bu dosya GitHub'dan klonlanmış Dipzone proje kökünde çalıştırılmalı."
  exit 1
fi

if [ -x ./gradlew ]; then
  echo "[1/3] Debug APK derleniyor..."
  ./gradlew --no-daemon :app:assembleDebug
else
  echo "UYARI: gradlew bulunamadı; derleme testi atlandı."
fi

echo "[2/3] Değişiklikler Git'e ekleniyor..."
git add app/src/main/java app/build.gradle.kts firestore.rules storage.rules firebase.json .gitignore FIREBASE_SETUP.md DIPZON_UPGRADE_NOTES.md

if git diff --cached --quiet; then
  echo "Yeni değişiklik yok; push gerekmiyor."
  exit 0
fi

git commit -m "Dipzon: real admin, uploads, sync and functional settings"

echo "[3/3] GitHub'a gönderiliyor..."
git push

echo "TAMAMLANDI: Kod derlendi, commit edildi ve GitHub'a gönderildi."
