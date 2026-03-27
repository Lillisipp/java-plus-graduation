# StatsControllerApi

All URIs are relative to *http://localhost:9090*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getStats**](StatsControllerApi.md#getStats) | **GET** /stats | Получение статистики по посещениям. Обратите внимание: значение даты и времени нужно закодировать (например используя java.net.URLEncoder.encode)  |
| [**hit**](StatsControllerApi.md#hit) | **POST** /hit | Сохранение информации о том, что к эндпоинту был запрос |


<a id="getStats"></a>
# **getStats**
> List&lt;ViewStats&gt; getStats(start, end, uris, unique)

Получение статистики по посещениям. Обратите внимание: значение даты и времени нужно закодировать (например используя java.net.URLEncoder.encode) 

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.StatsControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:9090");

    StatsControllerApi apiInstance = new StatsControllerApi(defaultClient);
    String start = "start_example"; // String | Дата и время начала диапазона за который нужно выгрузить статистику (в формате \"yyyy-MM-dd HH:mm:ss\")
    String end = "end_example"; // String | Дата и время конца диапазона за который нужно выгрузить статистику (в формате \"yyyy-MM-dd HH:mm:ss\")
    List<String> uris = Arrays.asList(); // List<String> | Список uri для которых нужно выгрузить статистику
    Boolean unique = false; // Boolean | Нужно ли учитывать только уникальные посещения (только с уникальным ip)
    try {
      List<ViewStats> result = apiInstance.getStats(start, end, uris, unique);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling StatsControllerApi#getStats");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **start** | **String**| Дата и время начала диапазона за который нужно выгрузить статистику (в формате \&quot;yyyy-MM-dd HH:mm:ss\&quot;) | |
| **end** | **String**| Дата и время конца диапазона за который нужно выгрузить статистику (в формате \&quot;yyyy-MM-dd HH:mm:ss\&quot;) | |
| **uris** | [**List&lt;String&gt;**](String.md)| Список uri для которых нужно выгрузить статистику | [optional] |
| **unique** | **Boolean**| Нужно ли учитывать только уникальные посещения (только с уникальным ip) | [optional] [default to false] |

### Return type

[**List&lt;ViewStats&gt;**](ViewStats.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Статистика собрана |  -  |

<a id="hit"></a>
# **hit**
> hit(endpointHit)

Сохранение информации о том, что к эндпоинту был запрос

Сохранение информации о том, что на uri конкретного сервиса был отправлен запрос пользователем. Название сервиса, uri и ip пользователя указаны в теле запроса.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.StatsControllerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:9090");

    StatsControllerApi apiInstance = new StatsControllerApi(defaultClient);
    EndpointHit endpointHit = new EndpointHit(); // EndpointHit | данные запроса
    try {
      apiInstance.hit(endpointHit);
    } catch (ApiException e) {
      System.err.println("Exception when calling StatsControllerApi#hit");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **endpointHit** | [**EndpointHit**](EndpointHit.md)| данные запроса | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Информация сохранена |  -  |

