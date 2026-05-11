---

# 🚀 Abonelik Yönetim Platformu (Subscription Management Platform)

Spring Boot 3, Java 21 ve sıfır güven (zero-trust) ağ mimarisi ile oluşturulmuş; ölçeklenebilir, sağlam ve mikro servis tabanlı bir abonelik yönetim sistemi.

---

## 🏛 Önemli Mimari ve Teknik Kararların Gerekçeleri

1. **Transactional Outbox Pattern (Olay Güdümlü Mimari):**
* **Karar:** Veritabanı işlemleri sırasında doğrudan RabbitMQ'ya mesaj göndermek yerine önce bir `outbox` tablosuna yazılır.
* **Gerekçe:** "Çift Yazma (Dual-Write)" problemini engeller. RabbitMQ'da geçici bir ağ kesintisi olsa bile `payment-service`'e en az bir kere (at-least-once) teslimatı garanti eder.


2. **`SKIP LOCKED` ile Eşzamanlılık (Concurrency) Kontrolü:**
* **Karar:** Zamanlanmış gece işleri (örn. Abonelik Yenilemeleri) MySQL'in `FOR UPDATE SKIP LOCKED` özelliğini kullanır.
* **Gerekçe:** Birden fazla Kubernetes Pod'unun veya Docker konteynerinin aynı zamanlanmış işi aynı anda çalıştırmasına olanak tanır ve aynı satırın iki kez işlenmesini engeller. Karmaşık dağıtık kilitlere (Redlock vb.) duyulan ihtiyacı ortadan kaldırır.


3. **Sıfır Güven (Zero-Trust) Ağ İzolasyonu:**
* **Karar:** Mikro servisler ana makineye (host) hiçbir port açmaz (`ports` yerine Docker `expose` parametresi kullanılır).
* **Gerekçe:** Tüm gelen trafiğin Kong API Gateway üzerinden geçmesini zorunlu kılar. Böylece hız sınırlandırma (rate-limiting) ve correlation-ID enjeksiyonunun doğrudan IP erişimiyle by-pass edilemeyeceğini garanti eder.


4. **Gözlemlenebilirlik için PLG Stack:**
* **Karar:** Ağır ELK stack yerine Promtail, Loki ve Grafana kullanılmıştır.
* **Gerekçe:** Loki yalnızca metadataları (etiketleri) indekslediği için kaynak tüketimi açısından oldukça verimlidir, bu da onu hafif bir mikro servis ortamı için mükemmel kılar. Grafana, "sıfır temas (zero-touch)" geliştirici deneyimi için konfigürasyon dosyası üzerinden otomatik olarak hazır hale getirilir (provisioning).



---

## ⚠️ Varsayımlar ve Bilinçli Olarak Kapsama Alınmayan Konular

* **Tekil Üyelik Altyapısı (Netflix/Spotify Modeli):** Proje karmaşıklığını azaltmak için tek tarife bazlı bir abonelik sistemi geliştirilmiştir.
* **Dağıtık İzleme (Distributed Tracing):** Altyapı yükünü azaltmak için Zipkin veya Jaeger gibi araçlar bilinçli olarak hariç tutulmuştur. Ancak, Loki'de servisler arası log filtrelemesine ve iz takibine olanak tanımak için Kong'da bir `Correlation-ID` eklentisi aktiftir.
* **Taklit (Mock) Ödeme Ağ Geçidi:** `payment-service`, gerçek harici HTTP çağrıları yapmak yerine üçüncü taraf sağlayıcılarla (Stripe, Iyzico veya PayPal gibi) olan etkileşimleri simüle edecek şekilde tasarlanmıştır.
* * **Taklit (Mock) Bildirim Servisi:** `notification-service`, gerçek harici HTTP çağrıları yapmak yerine üçüncü taraf sağlayıcılarla olan etkileşimleri simüle edecek şekilde tasarlanmıştır.

---

## 🚀 Projenin Nasıl Çalıştırılacağına Dair Yönergeler

### Ön Koşullar

* Java 21 ve Maven'ın yerel makinede kurulu olması. (Docker Desktop yoksa)
* Docker Desktop veya Docker Engine'in kurulu ve çalışır durumda olması.

### 1. Uygulamaları Derleme

Tüm mikro servisleri derlemek ve `.jar` dosyalarını oluşturmak için projenin kök dizininde aşağıdaki komutu çalıştırın:

```bash
./mvnw clean package -DskipTests

```

### 2. Altyapıyı ve Kümeyi (Cluster) Başlatma

Veritabanlarını, mesaj kuyruğunu, API gateway'i, loglama yığınını ve mikro servisleri ayağa kaldırın:

```bash
docker-compose up --build -d

```

*(Not: Spring Boot servisleri, veritabanları tam olarak hazır olana kadar bekleyecek şekilde sağlık kontrollerine (healthcheck) bağlanmıştır. Tüm servislerin tamamen ayağa kalkıp kullanılabilir duruma gelmesi yaklaşık 15-20 saniye sürebilir).*

### 3. Önemli Bağlantılar (URL'ler)

* **API Gateway:** `http://localhost:8000` (Tüm dış HTTP istekleri buraya gönderilmelidir)
* **Grafana Panelleri:** `http://localhost:3000` (Loki ile yapılandırılmalıdır. Bkz: Kullanım Kuralları)

---

## 🛠 Yardımcı Araçlar ve Otomasyonlar

### Kullanım Kuralları

* **Grafana ve Loki Loglama Altyapısı:** Sistem, "sıfır temas (zero-touch)" yapılandırma prensibiyle tasarlanmıştır. Grafana (`http://localhost:3000` üzerinden) ayağa kalktığında, Loki veri kaynağı (data source) `grafana-datasources.yml` dosyası aracılığıyla otomatik olarak tanımlı ve kullanıma hazır (`provisioned`) halde gelir; manuel bir ayar yapmanıza gerek yoktur. Servislerin loglarını anlık olarak incelemek için Grafana arayüzünde sol menüden **Explore** (Keşfet) sekmesine gidip kaynak olarak **Loki**'yi seçmeniz yeterlidir. *(Önemli Not: Veri kaynağı bağlantısında Docker içi ağ izolasyonu sebebiyle URL olarak `localhost` değil, iç ağ DNS adı olan `http://local-loki:3100` kullanılmıştır.)
* **Sistemi Kapatma ve Temizlik:** Geliştirme sürecini bitirip kümeyi durdurmak ve veritabanı verilerini (volume) tamamen temizlemek için `docker-compose down -v` komutunu kullanın.


### Güvenlik ve Kodlama Kısıtları

* **Kimlik Doğrulama (Authentication):** Sistemde durumlu (stateful) HTTP oturumları (session) kullanılmaz. Güvenlik yetkilendirmesi, servis sınırlarında doğrulanan durumsuz (stateless) JWT (JSON Web Token) yapısı ile sağlanır.
* **Hız Sınırlandırma (Rate Limiting):** Hizmet reddi (DoS) saldırılarına ve kaba kuvvet (brute-force) denemelerine karşı koruma sağlamak amacıyla, Gateway düzeyinde Redis tabanlı bir hız sınırlandırma politikası (IP başına dakikada 10 istek) aktif edilmiştir.
* * **Yük Dengeleme (Load Balancing):** Ağır trafik durumunda oluşacak yükü dengelemek için, Gateway düzeyinde Redis tabanlı bir yük dağıtım politikası aktif edilmiştir.
