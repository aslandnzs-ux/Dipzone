# Dipzon gerçek platform yükseltmesi

Bu paket mevcut tasarımı mümkün olduğunca koruyarak göstermelik alanları işlevsel hale getirir:

- Firebase e-posta/şifre tabanlı gerçek admin girişi + `admins/{uid}` yetki kontrolü
- Admin: dizi ekle/düzenle/sil, taslak/yayın durumu
- Admin: bölüm ekle/düzenle/sil
- Telefondan video seçme ve Firebase Storage'a yükleme
- Telefondan dizi posteri, arka planı ve bölüm kapağı seçip yükleme
- Firestore'a dizi/bölüm metadata yayınlama
- Diğer cihazlarda yayınlanmış içerikleri Firestore'dan gerçek zamanlı Room'a senkronlama
- Sabit sahte admin analitik rakamlarının kaldırılması; yalnız gerçek yerel olay sayaçları
- Sabit sahte arama geçmişi ve trend listesinin kaldırılması; gerçek arama geçmişi
- Sahte profil VIP/jeton/izleme süresi değerlerinin kaldırılması
- Video kalitesi, altyazı dili, veri tasarrufu ve bildirim tercihlerinin kalıcı Room ayarı olması
- Sahte video ilerleme simülasyonunun kaldırılması; video açılamazsa gerçek hata gösterimi
- Sahte bildirim metinlerinin kaldırılması
- Moderasyon sekmesinin tüm gerçek yorumları göstermesi ve silebilmesi
- Room v2 migration: eski demo kullanıcı aktivitesini temizler ve gerçek arama geçmişi tablosunu ekler

## Bilerek sonraya bırakılan tek ürün alanı
Google Play Billing / jeton satın alma sistemi henüz bağlanmadı. Kullanıcının isteğine göre satın almalar daha sonra eklenecek. Profil artık satın alma varmış gibi davranmıyor.

## Firebase
Gerçek ağ yayını için `FIREBASE_SETUP.md` dosyasındaki tek seferlik Firebase Console ayarları yapılmalıdır. Güvenlik kuralları pakete dahil edilmiştir.
