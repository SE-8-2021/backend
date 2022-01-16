<img src="https://i.imgur.com/CNbpWT0.webp" width=200>

# SE team-8 2021 backend

Inherited from 2020 team 2 project.

#### [Demo video](https://youtu.be/_iJlSMvSN-w)
#### [Visit Official PVS Online](https://pvs.xcc.tw)
#### [Self host PVS](https://github.com/SE-8-2021/container)
#### [Our Meeting documents](https://hackmd.io/@Xanonymous/SE-Note)

## 如何完全啟用 PVS ?

### Step 1, Install required tools

- JDK 17.
- maven.
- postgres (DB).
- IntelliJ IDEA (建議使用)

### Step 2, Get dependencies

在後端專案根目錄使用以下指令，使用 maven 安裝所有專案依賴項目
```shell=
mvn install
```

>也可以直接用 IntelliJ IDEA 打開後端 project 讓他自動安裝依賴。(替代方案)

### Step 3, Setup DATABASE

啟動 postgresSQL, 並設定好預設帳號跟密碼，假設為
```shell=
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=root
```

### Step 4, Initialize DATABASE & TABLES

由於 Team2 使用 ORM，此操作無需我們手動處理。
僅須將後端執行一次就會自動進行創建。

開始之前，請手動建立 DATABASE。
```sql=
CREATE DATABASE "PVS";
```

### Step 5, 取得 Tokens

目前 PVS 中所有的環境變數 => [Here](https://github.com/SE-8-2021/container/blob/main/.env_example)
請分別從以下來源取得相關 Token 後，在參考後續說明來設定環境變數。

#### DB_URL
連結至 Postgre SQL 的 URL。
Postgres SQL 預設開啟 `5432` port, 
範例：`jdbc:postgresql://localhost:5432/PVS`

#### PVS_GITHUB_TOKEN
存取 GitHub repository 相關資料的授權金鑰。
取得方法：參考[此說明](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)生成 Token，至少需要以下圖片所示之三個權限：
<img src="https://i.imgur.com/FrRcEE8.webp" width="70%">

#### PVS_SONAR_TOKEN
存取 SonarCloud repository 相關資料的授權金鑰。
取得方法：[參考此網站](https://docs.sonarqube.org/latest/user-guide/user-token/)

#### PVS_DB_USER
Postgre SQL 的帳號，預設是 `postgres`。

#### PVS_DB_PASS
Postgre SQL 的密碼，在建立資料庫時，需要自行設定。

#### PVS_GITLAB_TOKEN
存取 GitLab repository 相關資料的授權金鑰。
[參考此網站](https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html)

#### PVS_TRELLO_KEY & PVS_TRELLO_TOKEN
參考此網站 [Generate Trello Api Key & Token](https://docs.servicenow.com/bundle/quebec-it-asset-management/page/product/software-asset-management2/task/generate-trello-apikey-token.html)

#### JWT_SALT
JWT 生成過程所需的雜湊演算參數，屬機密資料，可以是任意值。
在 `JwtTokenUtils` 裡面有使用到。

### Step 6, 設定 Tokens 到環境變數

接著，在後端專案將要運行的環境下設定以上幾項環境變數，執行以下指令：
若你的作業系統並非是基於 Linux Kernal 而創造的，如 Windows，則需使用不一樣的指令來設定環境變數。

```bash=
# 以 PVS_TRELLO_KEY 為例
export PVS_TRELLO_KEY=[>>> YOUR KEY HERE]
```
```bash=
export PVS_TRELLO_TOKEN=[>>> YOUR TOKEN HERE]
```

Windows (power shell):
```shell=
$Env:PVS_TRELLO_KEY="[>>> YOUR KEY HERE]"
```
```shell=
$Env:PVS_TRELLO_TOKEN="[>>> YOUR TOKEN HERE]"
```

### Step 7, Run

```bash
mvn spring-boot:run
```

## 如何執行單元測試
The backend of the PVS system use `junit4` to test functions. However, if we run `mvn test` , spring-boot cannot catch any test.

To solve this problem, we suggest changing the import
`import org.junit.Test` to `import org.junit.jupiter.api.Test`

After the changing, `mvn test` can catch the tests which have applied this change.
In this case, original testing will be failed. Thus, the following todo list is what may need to be done.

### Todo

- [ ] Change the import
  - import org.junit.jupiter.api.Test;
  - import org.junit.jupiter.api.BeforeAll;
  - import org.junit.jupiter.api.AfterAll;
  - `...`
- [ ] Add some annotation that test classes may need
  - @ExtendWith(SpringExtension.class)
  - @WebAppConfiguration 
    - Allow MockMvc to interact with
  - @TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"}) 
    - Allow to override the WebClient bean
  - @AutoConfigureMockMvc(addFilters = false) 
    - The addFilters are set to false to disable security
  - @ActiveProfiles("local")
  - @ContextConfiguration
    - exp: @ContextConfiguration(classes = {GithubApiServiceTest.TestConfig.class})
  - @DisplayName("The_test_name_you_want_to_show_in_the_ terminal")
- [ ] Test classes
  - GitLabCommitDAOIntegrationTest
  - GitLabApiServiceTest
  - GitLabCommitServiceTest
  - TrelloApiServiceTest
