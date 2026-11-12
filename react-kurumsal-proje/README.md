# Kurumsal Yönetim Paneli

Personel ve araç kayıtlarını yöneten React uygulaması. Arayüz React Router ile sayfalara ayrılmıştır ve Spring Boot API'sine bağlanır.

## Geliştirme

```bash
npm install
npm run dev
```

Geliştirme sunucusu `/api` isteklerini `http://localhost:8080` adresine yönlendirir. Önce `spring-exception-handler-kurumsal` backend uygulamasını başlatın.

Farklı bir API adresi için `VITE_API_BASE_URL` ortam değişkenini ayarlayın; varsayılan değer `/api`'dir.

## Ekranlar

- `/`: Personel ve araç birleşik tablosu. Personel ID, ad, soyad, e-posta, araç ID, plaka, marka, model ve araç çıkış tarihi alanlarının tamamı filtrelenebilir.
- `/personeller`: Personel oluşturma, listeleme, güncelleme ve silme.
- `/arabalar`: Araç oluşturma, listeleme, güncelleme ve silme; araç sorumlusu personel olarak seçilir.
- `/garaj`: Personelin kullandığı aracın garaj giriş ve çıkış tarih/saat kayıtları. Çıkış bilgileri boş olan araçlar garajda görünür.

Üretim paketi `npm run build`, kod kontrolleri `npm run lint` ile çalıştırılır.
