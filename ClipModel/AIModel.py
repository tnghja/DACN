from io import BytesIO

import torch
from PIL import Image, UnidentifiedImageError
from fastapi import HTTPException, FastAPI, UploadFile, File
from transformers import CLIPModel, CLIPProcessor

# Khởi tạo model + processor
device = "cuda" if torch.cuda.is_available() else "cpu"
model = CLIPModel.from_pretrained("openai/clip-vit-base-patch32").to(device)
processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")

app = FastAPI()

def extract_image_vector(image: Image.Image):
    try:
        inputs = processor(images=image, return_tensors="pt").to(device)
        with torch.no_grad():
            image_features = model.get_image_features(**inputs)
        return image_features.cpu().numpy().flatten().tolist()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error while extracting image vector: {str(e)}")

@app.post("/extract-vector")
async def extract_vector_api(file: UploadFile = File(...)):
    try:
        file_content = await file.read()
        image = Image.open(BytesIO(file_content)).convert("RGB")
        vector = extract_image_vector(image)
        return {"vector": vector}
    except UnidentifiedImageError:
        raise HTTPException(status_code=400, detail="Uploaded file is not a valid image.")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error extracting vector: {str(e)}")
