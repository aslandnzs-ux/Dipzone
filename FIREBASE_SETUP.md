# Dipzon Firebase production setup

Kod artık Firebase Auth + Firestore + Storage kullanacak şekilde hazırdır. Gerçek ağ üzerinden içerik yayınlamak için bir defalık Firebase projesi kurulumu gerekir.

1. Firebase Console'da Android uygulamasını `com.aistudio.dipzon.vertical` paket adıyla ekle.
2. `google-services.json` dosyasını `app/google-services.json` konumuna koy. Bu dosyayı herkese açık GitHub deposuna koyma.
3. Authentication > Sign-in method altında Email/Password yöntemini aç ve yönetici hesabını oluştur.
4. Authentication > Users ekranından yöneticinin UID değerini kopyala.
5. Firestore'da `admins/{UID}` belgesi oluştur ve `enabled` alanını boolean `true` yap.
6. Firestore Database ve Storage'ı etkinleştir.
7. Bu paketteki `firestore.rules` ve `storage.rules` kurallarını Firebase'e deploy et.

Firebase CLI kullanıyorsan proje kökünde:

    firebase login
    firebase use --add
    firebase deploy --only firestore:rules,storage

Bundan sonra Admin panelinde Firebase e-posta/şifresiyle giriş yapılır. Telefonda seçilen video Firebase Storage'a yüklenir, bölüm URL'si Firestore'a yazılır ve diğer cihazlar yayınlanan içeriği gerçek zamanlı olarak Room veritabanlarına senkronlar.

Not: Google Play ödeme/jeton sistemi bu sürümde özellikle bağlanmamıştır. Profil ekranı artık var olmayan VIP/jeton bakiyesi göstermiyor.
