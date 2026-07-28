# TT Talep Destek Sistemi (Request Management System)

Monad Yazılım staj projesi — müşteri taleplerinin toplandığı, Ürün Sorumlusu (PO)
tarafından Etki ve Aciliyet kriterlerine göre (SIMPLE veya AHP yöntemiyle)
önceliklendirildiği ve yazılım ekibi için görevlere dönüştürüldüğü web tabanlı sistem.

## Teknolojiler
- Java 21, Spring Boot 3.5, Vaadin Flow 24
- Spring Data JPA (Hibernate)
- H2 (geliştirme) / Oracle veya PostgreSQL (üretim)
- BCrypt şifreleme, Strategy Pattern (SIMPLE / AHP skorlama)

## Çalıştırma (Windows) — EN KOLAY YOL
1. Bilgisayarda Java 21 kurulu olsun (adoptium.net → Temurin JDK 21)
2. **RUN.bat dosyasına çift tıkla** (Maven proje içinde gömülü, kurulum GEREKMEZ)
3. İlk çalıştırma kütüphaneleri indirdiği için 3-5 dk sürer
4. Tarayıcı: http://localhost:8080

## Demo Kullanıcılar (şifre: 1234)
| E-posta | Rol |
|---|---|
| musteri@demo.com | CUSTOMER |
| po@demo.com | PRODUCT_OWNER |
| dev@demo.com | DEVELOPER |
| admin@demo.com | ADMIN |
| topraktugce203@gmail.com | ADMIN (şifre sıfırlama mail testi için) |

## Yeni Özellikler (v2)
- Açılış (landing) sayfası: tanıtım + özellik kartları + Sıkça Sorulan Sorular (Accordion)
- Müşteri self-servis kayıt (`/register`) + hoş geldin e-postası
- Şifremi Unuttum: geçici şifre üretilir ve e-posta ile gönderilir
  (Gmail uygulama şifresi ayarlanmadıysa geçici şifre ekranda gösterilir, akış bozulmaz)
- Admin: Kullanıcı Yönetimi ekranı (rol değiştirme, aktif/pasif)
- Rol bazlı giriş yönlendirmesi ve ikonlu menü/tasarım

## Gmail ayarı (şifre sıfırlama maili için)
1. Google hesabında 2 Adımlı Doğrulama'yı aç
2. myaccount.google.com → Güvenlik → Uygulama Şifreleri → yeni şifre oluştur
3. `application.properties` içindeki `spring.mail.password` alanına bu 16 haneli şifreyi yaz

## PostgreSQL'e bağlanma (staj gereksinimi)
1. postgresql.org'dan PostgreSQL kur (pgAdmin ile birlikte gelir)
2. pgAdmin'de `talepdb` adında boş veritabanı oluştur
3. `sql/01_create_tables_postgres.sql` sonra `sql/02_mock_data_postgres.sql` çalıştır
4. `src/main/resources/application-postgres.properties` içine şifreni yaz
5. RUN.bat'ı düzenle: komutun sonuna ` -Dspring-boot.run.profiles=postgres` ekle

Not: PL/SQL Developer bir **Oracle** aracıdır, PostgreSQL ile çalışmaz.
PostgreSQL için pgAdmin kullan.

## Mimari
```
Vaadin View  ->  Service (iş mantığı)  ->  Repository (JPA)  ->  DB
                     |
              ScoringStrategy (Strategy Pattern)
              ├── SimpleScoringStrategy: skor = aciliyet × etki (1–25)
              └── AhpScoringStrategy:    Saaty ikili karşılaştırma, CR ≤ 0.10
```

## Ekranlar
1. **Müşteri** (`/`) — talep oluşturma formu + kendi taleplerini izleme
2. **PO Paneli** (`/po`) — talepleri listeleme/filtreleme, SIMPLE/AHP önceliklendirme,
   tek tuşla göreve dönüştürme (bir talepten birden fazla görev açılabilir — 1:N)
3. **Görev Panosu** (`/dev`) — yazılımcının görevleri, durum akışı BACKLOG→ASSIGNED→IN_PROGRESS→TESTING→DONE
4. **Dashboard** (`/dashboard`) — istatistik kartları + son aktiviteler (activity_log)
5. **Bildirimler** (`/notifications`)

## Durum Akışı
Talep: NEW → UNDER_REVIEW → PRIORITIZED → CONVERTED (veya REJECTED)
Görev: BACKLOG → ASSIGNED → IN_PROGRESS → TESTING → DONE
Her durum değişikliği request_status_history + activity_log tablolarına yazılır,
müşteriye bildirim düşer.
