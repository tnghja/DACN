# Kafka Consumer Config
import asyncio
import json
import os
from confluent_kafka import Consumer
from pinecone import Pinecone
import requests
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

conf = {
    "bootstrap.servers": os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
    "group.id": os.getenv("KAFKA_GROUP_ID", "fastapi"),
    "auto.offset.reset": "latest",
}

consumer = Consumer(conf)
TOPIC = "postgres.public.product_images"

# Pinecone config
PINECONE_INDEX_NAME = "dino"
PINECONE_API_KEY = os.getenv("PINECONE_API_KEY", "pcsk_5U6tzx_TiqxUcFxBVS4UHxct3cxctBbg8bwXG6dqHU7PZAbnTCrpJ7XpvGkNi6b8815EQD")
pinecone = Pinecone(api_key=PINECONE_API_KEY)
index = pinecone.Index(PINECONE_INDEX_NAME)

# FastAPI to extract vector
AIMODEL_API_URL = os.getenv("AIMODEL_API_URL", "http://localhost:8000/extract-vector")  # Use "aimodel:8000" in Docker Compose


def extract_image_vector_via_api(image_url: str):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
        'Referer': 'https://www.otto.de'  # Adjust based on the website
    }
    try:
        response = requests.post(AIMODEL_API_URL, files={"file": requests.get(image_url, headers= headers, stream=True).raw})
        response.raise_for_status()
        return response.json()["vector"]
    except Exception as e:
        logger.error(f"❌ Failed to extract vector via API for image {image_url}: {e}")
        return None


def upsert_vector_in_pinecone(id: str, product_id: str, image_url: str, embedding):
    if embedding:
        metadata = {"url": image_url, "product_id": product_id}
        index.upsert(vectors=[(id, embedding, metadata)])
        logger.info(f"✅ Upserted vector for ID {id} (product {product_id})")


def delete_vector_from_pinecone(id: str):
    index.delete(ids=[id])
    logger.info(f"🗑️ Deleted vector with ID {id}")


async def consume_kafka():
    consumer.subscribe([TOPIC])
    logger.info("📡 Listening to Kafka topic...")

    try:
        while True:
            msg = consumer.poll(1.0)
            if msg is None:
                await asyncio.sleep(1)
                continue
            if msg.error():
                logger.error(f"❌ Kafka error: {msg.error()}")
                continue

            try:
                data = json.loads(msg.value().decode("utf-8"))
                op = data["payload"]["op"]
                after = data["payload"].get("after", {})
                before = data["payload"].get("before", {})

                if op in ("c", "r"):
                    id = after.get("id")
                    image_url = after.get("url")
                    product_id = after.get("product_id")
                    logger.info(f"📥 Insert {id}: {image_url}")
                    if id and image_url and product_id:
                        embedding = extract_image_vector_via_api(image_url)
                        upsert_vector_in_pinecone(id, product_id, image_url, embedding)

                elif op == "u":
                    id = after.get("id")
                    image_url = after.get("url")
                    product_id = after.get("product_id")
                    logger.info(f"🔄 Update {id}: {image_url}")
                    if id:
                        if not image_url or not product_id:
                            delete_vector_from_pinecone(id)
                        else:
                            embedding = extract_image_vector_via_api(image_url)
                            upsert_vector_in_pinecone(id, product_id, image_url, embedding)

                elif op == "d":
                    id = before.get("id")
                    logger.info(f"🗑️ Delete {id}")
                    if id:
                        delete_vector_from_pinecone(id)

                else:
                    logger.error(f"⚠️ Unknown operation: {op}")

            except Exception as e:
                logger.error(f"❌ Error handling Kafka message: {e}")

    except asyncio.CancelledError:
        logger.error("⏹️ Kafka consumer cancelled")
    finally:
        consumer.close()


async def main():
    logger.info("🚀 Starting Kafka Consumer Listen to " + conf.get("bootstrap.servers"))
    await consume_kafka()


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except Exception as e:
        logger.error(f"❌ Fatal error starting consumer: {e}")
        raise