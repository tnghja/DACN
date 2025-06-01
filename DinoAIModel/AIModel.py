from fastapi import FastAPI, UploadFile, File, HTTPException
from PIL import Image, UnidentifiedImageError
from io import BytesIO
import timm
import torch
from contextlib import asynccontextmanager

device = "cuda" if torch.cuda.is_available() else "cpu"

@asynccontextmanager
async def lifespan(app: FastAPI):
    model = timm.create_model('vit_base_patch14_reg4_dinov2.lvd142m', pretrained=True, num_classes=0)
    model.eval()
    model.to(device)
    data_config = timm.data.resolve_model_data_config(model)
    transform = timm.data.create_transform(**data_config, is_training=False)
    app.state.model = model
    app.state.transform = transform
    yield

app = FastAPI(lifespan=lifespan)

def extract_image_vector(image, model, transform):
    img_tensor = transform(image).unsqueeze(0).to(device)
    with torch.no_grad():
        features = model.forward_features(img_tensor)
        vector = model.forward_head(features, pre_logits=True)
    return vector.cpu().numpy().flatten().tolist()

@app.post("/extract-vector")
async def extract_vector_api(file: UploadFile = File(...)):
    try:
        file_content = await file.read()
        image = Image.open(BytesIO(file_content)).convert("RGB")
        vector = extract_image_vector(image, app.state.model, app.state.transform)
        return {"vector": vector}
    except UnidentifiedImageError:
        raise HTTPException(status_code=400, detail="Uploaded file is not a valid image.")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error extracting vector: {str(e)}")