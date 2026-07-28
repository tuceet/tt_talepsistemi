# TT Talep Destek Sistemi (Request Management System)

Monad Yazılım staj projesi — müşteri taleplerinin toplandığı, Ürün Sorumlusu (PO)
tarafından Etki ve Aciliyet kriterlerine göre (SIMPLE veya AHP yöntemiyle)
önceliklendirildiği ve yazılım ekibi için görevlere dönüştürüldüğü web tabanlı sistem.

## Teknolojiler
- Java 21, Spring Boot 3.5, Vaadin Flow 24
- Spring Data JPA (Hibernate)
- H2 (geliştirme) / Oracle veya PostgreSQL (üretim)
- BCrypt şifreleme, Strategy Pattern (SIMPLE / AHP skorlama)


## Durum Akışı
Talep: NEW → UNDER_REVIEW → PRIORITIZED → CONVERTED (veya REJECTED)
Görev: BACKLOG → ASSIGNED → IN_PROGRESS → TESTING → DONE
Her durum değişikliği request_status_history + activity_log tablolarına yazılır,
müşteriye bildirim düşer.
