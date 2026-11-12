# Spring Boot Global Exception Handler – Kurumsal Eğitim Projesi

Bu proje, `@RestControllerAdvice` ve `@ExceptionHandler` konusunu gerçek bir katmanlı REST API üzerinden öğretmek için hazırlanmıştır.

## Teknolojiler

- Java 17
- Spring Boot 3.5.2
- Spring Web
- Spring Data JPA
- Jakarta Bean Validation
- PostgreSQL (uygulama)
- H2 in-memory veritabanı (testler)
- Maven
- JUnit 5 + MockMvc

Uygulama PostgreSQL'e, `araba` şemasına bağlanır. Testler ayrı bir H2 veritabanı kullanır.

## Proje katmanları

```text
com.kurum.personel
├── controller
│   └── PersonelController.java
├── service
│   └── PersonelService.java
├── repository
│   └── PersonelRepository.java
├── entity
│   └── Personel.java
├── dto
│   ├── request
│   │   ├── PersonelCreateRequest.java
│   │   └── PersonelUpdateRequest.java
│   └── response
│       ├── PersonelResponse.java
│       └── ApiErrorResponse.java
└── exception
    ├── BusinessException.java
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java
```

## Çalıştırma

Gereksinim: JDK 17, Maven ve PostgreSQL.

Varsayılan bağlantı `jdbc:postgresql://localhost:5434/mhuys?currentSchema=araba`, kullanıcı `postgres` şeklindedir. Parolayı kaynak koduna eklemeyin; uygulamayı başlatmadan önce ortam değişkeni olarak tanımlayın. PowerShell:

```powershell
$env:DB_PASSWORD = "<PostgreSQL parolanız>"
mvn spring-boot:run
```

Farklı bir bağlantı kullanmak için `DB_URL` ve `DB_USERNAME` ortam değişkenlerini de ayarlayın. Örneğin:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5434/mhuys?currentSchema=araba"
$env:DB_USERNAME = "postgres"
```

Uygulama `currentSchema=araba` ile PostgreSQL'deki `araba` şemasını kullanır. Hibernate `update` modu eksik entity tablolarını oluşturur ve mevcut tablolardaki verileri silmez. Mevcut bir tablo yapısı değiştirilecekse şema değişikliğini dağıtımdan önce ayrıca yedekleyip gözden geçirin; kurumsal üretim dağıtımlarında şema değişikliklerini sürümlü migration'larla yönetmeniz önerilir.

Testleri çalıştırmak için:

```bash
mvn test
```

## Loglama

Uygulama SLF4J API'si ve Spring Boot'un varsayılan Logback yapılandırmasıyla log üretir.
Loglar konsola ve `logs/spring-exception-handler.log` dosyasına yazılır. Dosyalar günlük ve
10 MB boyut sınırına göre sıkıştırılarak döndürülür; 30 günlük arşiv saklanır. Log klasörünü
değiştirmek için `LOG_PATH` ortam değişkenini ayarlayın. Servis işlemleri ve merkezi hata
yakalayıcı, işlem kimliği, HTTP yolu ve hata kodu gibi operasyonel bilgileri loglar; istek
gövdelerini ve kişisel verileri loglamaz.

IntelliJ IDEA kullanıyorsanız `pom.xml` dosyasını Maven projesi olarak açıp `PersonelApplication` sınıfını çalıştırabilirsiniz.

## API uçları

| Metot | Adres | Açıklama |
|---|---|---|
| GET | `/api/personeller` | Tüm personeller |
| GET | `/api/personeller/detayli` | Personelleri arabalarıyla birlikte listele |
| GET | `/api/personeller/ara` | Personel ve araba alanlarına göre filtrele |
| GET | `/api/personeller/{id}` | Personel getir |
| POST | `/api/personeller` | Personel ekle |
| PUT | `/api/personeller/{id}` | Personel güncelle |
| DELETE | `/api/personeller/{id}` | Personel sil |
| GET | `/api/arabalar` | Tüm arabalar |
| GET | `/api/arabalar/{id}` | Araba getir |
| POST | `/api/arabalar` | Arabayı personele bağlayarak ekle |
| PUT | `/api/arabalar/{id}` | Araba bilgilerini ve personelini güncelle |
| DELETE | `/api/arabalar/{id}` | Araba sil |

### Araba CRUD ve personel ilişkisi

Araba ekleme ve güncelleme isteklerinde `personelId` zorunludur. İlgili personel bulunamazsa API `404 Not Found` döndürür.

```http
POST http://localhost:8080/api/arabalar
Content-Type: application/json
```

```json
{
  "plaka": "34ABC123",
  "marka": "Toyota",
  "model": "Corolla",
  "aracCikisYili": "2022-01-01",
  "personelId": 1
}
```

Bir personelin arabaları varken personel silinmeye çalışılırsa ilişkiyi korumak için `409 Conflict` döner. Önce arabalar silinmeli veya başka personele atanmalıdır.

Personel ve arabalarını birlikte listelemek için `GET /api/personeller/detayli` çağrılır. Her personelin `arabalar` alanı, araba yoksa boş liste (`[]`) olarak döner.

Filtreli listeleme için `GET /api/personeller/ara` kullanılır. Aşağıdaki query parametrelerinin tümü isteğe bağlıdır; yalnızca ihtiyacınız olan bir veya birkaçını gönderebilirsiniz:

`id`, `ad`, `soyad`, `email`, `arabaId`, `plaka`, `marka`, `model`, `aracCikisYili`.

Metin filtreleri büyük/küçük harfe duyarsız kısmi eşleşir. Birden fazla filtre gönderildiğinde tümü aynı anda eşleşmelidir (AND). Araba alanlarından filtre verilirse personel, eşleşen en az bir arabası varsa listelenir ve cevabında yalnızca filtreye uyan arabaları yer alır.

```http
GET /api/personeller/ara?ad=Ay%C5%9Fe&marka=Toyota&model=Corolla
```

Tarih filtresi `YYYY-MM-DD` biçiminde gönderilir; örneğin `aracCikisYili=2022-01-01`.

### 1. Personel ekle – başarılı istek

```http
POST http://localhost:8080/api/personeller
Content-Type: application/json
```

```json
{
  "ad": "Ahmet",
  "soyad": "Yılmaz",
  "email": "ahmet@example.com"
}
```

Beklenen HTTP: `201 Created`.

### 2. Personel bulunamadı – 404

```http
GET http://localhost:8080/api/personeller/999
```

Service katmanındaki `orElseThrow`, `ResourceNotFoundException` fırlatır. Exception Controller'a kadar çıkar ve `GlobalExceptionHandler.handleNotFound` metoduna ulaşır.

Örnek cevap:

```json
{
  "timestamp": "2026-11-09T09:00:00",
  "status": 404,
  "error": "Not Found",
  "code": "RESOURCE_NOT_FOUND",
  "message": "Personel bulunamadı. ID: 999",
  "path": "/api/personeller/999",
  "validationErrors": {}
}
```

### 3. Validation hatası – 400

```http
POST http://localhost:8080/api/personeller
Content-Type: application/json
```

```json
{
  "ad": "",
  "soyad": "Yılmaz",
  "email": "hatalı-email"
}
```

`@Valid` doğrulaması başarısız olur. Spring `MethodArgumentNotValidException` üretir ve global handler alan hatalarını tek cevapta döndürür.

### 4. İş kuralı ihlali – 409

Aynı e-posta ile ikinci personeli eklemeyi deneyin. Service katmanı `BusinessException` fırlatır. Handler HTTP `409 Conflict` döndürür.

### 5. Genel hata – 500

Öngörülmeyen bir exception oluşursa `handleUnexpected` çalışır. Ayrıntı sunucu loguna yazılır; istemciye stack trace veya veritabanı gibi iç sistem ayrıntıları gönderilmez.

## En önemli sınıf: GlobalExceptionHandler

`@RestControllerAdvice`, Controller katmanları için ortak hata yönetimi sağlar.

- `@ExceptionHandler(ResourceNotFoundException.class)`: 404
- `@ExceptionHandler(BusinessException.class)`: iş kuralına göre 4xx
- `@ExceptionHandler(MethodArgumentNotValidException.class)`: 400
- `@ExceptionHandler(HttpMessageNotReadableException.class)`: hatalı JSON için 400
- `@ExceptionHandler(DataIntegrityViolationException.class)`: veritabanı bütünlük ihlali için 409
- `@ExceptionHandler(Exception.class)`: beklenmeyen hatalar için 500

Daha özel exception handler'lar, genel `Exception.class` handler'ından önce eşleşir.

## Exception ve transaction ilişkisi

Bu projede Service metotlarında `@Transactional` kullanılır. Örneğin `create` metodu içinde `BusinessException` fırlatılırsa bu exception `RuntimeException` alt sınıfı olduğu için Spring varsayılan rollback kuralı gereği transaction rollback edilir.

Önemli ayrım:

- Rollback kararını `@RestControllerAdvice` vermez.
- Rollback, transaction interceptor tarafından exception'ın transaction sınırından çıkışına göre değerlendirilir.
- Global handler exception'ı HTTP cevabına dönüştürür.
- Service içinde exception yakalanıp yutulursa Spring normalde bunu görmez; rollback için `setRollbackOnly()` veya exception'ı yeniden fırlatma gibi bir yaklaşım gerekir.

## Öğrenme sırası

1. `PersonelController` dosyasını inceleyin: Controller neden try-catch içermiyor?
2. `PersonelService` dosyasını inceleyin: Hangi koşullarda hangi exception fırlatılıyor?
3. `ResourceNotFoundException` ve `BusinessException` sınıflarını inceleyin.
4. `GlobalExceptionHandler` dosyasındaki metotları tek tek okuyun.
5. Postman ile başarılı ve hatalı istekleri deneyin.
6. `mvn test` komutuyla entegrasyon testlerini çalıştırın.
7. Son olarak Service metoduna geçici bir `throw new RuntimeException("Deneme");` ekleyip genel 500 handler'ını gözlemleyin. Testten sonra bu satırı kaldırın.

## Kurumsal kullanım notları

- İstemciye teknik exception mesajı, SQL, stack trace veya hassas veri döndürmeyin.
- Beklenmeyen hataları loglayın.
- Hata cevaplarını tek bir DTO formatında tutun.
- HTTP durum kodlarını hatanın anlamına uygun seçin.
- `Exception.class` handler'ını en genel fallback olarak kullanın.
- Uygulamanın güvenlik/kimlik doğrulama hataları Spring Security filtre zincirinden gelebilir; bunlar her zaman MVC `@RestControllerAdvice` kapsamına girmez. Security için ayrıca `AuthenticationEntryPoint` ve `AccessDeniedHandler` gerekebilir.
