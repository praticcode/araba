# Spring Boot Global Exception Handler – Kurumsal Eğitim Projesi

Bu proje, `@RestControllerAdvice` ve `@ExceptionHandler` konusunu gerçek bir katmanlı REST API üzerinden öğretmek için hazırlanmıştır.

## Teknolojiler

- Java 17
- Spring Boot 3.5.2
- Spring Web
- Spring Data JPA
- Jakarta Bean Validation
- H2 in-memory veritabanı
- Maven
- JUnit 5 + MockMvc

> Veritabanı H2 olduğu için ayrıca PostgreSQL/MySQL kurmanız gerekmez. Uygulama kapanınca veriler silinir.

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

Gereksinim: JDK 17 ve Maven.

Proje klasöründe:

```bash
mvn spring-boot:run
```

Testleri çalıştırmak için:

```bash
mvn test
```

IntelliJ IDEA kullanıyorsanız `pom.xml` dosyasını Maven projesi olarak açıp `PersonelApplication` sınıfını çalıştırabilirsiniz.

## API uçları

| Metot | Adres | Açıklama |
|---|---|---|
| GET | `/api/personeller` | Tüm personeller |
| GET | `/api/personeller/{id}` | Personel getir |
| POST | `/api/personeller` | Personel ekle |
| PUT | `/api/personeller/{id}` | Personel güncelle |
| DELETE | `/api/personeller/{id}` | Personel sil |

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
