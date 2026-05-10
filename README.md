🚀 Abonelik Yönetim Platformu (Subscription Management Platform)
Spring Boot 3, Java 21 ve sıfır güven (zero-trust) ağ mimarisi ile oluşturulmuş; ölçeklenebilir, sağlam ve mikro servis tabanlı bir abonelik yönetim sistemi.

📐 Sistem Mimari Diyagramı

graph TD
Client([İstemci / Önyüz]) -->|HTTP :8000| Kong[Kong API Gateway]

    subgraph "Güvenli Docker Ağı (Dışarıya Açık Port Yok)"
        Kong -->|/api/auth| Auth[Auth Servisi]
        Kong -->|/api/subscriptions| Sub[Abonelik Servisi]
        Kong -->|/api/payments| Pay[Ödeme Servisi]
        Kong -->|/api/notifications| Notif[Bildirim Servisi]
        
        Auth --> AuthDB[(Auth MySQL)]
        Sub --> SubDB[(Abonelik MySQL)]
        Pay --> PayDB[(Ödeme MySQL)]
        Notif --> NotifDB[(Bildirim MySQL)]
        
        Sub -->|Outbox Pattern| RMQ((RabbitMQ))
        Pay --> RMQ
        Notif --> RMQ
        
        Kong -.->|Rate Limiting| Redis[(Redis)]
        Auth -.-> Redis
        
        Promtail[Promtail] -.->|Docker Sock Dinler| Loki[Loki]
        Loki --> Grafana[Grafana :3000]
    end