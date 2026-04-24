<div align="center">

<br>

# 💄 OverPick
## ✨ 광고 없는 청정 구역 AI 뷰티 솔루션 🌟

<br>

> 올리브영·글로우픽 실사용 리뷰 데이터를 기반으로 광고를 걷어내고,  
> 퍼스널컬러 분석·피부타입 맞춤 추천·클린 성분 검색을 한 곳에서 제공합니다.

<br>

![Java](https://img.shields.io/badge/Java-Jakarta_EE-orange?style=for-the-badge&logo=java)
![Python](https://img.shields.io/badge/Python-FastAPI-3776AB?style=for-the-badge&logo=python&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI-GPT--4o--mini-412991?style=for-the-badge&logo=openai&logoColor=white)
![Tomcat](https://img.shields.io/badge/Apache-Tomcat_11-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=black)

<br>

</div>

---

<br>

## 💄 프로젝트 소개 ✨🌟

<br>

**OverPick**은 **Team OverFit**이 개발한 AI 기반 화장품 추천 웹 서비스입니다.

현재 뷰티 시장에는 광고성 리뷰가 넘쳐나 소비자들이 제품을 신뢰하기 어려운 상황입니다.  
OverPick은 이 문제를 해결하기 위해 **GPT-4o-mini**로 광고 리뷰를 필터링하고,  
**OpenCV** 퍼스널컬러 분석과 **성분 기반 추천 엔진**으로 진짜 나에게 맞는 화장품을 찾아드립니다.

<br>

```
🧴 광고 없는 클린 리뷰   +   🎨 퍼스널컬러 분석   +   💆 피부타입 맞춤 추천
                    =  💄 OverPick
```

<br>

### 📁 프로젝트 구조

```
📦 핵심프로젝트_최종코드모음/
│
├── 🐍 main.py                        # FastAPI AI 분석 서버
│
├── ☕ overfit/                        # Java 웹 애플리케이션 (Maven)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/overfit/
│       │   ├── controller/
│       │   │   ├── AnalyzeCon.java   # 퍼스널컬러 분석 API
│       │   │   ├── ProductCon.java   # 클린 검색 / 맞춤 추천 API
│       │   │   ├── JoinCon.java      # 회원가입
│       │   │   ├── LoginCon.java     # 로그인
│       │   │   ├── LogoutCon.java    # 로그아웃
│       │   │   └── UserCon.java      # 사용자 정보 관리
│       │   ├── database/
│       │   │   ├── ProductDAO.java
│       │   │   ├── UserDAO.java
│       │   │   ├── FeedbackDAO.java
│       │   │   └── MyBatisUtil.java
│       │   └── model/
│       │       ├── ProductVO.java
│       │       ├── UserVO.java
│       │       ├── ReviewVO.java
│       │       └── FeedbackVO.java
│       ├── resources/
│       │   ├── mybatis-config.xml
│       │   └── com/overfit/mapper/
│       │       ├── ProductMapper.xml
│       │       ├── UserMapper.xml
│       │       └── FeedbackMapper.xml
│       └── webapp/
│           ├── index.html            # 메인 페이지
│           └── pages/
│               ├── recommend.html    # 맞춤 추천
│               ├── celeb.html        # 셀럽 매칭
│               └── clean.html        # 클린 검색
│
├── 🐱 tomcat/                        # Apache Tomcat 11 런타임
│
└── 📓 *.ipynb                        # 데이터 수집 및 전처리 파이프라인
    ├── 올리브영_크롤링_최종.ipynb
    ├── 글로우픽_크롤링.ipynb
    ├── 화장품성분크롤링.ipynb
    ├── 화장품성분병합.ipynb
    ├── 고유성분추출.ipynb
    ├── 고유성분사전_gpt분류.ipynb
    ├── 성분포함적재.ipynb
    ├── 유해성분DB적재.ipynb
    ├── 리뷰(필터링완료)_DB적재.ipynb
    ├── 핵심텍스트마이닝.ipynb
    └── 화장품성분AI분석.ipynb
```

<br>

---

<br>

## 🛠️ 기술 스택 ⚙️🔧

<br>

<div align="center">

| 분류 | 기술 |
|:----:|------|
| 🖥️ **Frontend** | HTML5 · CSS3 · Vanilla JS · Google Fonts |
| 🌐 **웹 서버** | Java Servlet (Jakarta EE) · Apache Tomcat 11 |
| 🧠 **AI 서버** | Python · FastAPI · Uvicorn |
| 🗂️ **ORM** | MyBatis |
| 🗄️ **Database** | MySQL |
| 👁️ **Computer Vision** | OpenCV (Haar Cascade 얼굴 인식 · HSV 피부 분석) |
| 💬 **AI 분석** | OpenAI GPT-4o-mini |
| 📊 **데이터 처리** | Pandas · NumPy |
| 🔄 **데이터 수집** | Jupyter Notebook · Web Crawling |

</div>

<br>

### 🔄 데이터 파이프라인

```
🛒  올리브영 / 글로우픽 크롤링
           ↓
🧴  화장품 성분 크롤링 · 병합
           ↓
🔬  고유 성분 추출 → GPT 분류 (추천 피부타입 태깅)
           ↓
💾  성분 포함 DB 적재 / 유해 성분 DB 적재
           ↓
📝  리뷰 수집 → 광고 필터링(ad_flag) → DB 적재
           ↓
📊  텍스트마이닝 · AI 성분 분석
```

<br>

### 🗄️ DB 테이블 구조

| 테이블 | 주요 컬럼 |
|--------|-----------|
| 🛍️ `t_product` | prod_idx, prod_name, brand_name, category, target_skin, img_url |
| 🧪 `t_ingredient` | prod_idx, ingredient_name, recommended_skin |
| 📝 `t_review` | prod_idx, review_content, rating, ad_flag |
| 👤 `t_user` | user_id, password, skin_type |
| 🌟 `t_celebrity` | celeb_name, celeb_skintype, personal_color, prod_name, brand_name, img_url |

<br>

---

<br>

## 🎯 핵심 기능 💡🔍

<br>

### 🎨 퍼스널컬러 분석
> 셀카 한 장으로 나의 퍼스널컬러를 즉시 진단합니다.

- OpenCV Haar Cascade로 얼굴 영역(이마·볼) 자동 추출
- HSV 색공간 분석으로 피부 웜/쿨 톤 판별
- **봄웜 🌸 · 여름쿨 🩵 · 가을웜 🍂 · 겨울쿨 ❄️** 4가지 유형 분류
- 진단 결과에 맞는 어울리는 색상·메이크업 팁 제공

<br>

### 🌸 셀럽 매칭
> 나와 같은 퍼스널컬러 셀럽과 그들의 제품을 확인합니다.

- DB 내 셀럽 정보와 퍼스널컬러 매핑
- 셀럽이 실제 사용하는 화장품 브랜드·카테고리 추천

<br>

### 💆 피부타입 맞춤 추천
> 내 피부에 맞는 성분이 담긴 제품만 추천합니다.

- **건성 · 지성 · 복합성 · 민감성** 피부별 맞춤 필터링
- 성분(t_ingredient)의 `recommended_skin` 기반 스코어링
- 로그인 시 자동으로 내 피부타입 불러와 즉시 추천

<br>

### 🧹 클린 검색
> 광고 리뷰를 걷어낸 진짜 후기만 보여줍니다.

- `ad_flag = 'N'` 리뷰만 집계한 신뢰도 기반 검색
- 제품명 · 브랜드 · 카테고리 키워드 통합 검색

<br>

### 🤖 AI 리뷰 분석
> GPT-4o-mini가 리뷰를 읽고 광고를 걸러 핵심만 요약합니다.

- 협찬·체험단·과도한 반복 표현 자동 감지 및 필터링
- 추천 / 비추천 판정 + 핵심 이유 3가지 요약
- 긍정·부정 키워드, 적합 피부타입 도출

<br>

### 📡 API 명세

**🐍 Python FastAPI (포트 8000)**

| Method | Endpoint | 설명 |
|:------:|----------|------|
| `POST` | `/analyze` | 📸 이미지 업로드 → 퍼스널컬러 분석 + 셀럽 매칭 |
| `GET` | `/recommend/{user_id}` | 👤 로그인 사용자 피부타입 기반 추천 |
| `GET` | `/recommend-by-skin/{skin_type}` | 💆 피부타입 직접 입력 추천 |
| `GET` | `/review/analyze/{prod_idx}` | 🤖 제품 리뷰 AI 분석 (GPT-4o-mini) |
| `GET` | `/search` | 🔍 제품명/브랜드/카테고리 키워드 검색 |

**☕ Java Servlet (Tomcat)**

| Method | Endpoint | 설명 |
|:------:|----------|------|
| `GET` | `/api/products` | 🧹 클린 검색 (광고 제거) |
| `GET` | `/api/products/recommend` | 💆 피부타입 맞춤 추천 |
| `POST` | `/api/feedback` | ✍️ 리뷰(피드백) 등록 |
| `POST` | `/api/analyze` | 🎨 퍼스널컬러 분석 (Python 서버 프록시) |

<br>

---

<br>

## 🚀 설치 방법 📦

<br>

### 1️⃣ Python AI 서버 실행

```bash
pip install fastapi uvicorn sqlalchemy pymysql openai opencv-python numpy pandas
python main.py
# ✅ http://localhost:8000 에서 실행
```

<br>

### 2️⃣ Java 웹 서버 실행

```bash
cd overfit
mvn clean package
# ✅ 생성된 .war 파일을 tomcat/webapps/ 에 배포 후 Tomcat 시작
```

<br>

### 3️⃣ 데이터 파이프라인 (최초 1회)

Jupyter 노트북을 아래 순서로 실행합니다.

```
① 올리브영_크롤링_최종.ipynb
② 글로우픽_크롤링.ipynb
③ 화장품성분크롤링.ipynb  →  화장품성분병합.ipynb
④ 고유성분추출.ipynb  →  고유성분사전_gpt분류.ipynb
⑤ 성분포함적재.ipynb  /  유해성분DB적재.ipynb
⑥ 리뷰(필터링완료)_DB적재.ipynb
⑦ 핵심텍스트마이닝.ipynb  /  화장품성분AI분석.ipynb
```

<br>

---

<br>

## 👥 팀원 소개 🙋

<br>

<div align="center">

| 이름 | 역할 |
|:----:|:----:|
| 👑 **윤지은** | 팀장 · 총괄 기획 |
| 🔧 **김재원** | 팀원 · 기술 문서 및 테스트 |
| 🖥️ **김효식** | 팀원 · 프론트엔드 개발 |
| ⚙️ **이권형** | 팀원 · 백엔드 개발 |
| 🎨 **한송이** | 팀원 · UI 디자인 |

**💪 Team OverFit** | 핵심 프로젝트 | 2026

</div>

<br>

---

<br>

## 📈 기대 효과 🎉

<br>

- 🛡️ **신뢰도 향상** — 광고 리뷰를 자동 제거해 소비자가 진짜 후기만 볼 수 있습니다.
- 🎯 **맞춤형 경험** — 피부타입·퍼스널컬러 기반으로 개인화된 제품 추천을 제공합니다.
- ⏱️ **시간 절약** — AI 리뷰 요약으로 수백 개의 리뷰를 읽지 않아도 핵심을 파악합니다.
- 💡 **정보 격차 해소** — 성분 정보와 유해 성분 데이터를 쉽게 접근할 수 있도록 제공합니다.
- 🌱 **클린 뷰티 문화** — 광고 없는 투명한 리뷰 생태계 형성에 기여합니다.

<br>

---

<br>

## ⚠️ 주의사항 📌

<br>

- 🔑 `main.py` 내 **OpenAI API Key**는 외부에 노출되지 않도록 환경변수로 관리하세요.
- 🖼️ 퍼스널컬러 분석은 **정면 얼굴 사진**에서 가장 정확한 결과를 제공합니다.
- 🗄️ DB 접속 정보(`DB_HOST`, `DB_USER`, `DB_PASS`)는 `.env` 파일로 분리하여 관리하는 것을 권장합니다.
- 🐍 Python AI 서버(포트 8000)가 **먼저 실행**된 상태에서 Java 웹 서버를 실행해야 합니다.
- 📓 데이터 파이프라인 노트북은 **순서대로** 실행해야 정상적으로 DB에 적재됩니다.

<br>

---

<div align="center">
<br>

**💄 OverPick** — 광고 없는 청정 구역 AI 뷰티 솔루션

*Made with ❤️ by Team OverFit · 2026*

</div>
