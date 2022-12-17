<h1 align="center">
  <img alt="Eagle logo" src="assets/resources.png" width="224px"/><br/>
  Config middleware resource server
</h1>

<p align="center">
The base library including validate authentication token / jwt / user-detail / permit / deny endpoints
<br/>
</p>


## ⚡️ Quick start

Build application:

```bash
/bin/bash gradlew jar
```
> output jar: <b><i>ngx-blobs-o2jwt-1.0.0.jar</i></b>

## :rocket: Functions

#### Config endpoints resource

:package: add file [`application-rss.yml`](src/main/resources/application-rss.yml)

```yml
spring:
  resource-server-starter:
    enabled: true # enable resources server, default is true
    resource-id: product-api # set resources_id
    jwt-key: '' # set key to encode token, default is empty (using key system)
    endpoints-permitted:
      - enabled: true
        endpoint-short-url: '/swagger-ui.html'
        endpoint-regex: ''
        endpoint-description: API list all endpoints via swagger
      - enabled: true
        endpoint-short-url: '/publish/event/**'
        endpoint-regex: ''
        endpoint-description: Uri websocket to publish event
      - enabled: false
        endpoint-short-url: '/api/v1/users/redis/heartbeat/123?value=1&value1=2'
        endpoint-regex: '/api/v1/users/redis/heartbeat/123{1}.*'
        endpoint-description: Uri routes API endpoint staging
    endpoints-denied:
      - enabled: true
        endpoint-short-url: '/api/v1/admin/**'
        endpoint-regex: ''
        endpoint-description: API admin
      - enabled: false
        endpoint-short-url: '/api/v1/configs/**'
        endpoint-regex: ''
        endpoint-description: API configs system
      - enabled: false
        endpoint-short-url: '/api/v1/redis-props/keys'
        endpoint-regex: ''
        endpoint-description: API redis props
```

#### Config re-callback check token from OAuth2.0

:package: checkout file [`application-params.yml`](src/main/resources/application-params.yml)

```yml
# ////////////////////////////
# Config Resource Attributes
# ////////////////////////////
---
spring:
  resource-server-callback-starter:
    enabled: false # enable check token from OAuth2.0, default is false

# ////////////////////////////
# Config Application Attributes
# ////////////////////////////
---
spring:
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Ho_Chi_Minh

# ////////////////////////////
# Config Cors Attributes
# ////////////////////////////
---
spring:
  cors-starter:
    enabled: false
    allow-credentials: true
    max-age-in-seconds: 1800
    allowed-origins:
      - * # default is any origins
    allowed-headers: # default (*) is any headers
      - Authorization
      - Requestor-Type
    allowed-methods:
      - * # default is any methods (GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE)
    exposed-headers: # not allowed for *
      - X-Get-Header
      - Access-Control-Expose-Headers
```

:package: checkout file [`application-dev.yml`](src/main/resources/application-dev.yml), [`application-local.yml`](src/main/resources/application-dev.yml), [`application-prod.yml`](src/main/resources/application-dev.yml)

```yml
# ////////////////////////////
# Config Resource Attributes
# ////////////////////////////
---
spring:
  resource-server-callback-starter:
    host-auth: http://localhost:8083 # host OAuth2.0
```

