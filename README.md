# Spring Boot 三層架構與 Apache Wicket 範例

商品管理範例使用 Java 17、Maven、Spring Boot 3.5.16、Apache Wicket 10.12.0、Spring Data JPA 與 H2。

## 啟動與測試

先安裝 JDK 17（或相容的較新 JDK）與 Maven 3.6.3 以上，確認 `java -version` 和 `mvn -version` 可正常執行。

```bash
mvn test
mvn spring-boot:run
```

啟動後開啟 http://localhost:8080/ui/，可新增商品與查看列表。JSON API 位於 http://localhost:8080/api/products。

也可以打包後啟動：

```bash
mvn clean package
java -jar target/springboot-demo-0.0.1-SNAPSHOT.jar
```

H2 是記憶體資料庫，首次啟動列表為空，重啟會清除資料。這是教學專案，尚未加入登入與權限控管。

## 三層架構

```text
REST 請求 → ProductController ─┐
                             ├→ ProductService → ProductRepository → H2
瀏覽器 → Wicket HomePage ──────┘
```

- Controller：處理 HTTP 路由、JSON 輸入驗證、狀態碼與回應；不直接存取 Repository。
- Service：處理業務邏輯、共用驗證與交易。介面 `ProductService` 搭配 `ProductServiceImpl`，提供 REST 與 Wicket 共用。
- Repository：`ProductRepository` 繼承 `JpaRepository<Product, Long>`，由 Spring Data JPA 實作資料存取。

`entity/Product` 對應資料表；`dto` 隔離 HTTP 資料與 JPA Entity；`exception/ApiExceptionHandler` 統一處理 API 的 400／404。Service 回傳 DTO，避免頁面持有 JPA Entity 或依賴延遲載入。更新商品透過交易內的 JPA dirty checking 寫回資料庫。

主要程式位於 `src/main/java/com/example/demo/`，套件依職責分成 `controller`、`service`、`service/impl`、`repository`、`entity`、`dto`、`exception`、`config`、`wicket`。

## API 操作

| 方法 | 路徑 | 用途 | 成功狀態 |
| --- | --- | --- | --- |
| GET | /api/products | 列出商品 | 200 |
| GET | /api/products/{id} | 查詢單筆 | 200 |
| POST | /api/products | 新增 | 201，含 Location |
| PUT | /api/products/{id} | 更新 | 200 |
| DELETE | /api/products/{id} | 刪除 | 204 |

```bash
curl -i -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Coffee","price":80.00}'

curl http://localhost:8080/api/products
curl http://localhost:8080/api/products/1

curl -i -X PUT http://localhost:8080/api/products/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Tea","price":50.00}'

curl -i -X DELETE http://localhost:8080/api/products/1
```

ID 以新增回應為準。名稱不可為空白且最多 100 字元；價格須為非負數，最多十位整數及兩位小數。無效商品回傳 400；不存在的正整數 ID 回傳 404。

## 如何整合 Apache Wicket

### 1. 選擇相容版本並加入依賴

Spring Boot 3 使用 Spring 6 與 Jakarta Servlet，因此範例搭配 Wicket 10；Wicket 10 支援 Java 17 以上、Jakarta Servlet 5 以上與 Spring 6。`pom.xml` 加入同版本的 `wicket-core` 與 `wicket-spring`。這個範例直接註冊 Filter，無須額外的第三方 Boot starter，也不需要 web.xml。

### 2. 註冊 WicketFilter

`config/WicketConfig.java` 用 `FilterRegistrationBean` 把 `WicketFilter` 註冊到 `/ui/*`，同時設定 `filterMappingUrlPattern`。透過建構子傳入 `WicketApplication`，由 Filter 管理其生命週期。`/api/*` 由 Spring MVC 處理，兩種入口可並存。範例設定 Wicket deployment 模式。

### 3. 指定首頁並連接 Spring Context

`wicket/WicketApplication.java` 繼承 `WebApplication`，覆寫 `getHomePage()`；在 `init()` 中加入：

```java
getComponentInstantiationListeners().add(new SpringComponentInjector(this));
```

Injector 使用 Spring Boot 已建立的 WebApplicationContext，不另外建立 Spring Context。

### 4. 在頁面注入共用 Service

```java
@SpringBean
private ProductService productService;
```

Wicket 負責建立 Page，因此 Page 透過 `@SpringBean` 注入。注入介面可使用可序列化代理；不要把這個欄位標為 `transient`。Wicket Page 可直接呼叫 Service，不必透過 HTTP 呼叫同一應用程式的 Controller。

### 5. Java 元件與 HTML 對應

`HomePage.java` 使用 `Form`、`TextField`、`FeedbackPanel` 與 `ListView`；HTML 中的 `wicket:id` 必須對應 Java 元件 ID 與階層。

版型放在 `src/main/resources/com/example/demo/wicket/HomePage.html`，與 Java Page 的套件路徑及類別名稱一致，Maven 會把它放入 classpath。列表使用 `LoadableDetachableModel`，每次請求需要時重新從 Service 讀取；新增後重新導向首頁，避免重新整理時重送表單。

## 驗證範圍

`DemoApplicationTests` 透過隨機連接埠啟動完整應用程式，涵蓋商品 CRUD、400／404、無效輸入未寫入資料庫，以及 Wicket 頁面讀取 REST 建立的商品與 API 路由共存。Wicket 表單提交另可透過 `/ui/` 手動驗證。

已完成 Java 編譯與測試編譯。此代理執行環境禁止建立監聽 socket，整合測試在啟動 Tomcat 時遇到 `java.net.SocketException: Operation not permitted`，因此尚未完成修正後的執行驗證；請在一般終端機執行 `mvn test`。

## 官方參考

- [Spring Boot 3.5 系統需求](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Wicket 10 版本與相容需求](https://wicket.apache.org/versions/wicket-10.x/)
- [Wicket Spring 注入](https://wicket.apache.org/docs/spring.html)
