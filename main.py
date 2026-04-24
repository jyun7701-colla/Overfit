from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import create_engine, text
import openai
import json
import pandas as pd
import cv2
import numpy as np
import os
import uvicorn
from dotenv import load_dotenv

app = FastAPI()
load_dotenv() 
# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

# DB 연결
DB_HOST = os.getenv("DB_HOST")
DB_PORT = int(os.getenv("DB_PORT"))
DB_USER = os.getenv("DB_USER")
DB_PASS = os.getenv("DB_PASSWORD")
DB_NAME = os.getenv("DB_NAME")

engine = create_engine(
    f'mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}?charset=utf8mb4'
)

# OpenAI 클라이언트
client = openai.OpenAI(api_key=os.getenv("OPENAI_API_KEY"))


# ✅ 1번: 피부타입 직접 입력 추천 (구체적 경로 먼저)
@app.get("/recommend-by-skin/{skin_type}")
def recommend_by_skin(skin_type: str, top_n: int = 10, category: str = ""):
    with engine.connect() as conn:
        # 카테고리 필터 여부에 따라 SQL 분기
        if category:
            result = conn.execute(text("""
                SELECT 
                    p.prod_idx, p.prod_name, p.brand_name,
                    p.category, p.target_skin, p.img_url,
                    COUNT(*) as score
                FROM t_product p
                JOIN t_ingredient i ON p.prod_idx = i.prod_idx
                WHERE i.recommended_skin LIKE :skin_type
                  AND p.category LIKE :category
                GROUP BY 
                    p.prod_idx, p.prod_name, p.brand_name,
                    p.category, p.target_skin, p.img_url
                ORDER BY 
                    CASE WHEN p.target_skin LIKE :skin_type2 THEN 1 ELSE 2 END,
                    score DESC
                LIMIT :top_n
            """), {
                "skin_type": f"%{skin_type}%",
                "skin_type2": f"%{skin_type}%",
                "category": f"%{category}%",
                "top_n": top_n
            }).fetchall()
        else:
            result = conn.execute(text("""
                SELECT 
                    p.prod_idx, p.prod_name, p.brand_name,
                    p.category, p.target_skin, p.img_url,
                    COUNT(*) as score
                FROM t_product p
                JOIN t_ingredient i ON p.prod_idx = i.prod_idx
                WHERE i.recommended_skin LIKE :skin_type
                GROUP BY 
                    p.prod_idx, p.prod_name, p.brand_name,
                    p.category, p.target_skin, p.img_url
                ORDER BY 
                    CASE WHEN p.target_skin LIKE :skin_type2 THEN 1 ELSE 2 END,
                    score DESC
                LIMIT :top_n
            """), {
                "skin_type": f"%{skin_type}%",
                "skin_type2": f"%{skin_type}%",
                "top_n": top_n
            }).fetchall()

    products = [
        {
            "prod_idx": row[0],
            "prod_name": row[1],
            "brand_name": row[2],
            "category": row[3],
            "target_skin": row[4],
            "img_url": row[5],
            "score": row[6]
        }
        for row in result
    ]

    return {"skin_type": skin_type, "recommendations": products}


# ✅ 2번: 리뷰 분석 (구체적 경로 먼저)
@app.get("/review/analyze/{prod_idx}")
def analyze_reviews(prod_idx: int):
    with engine.connect() as conn:

        # 제품 정보
        product = conn.execute(text("""
            SELECT prod_name, brand_name FROM t_product 
            WHERE prod_idx = :prod_idx
        """), {"prod_idx": prod_idx}).fetchone()

        if not product:
            return {"error": "제품을 찾을 수 없습니다."}

        # 리뷰 가져오기 (최대 50개)
        reviews = conn.execute(text("""
            SELECT review_content, rating
            FROM t_review
            WHERE prod_idx = :prod_idx AND ad_flag = 'N'
            ORDER BY rating DESC
            LIMIT 1000
        """), {"prod_idx": prod_idx}).fetchall()

        if not reviews:
            return {"error": "리뷰가 없습니다."}

    # 리뷰 텍스트 합치기
    review_text = "\n".join([f"[별점:{r[1]}] {r[0]}" for r in reviews])

    # GPT 분석
    prompt = f"""
당신은 화장품 리뷰 분석 전문가입니다.
아래는 '{product[0]}' 제품의 리뷰들입니다.

{review_text}

다음 순서로 분석해주세요:

1. 뒷광고 의심 리뷰 필터링
   - 아래 조건 중 1개라도 해당하면 무조건 광고 리뷰로 판단하고 제거할 것
   - 판단이 애매한 경우에도 광고로 간주하고 제거할 것 (관대하게 필터링 금지)

   조건:
   - "쟁여", "쟁임", "세일 때 구매", "올영세일" 등 구매 유도 표현 포함
   - "너무너무", "진짜진짜", "강추강추" 등 감탄사 2회 이상 반복
   - 구체적인 사용 경험 없이 30자 미만의 짧은 긍정 리뷰
   - "협찬", "체험단", "제공받아", "선물받아" 표현 포함
   - 제품명이나 브랜드명을 2회 이상 반복 언급

2. 필터링된 리뷰 기반으로 분석
   - 추천 여부: 추천 또는 비추천
   * [추천]: 전체 리뷰 중 긍정적인 평가가 60% 이상이며, 심각한 부작용(트러블, 알러지 등) 언급이 3% 미만일 때.
   * [비추천]: 부정적인 평가가 20% 이상이거나, 피부 뒤집어짐 등 심각한 부작용 언급이 10% 이상일 때.
   - 추천/비추천 이유: 3가지 핵심 이유
   - 주요 긍정 키워드: 3개
   - 주요 부정 키워드: 3개 (없으면 없음)
   - 적합 피부타입: 리뷰 기반으로 판단

다음 JSON 형식으로만 답해줘:
{{
  "product_name": "{product[0]}",
  "ad_filtered_count": 뒷광고로 필터링된 리뷰 수,
  "total_reviews": 전체 리뷰 수,
  "recommendation": "추천" 또는 "비추천",
  "reasons": ["이유1", "이유2", "이유3"],
  "positive_keywords": ["키워드1", "키워드2", "키워드3"],
  "negative_keywords": ["키워드1", "키워드2", "키워드3"],
  "suitable_skin": "적합 피부타입"
}}
"""

    try:
        res = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[{"role": "user", "content": prompt}],
            temperature=0
        )
        text_result = res.choices[0].message.content.strip()
        text_result = text_result.replace('```json', '').replace('```', '').strip()
        return json.loads(text_result)

    except Exception as e:
        return {"error": str(e)}


# ✅ 3번: 사용자 기반 추천 (가장 마지막)
@app.get("/recommend/{user_id}")
def recommend_by_user(user_id: str, top_n: int = 10):
    with engine.connect() as conn:

        user = conn.execute(text("""
            SELECT skin_type FROM t_user WHERE user_id = :user_id
        """), {"user_id": user_id}).fetchone()

        if not user:
            return {"error": "사용자를 찾을 수 없습니다."}

        skin_type = user[0]

        result = conn.execute(text("""
            SELECT 
                p.prod_idx, p.prod_name, p.brand_name,
                p.category, p.target_skin, p.img_url,
                COUNT(*) as score
            FROM t_product p
            JOIN t_ingredient i ON p.prod_idx = i.prod_idx
            WHERE i.recommended_skin LIKE :skin_type
            GROUP BY 
                p.prod_idx, p.prod_name, p.brand_name,
                p.category, p.target_skin, p.img_url
            ORDER BY 
                CASE WHEN p.target_skin LIKE :skin_type THEN 1 ELSE 2 END,
                score DESC
            LIMIT :top_n
        """), {
            "skin_type": f"%{skin_type}%",
            "top_n": top_n
        }).fetchall()

    products = [
        {
            "prod_idx": row[0],
            "prod_name": row[1],
            "brand_name": row[2],
            "category": row[3],
            "target_skin": row[4],
            "img_url": row[5],
            "score": row[6]
        }
        for row in result
    ]

    return {"user_id": user_id, "skin_type": skin_type, "recommendations": products}

@app.get("/search")
def search_products(keyword: str, top_n: int = 20):
    with engine.connect() as conn:
        result = conn.execute(text("""
            SELECT DISTINCT
                p.prod_idx, p.prod_name, p.brand_name,
                p.category, p.target_skin, p.img_url
            FROM t_product p
            WHERE p.prod_name LIKE :keyword
               OR p.brand_name LIKE :keyword
               OR p.category LIKE :keyword
            ORDER BY p.prod_name
            LIMIT :top_n
        """), {
            "keyword": f"%{keyword}%",
            "top_n": top_n
        }).fetchall()

    products = [
        {
            "prod_idx": row[0],
            "prod_name": row[1],
            "brand_name": row[2],
            "category": row[3],
            "target_skin": row[4],
            "img_url": row[5],
            "score": top_n - i  # 순위 기반 점수
        }
        for i, row in enumerate(result)
    ]

    return {"keyword": keyword, "products": products}

# 퍼스널컬러 분석 API 추가
from fastapi import UploadFile, File
import base64


def classify_personal_color(hue, sat, val, warm_score):
    is_warm   = warm_score > 0
    is_bright = val > 140
    if is_warm and is_bright:      return "봄웜"
    elif not is_warm and is_bright: return "여름쿨"
    elif is_warm and not is_bright: return "가을웜"
    else:                           return "겨울쿨"

def analyze_skin_tone(image_bytes):
    nparr = np.frombuffer(image_bytes, np.uint8)
    img   = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if img is None:
        return None, "이미지를 읽을 수 없습니다."

    # 한글 경로 문제 해결 - 파일을 직접 읽어서 로드
    cascade_path = r'C:\Users\smhrd\anaconda3\Lib\site-packages\cv2\data\haarcascade_frontalface_default.xml'
    
    # 파일을 bytes로 읽어서 numpy array로 로드
    with open(cascade_path, 'r') as f:
        xml_content = f.read()
    
    import tempfile, os
    tmp = tempfile.NamedTemporaryFile(suffix='.xml', delete=False, mode='w', encoding='utf-8')
    tmp.write(xml_content)
    tmp.close()
    
    face_cascade = cv2.CascadeClassifier(tmp.name)
    os.unlink(tmp.name)
    
    if face_cascade.empty():
        return None, "얼굴 인식 모델을 로드할 수 없습니다."

    gray  = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, 1.1, 5, minSize=(80,80))

    if len(faces) == 0:
        return None, "얼굴을 찾을 수 없습니다. 정면 사진을 올려주세요."

    x, y, w, h = max(faces, key=lambda f: f[2]*f[3])
    forehead = img[y+int(h*0.1):y+int(h*0.3), x+int(w*0.3):x+int(w*0.7)]
    cheek    = img[y+int(h*0.4):y+int(h*0.7), x+int(w*0.1):x+int(w*0.4)]
    skin_region = np.vstack([forehead.reshape(-1,3), cheek.reshape(-1,3)])
    skin_hsv    = cv2.cvtColor(skin_region.reshape(1,-1,3), cv2.COLOR_BGR2HSV).reshape(-1,3)
    mask = (skin_hsv[:,1]>20) & (skin_hsv[:,2]>60) & (skin_hsv[:,2]<240)
    skin_pixels = skin_hsv[mask]

    if len(skin_pixels) < 10:
        return None, "피부 영역을 분석할 수 없습니다."

    avg_hue = float(np.mean(skin_pixels[:,0]))
    avg_sat = float(np.mean(skin_pixels[:,1]))
    avg_val = float(np.mean(skin_pixels[:,2]))
    skin_bgr = skin_region[mask[:len(skin_region)]]
    warm_score = float(np.mean(skin_bgr[:,2]) - np.mean(skin_bgr[:,0])) if len(skin_bgr) > 0 else 0
    personal_color = classify_personal_color(avg_hue, avg_sat, avg_val, warm_score)

    descriptions = {
        "봄웜":  {"desc":"밝고 생기있는 따뜻한 톤","colors":"복숭아, 산호, 아이보리, 연두","avoid":"차갑고 어두운 색상","makeup":"피치, 코랄, 골드 계열"},
        "여름쿨": {"desc":"부드럽고 우아한 차가운 톤","colors":"라벤더, 로즈, 파우더블루, 민트","avoid":"강렬하고 탁한 색상","makeup":"로즈, 핑크, 실버 계열"},
        "가을웜": {"desc":"깊고 풍부한 따뜻한 톤","colors":"카멜, 브라운, 올리브, 머스타드","avoid":"차갑고 밝은 색상","makeup":"브라운, 테라코타, 골드 계열"},
        "겨울쿨": {"desc":"선명하고 강렬한 차가운 톤","colors":"블랙, 화이트, 버건디, 로얄블루","avoid":"탁하고 따뜻한 색상","makeup":"버건디, 플럼, 실버 계열"},
    }
    d = descriptions[personal_color]
    return {
        "personal_color": personal_color,
        "description": d["desc"],
        "colors": d["colors"],
        "avoid": d["avoid"],
        "makeup": d["makeup"],
    }, None

@app.post("/analyze")
async def analyze_personal_color(photo: UploadFile = File(None), file: UploadFile = File(None)):
    try:
        upload = photo if photo else file
        if not upload:
            return {"success": False, "message": "사진을 업로드해주세요."}

        image_bytes = await upload.read()
        result, error = analyze_skin_tone(image_bytes)
        if error:
            return {"success": False, "message": error}

        # DB에서 셀럽 조회 추가
        with engine.connect() as conn:
            celebs = conn.execute(text("""
                SELECT celeb_name, celeb_skintype, prod_name, brand_name, category, personal_color, img_url
                FROM t_celebrity
                WHERE personal_color = :personal_color
                ORDER BY celeb_name ASC
            """), {"personal_color": result["personal_color"]}).fetchall()

        celeb_list = [
            {
                "celeb_name": row[0],
                "celeb_skintype": row[1],
                "prod_name": row[2],
                "brand_name": row[3],
                "category": row[4],
                "personal_color": row[5],
                "img_url": row[6]
            }
            for row in celebs
        ]

        return {
            "success": True,
            "personal_color": result["personal_color"],
            "description": result["description"],
            "colors": result["colors"],
            "avoid": result["avoid"],
            "makeup": result["makeup"],
            "celebs": celeb_list
        }
    except Exception as e:
        print(f"[ERROR] {str(e)}")
        import traceback
        traceback.print_exc()
        return {"success": False, "message": str(e)}
    
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="info")
