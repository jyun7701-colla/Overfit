# OverPick — 광고 없는 청정 구역 AI 뷰티 솔루션

> **Team OverFit**의 화장품 AI 추천 웹 서비스

올리브영·글로우픽 실사용 리뷰 데이터를 기반으로 광고를 걷어내고,  
퍼스널컬러 분석·피부타입 맞춤 추천·클린 성분 검색을 한 곳에서 제공합니다.

---

## 주요 기능

| 기능 | 설명 |
|------|------|
| **퍼스널컬러 분석** | 셀카 한 장으로 봄웜·여름쿨·가을웜·겨울쿨을 즉시 진단 |
| **셀럽 매칭** | 동일 퍼스널컬러 셀럽과 그들의 추천 제품 확인 |
| **피부타입 맞춤 추천** | 건성·지성·복합성·민감성 피부별 성분 기반 제품 추천 |
| **클린 검색** | 뒷광고 리뷰(ad_flag=N)를 제거한 신뢰 있는 제품 검색 |
| **AI 리뷰 분석** | GPT-4o-mini가 광고성 리뷰를 필터링하고 추천/비추천 근거를 요약 |

---

## 기술 스택

### Frontend
- HTML5 / CSS3 / Vanilla JS
- Cormorant Garamond · Noto Serif KR · Noto Sans KR (Google Fonts)

### Backend
| 레이어 | 기술 |
|--------|------|
| 웹 서버 | Java Servlet (Jakarta EE) + Apache Tomcat 11 |
| AI 서버 | Python FastAPI + Uvicorn (포트 8000) |
| ORM | MyBatis |
| DB | MySQL |

### AI / 분석
- **OpenCV** — Haar Cascade 기반 얼굴 영역 추출 및 피부 HSV 분석
- **OpenAI GPT-4o-mini** — 리뷰 뒷광고 필터링 및 감성 분석
- **Pandas / NumPy** — 성분 데이터 전처리

---

## 프로젝트 구조

```
핵심프로젝트_최종코드모음/
│
├── main.py                     # FastAPI AI 분석 서버
│
├── overfit/                    # Java 웹 애플리케이션 (Maven)
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
├── tomcat/                     # Apache Tomcat 11 런타임
│
└── *.ipynb                     # 데이터 수집 및 전처리 파이프라인
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

---

## 데이터 파이프라인

```
올리브영 / 글로우픽 크롤링
        ↓
화장품 성분 크롤링 · 병합
        ↓
고유 성분 추출 → GPT 분류 (추천 피부타입 태깅)
        ↓
성분 포함 DB 적재 / 유해 성분 DB 적재
        ↓
리뷰 수집 → 뒷광고 필터링(ad_flag) → DB 적재
        ↓
텍스트마이닝 · AI 성분 분석
```

---

## API 명세

### Python FastAPI (포트 8000)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/analyze` | 이미지 업로드 → 퍼스널컬러 분석 + 셀럽 매칭 |
| GET | `/recommend/{user_id}` | 로그인 사용자 피부타입 기반 추천 |
| GET | `/recommend-by-skin/{skin_type}` | 피부타입 직접 입력 추천 |
| GET | `/review/analyze/{prod_idx}` | 제품 리뷰 AI 분석 (GPT-4o-mini) |
| GET | `/search` | 제품명/브랜드/카테고리 키워드 검색 |

### Java Servlet (Tomcat)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/products` | 클린 검색 (뒷광고 제거) |
| GET | `/api/products/recommend` | 피부타입 맞춤 추천 |
| POST | `/api/feedback` | 리뷰(피드백) 등록 |
| POST | `/api/analyze` | 퍼스널컬러 분석 (Python 서버 프록시) |

---

## 실행 방법

### 1. Python AI 서버 실행

```bash
pip install fastapi uvicorn sqlalchemy pymysql openai opencv-python numpy pandas
python main.py
# http://localhost:8000 에서 실행
```

### 2. Java 웹 서버 실행

```bash
cd overfit
mvn clean package
# 생성된 .war 파일을 tomcat/webapps/ 에 배포 후 Tomcat 시작
```

### 3. 데이터 파이프라인 (최초 1회)

Jupyter 노트북을 아래 순서로 실행합니다.

```
1. 올리브영_크롤링_최종.ipynb
2. 글로우픽_크롤링.ipynb
3. 화장품성분크롤링.ipynb → 화장품성분병합.ipynb
4. 고유성분추출.ipynb → 고유성분사전_gpt분류.ipynb
5. 성분포함적재.ipynb / 유해성분DB적재.ipynb
6. 리뷰(필터링완료)_DB적재.ipynb
7. 핵심텍스트마이닝.ipynb / 화장품성분AI분석.ipynb
```

---

## DB 테이블 구조

| 테이블 | 주요 컬럼 |
|--------|-----------|
| `t_product` | prod_idx, prod_name, brand_name, category, target_skin, img_url |
| `t_ingredient` | prod_idx, ingredient_name, recommended_skin |
| `t_review` | prod_idx, review_content, rating, ad_flag |
| `t_user` | user_id, password, skin_type |
| `t_celebrity` | celeb_name, celeb_skintype, personal_color, prod_name, brand_name, img_url |

---

## 팀 정보

**Team OverFit**  
핵심 프로젝트 | 2026
