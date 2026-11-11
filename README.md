# Hava Durumu

Kotlin ve Jetpack Compose ile geliştirilmiş Android hava durumu uygulaması. Anlık hava koşullarını, saatlik ve 7 günlük tahminleri; şehir arama, favori ve sabit şehir yönetimi ile harita üzerinden konum seçimini tek uygulamada bir araya getirir.

| Platform | Minimum SDK | Target SDK | JDK |
|---|---:|---:|---:|
| Android | 26 (Android 8.0) | 36 | 17 |

## Özellikler

- Anlık sıcaklık, hissedilen sıcaklık, yağış olasılığı, nem ve rüzgâr bilgileri
- Saatlik ve 7 günlük hava tahminleri
- Hava koşullarına göre giyim ve açık hava aktivitesi önerileri
- Open-Meteo üzerinden şehir arama
- Favori ve sabit şehirleri cihazda saklama
- Haritada konum seçip o konumun tahminini görüntüleme
- Yükleniyor, başarılı, boş ve hata durumları için ayrı arayüzler

## Ekran Görüntüleri

### 1. Ana Sayfa Akışı

| Anlık Metrikler & Saatlik Tahmin | 7 Günlük Tahmin & Giyim/Aktivite Önerisi |
|---|---|
| <img width="380" height="840" alt="Screenshot from 2026-11-11 00-09-59" src="https://github.com/user-attachments/assets/8818c7d6-941d-4dae-a321-4bb1c1bc02fb" />|<img width="380" height="840" alt="Screenshot from 2026-11-11 00-10-35" src="https://github.com/user-attachments/assets/4425e8b1-d540-4bf0-9b7e-6794d2eb616c" />


### 2. Şehir Arama & Favori Yönetimi

| Şehir Arama Sonuç Listesi | Arama Boş Durum / EmptyView | Favori Şehirler |
|---|---|---|
| <img width="380" height="840" alt="Screenshot from 2026-11-11 00-12-32" src="https://github.com/user-attachments/assets/e3dcfd9a-aa69-468c-a18c-a382708f80e6" />| <img width="380" height="840" alt="Screenshot from 2026-11-11 00-11-07" src="https://github.com/user-attachments/assets/0483f9ce-1197-487a-80ed-bfac5ef5f294" />| <img width="380" height="840" alt="Screenshot from 2026-11-11 00-13-10" src="https://github.com/user-attachments/assets/0ef27670-53d4-41d6-8341-2c2dbf0363e0" />|

### 3. İnteraktif Harita - osmdroid

| Türkiye Haritası | Konum Seçimi & Alt Bilgi Kartı |
|---|---|
| <img width="380" height="840" alt="Screenshot from 2026-11-11 00-13-22" src="https://github.com/user-attachments/assets/9a154173-0aac-4a55-9f1f-67f517a5731c" />|<img width="380" height="840" alt="Screenshot from 2026-11-11 00-14-03" src="https://github.com/user-attachments/assets/706ec3d9-752b-42b7-8e49-49d3aac0e970" />|


## Geliştirici Notları

### Jetpack Compose ve `@Composable`

Jetpack Compose, arayüzü XML layout dosyaları ile ayrı ayrı tanımlamak yerine Kotlin fonksiyonlarıyla kurmamı sağlar. `@Composable` fonksiyonlar arayüzün bir bölümünü tanımlar; parametre olarak aldıkları state'e göre ekrana içerik çizer. Bu yaklaşım UI ile kod arasındaki geçişleri azaltır, tekrar kullanılabilir bileşenler yazmayı ve arayüzü önizlemeyi kolaylaştırır.

State değiştiğinde Compose, o state'i okuyan composable'ları yeniden çalıştırır (**recomposition**). Bu, ekranın tamamını elle güncellemek yerine değişen veriye bağlı arayüzün güncellenmesini sağlar. Bu yüzden composable'ları mümkün olduğunca deterministik tutmalı; ağ isteği veya veritabanına yazma gibi yan etkileri doğrudan çizim fonksiyonlarının içine koymamalıyım.

### State ve Tek Yönlü Veri Akışı (UDF)

Ekran durumu ViewModel'den arayüze akar; kullanıcı etkileşimleri ise callback'ler üzerinden ViewModel'e geri gider:

```text
Kullanıcı olayı → View / Route → ViewModel → Repository → API veya Room
UI              ← StateFlow   ← ViewModel ← sonuç
```

ViewModel, UI state'ini `StateFlow` ile yayımlar. Compose tarafı `collectAsStateWithLifecycle()` kullanarak bu akışı Lifecycle ile uyumlu biçimde toplar. Böylece ekran görünür değilken gereksiz UI güncellemeleri azaltılır.

`LoadState` sealed interface'i veri isteyen ekranlar için dört açık durumu tanımlar:

- **Loading:** İstek sürüyor; yüklenme göstergesi gösterilir.
- **Success:** Veri geldi; içerik çizilir.
- **Empty:** İstek tamamlandı ancak gösterilecek sonuç yok.
- **Error:** İstek başarısız oldu; anlaşılır hata ve yeniden deneme seçeneği sunulur.

Kapalı durum kümesi, her ekranın tüm olasılıkları `when` ile ele almasını sağlar.

### MVVM ve Lifecycle

MVVM, arayüz çizimini veri ve ekran davranışından ayırır:

- **Model:** `City`, `Forecast` gibi uygulama verileri; repository'ler üzerinden API ve yerel veri kaynaklarına erişim.
- **View:** Compose ekranları ve bileşenleri. State'i gösterir, kullanıcı olaylarını iletir.
- **ViewModel:** Ekranın state'ini ve kullanıcı olaylarına verilecek yanıtı yönetir; repository'lerden veri ister.

ViewModel, Activity veya ekran yeniden oluşturulduğunda yapılandırma değişiklikleri boyunca korunur. Ağ işleri `viewModelScope` içinde başlatıldığı için ViewModel'in Lifecycle'ı sona erdiğinde işler iptal edilir. Rota parametrelerini okuyan tahmin ekranı ayrıca `SavedStateHandle` kullanır.

### Room Database ve KSP

Room, favori şehirleri ve sabit şehri SQLite üzerinde cihazda saklar. Bu kayıtlar uygulama yeniden açıldığında da kullanılabilir ve internet bağlantısından bağımsızdır; güncel hava tahminini almak için yine ağ bağlantısı gerekir.

Entity sınıfları tablo yapısını, DAO'lar veritabanı işlemlerini, `AppDatabase` ise veritabanı örneğini ve şema geçişlerini tanımlar. KSP, Room'un gerekli kodlarını derleme sırasında üretir; bu sayede sorgu ve şema hatalarının önemli bir kısmı çalışma zamanından önce yakalanır.

### Retrofit, Coroutines ve Flow

Retrofit, Open-Meteo şehir arama ve hava tahmini servislerini Kotlin arayüzleri üzerinden çağırmamı sağlar. Gson, JSON yanıtlarını Kotlin veri yapılarına dönüştürür. Repository'lerdeki `suspend` fonksiyonlar ağ erişimini coroutine ile asenkron yürütür ve IO dispatcher'ını kullanır; böylece ana UI thread'i ağ yanıtını beklemez.

ViewModel, istekleri `viewModelScope` içinde başlatıp sonuçları `StateFlow` ile UI'a aktarır. Şehir aramasında `debounce` ve `distinctUntilChanged` gereksiz istekleri azaltır; `collectLatest` yeni sorgu geldiğinde önceki sorgunun sonucunun eski UI state'ini ezmesini önlemeye yardımcı olur.

### osmdroid ve OpenStreetMap

`osmdroid`, OpenStreetMap karo verilerini Android'de harita olarak gösterir. Kullanıcı haritaya dokunduğunda koordinatlar alınır ve seçilen konum için hava tahmini istenir. Harita karoları osmdroid'in uygulama içindeki disk önbelleğinde saklanır; daha önce yüklenmiş alanların yeniden açılması ağ kullanımını azaltabilir. İlk kez alınmamış karolar için internet gerekir.

Koordinatın okunabilir yer adına çevrilmesi için Android `Geocoder` ile reverse geocoding yapılır. Bulunabilen il ve ilçe bilgileri alt bilgi kartında gösterilir; sonuç cihazın geocoder desteğine ve veri kapsamına bağlı olduğundan ad çözümlenemediğinde genel bir konum etiketi kullanılır. Harita yaşam döngüsü `ON_RESUME` / `ON_PAUSE` olaylarıyla eşleştirilir.

## Dizin Yapısı

```text
app/src/main/java/com/kampplus/hava/
├── data/
│   ├── local/              # Room DB (AppDataBase), DAO ve Entity sınıfları
│   └── ...                 # Retrofit API servisleri ve Repository sınıfları
├── model/                  # City, Forecast modelleri ve WeatherAdviceHelper
├── ui/
│   ├── city/               # Şehir arama ekranı ve ViewModel
│   ├── forecast/           # Ana sayfa, saatlik ve haftalık tahmin ekranları
│   ├── map/                # osmdroid harita entegrasyonu ve ViewModel
│   ├── component/          # Shimmer, ikonlar ve ortak arayüz bileşenleri
│   ├── theme/              # Renk paleti, tipografi ve tema ayarları
│   └── HavaNavHost.kt      # Ekranlar arası gezinme (Navigation)
└── MainActivity.kt         # Jetpack Compose giriş noktası
```



