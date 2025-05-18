# Recombee Recommendation Service

This service provides recommendation features for the e-commerce platform using Recombee API.

## Features

- **User-based recommendations**: Personalized product recommendations for logged-in users
- **Non-signed-in user recommendations**: Various recommendation strategies for anonymous users
- **Item-to-item recommendations**: Find similar items to a specific product
- **Search functionality**: Full-text search with personalization options
- **Interaction tracking**: Record user interactions with products

## API Endpoints

### Signed-in User Recommendations

- `GET /recommend` - Get personalized recommendations for a signed-in user

### Item-to-Item Recommendations

- `GET /recommend-similar-items` - Get items similar to a specific product (with optional user context and filtering)

### Non-signed-in User Recommendations

- `GET /popular` - Get popular items (trending products)
- `GET /new-items` - Get newly added items
- `GET /featured` - Get featured items (editorially curated)
- `POST /context-based` - Get recommendations based on browsing context (recently viewed items, categories)

### Search Functionality

- `GET /search` - Search for items with optional personalization for signed-in users

### Interaction Tracking

- `POST /view` - Record a product view
- `POST /purchase` - Record a product purchase
- `POST /batch-view` - Record multiple product views
- `POST /cart-addition` - Record adding a product to cart
- `POST /bookmark` - Record a product bookmark/favorite

## Example Usage

### Getting Popular Items for Non-signed-in Users

```
GET /popular?count=5
```

Response:
```json
{
  "success": true,
  "data": [
    "product789",
    "product456",
    "product123",
    "product234",
    "product567"
  ]
}
```

### Getting Similar Items

```
GET /recommend-similar-items?itemId=product456&count=5
```

With user context for personalization:
```
GET /recommend-similar-items?itemId=product456&userId=user123&count=5
```

With filtering for category:
```
GET /recommend-similar-items?itemId=product456&filter=categories%20IN%20%5B'electronics'%5D&count=5
```

Response:
```json
{
  "success": true,
  "data": [
    "product789",
    "product235",
    "product872",
    "product653",
    "product901"
  ]
}
```

### Getting Context-based Recommendations

```
POST /context-based?count=5
```

Request Body:
```json
{
  "recentlyViewedItems": ["product123", "product456"],
  "categories": ["electronics", "smartphones"]
}
```

Response:
```json
{
  "success": true,
  "data": [
    "product789",
    "product101",
    "product202",
    "product303",
    "product404"
  ]
}
```

### Searching for Products

```
GET /search?query=smartphone&count=5
```

For personalized search (when user is signed in):
```
GET /search?query=smartphone&userId=user123&count=5
```

With category filter:
```
GET /search?query=smartphone&filter=categories%20IN%20%5B'electronics'%5D&count=5
```

Response:
```json
{
  "success": true,
  "data": [
    "iphone-13",
    "samsung-galaxy-s21",
    "pixel-6",
    "oneplus-9",
    "xiaomi-11t"
  ]
}
```

## How It Works

### Non-signed-in User Recommendations

For users who are not signed in, we offer several recommendation strategies:

1. **Popular Items**: Shows trending products based on overall interaction data

2. **New Items**: Shows recently added products to promote fresh content

3. **Featured Items**: Shows products that have been marked as featured

4. **Context-based Recommendations**: Uses a temporary anonymous session to provide personalized recommendations based on:
   - Recently viewed items (stored in browser localStorage)
   - Category context (from current browsing)

### Item-to-Item Recommendations

Uses Recombee's collaborative filtering technology to recommend products similar to a specific item:

1. Can be used on product detail pages to show "Customers who viewed this also viewed..." sections
2. Supports optional user context for personalization 
3. Supports filtering to restrict recommendations to specific categories

### Implementation Details

For context-based recommendations:

1. A temporary anonymous user ID is created
2. Recently viewed items are associated with this temporary user
3. Recommendations are generated based on this context
4. The temporary user is deleted after generating recommendations

For search functionality:

1. Uses Recombee's personalized full-text search capability
2. Can be used with or without user context
3. Supports filtering by item properties
4. Returns relevant items matching the search query

## Configuration

Configuration is done through environment variables:

- `RECOMBEE_DB_ID` - Your Recombee database ID
- `RECOMBEE_TOKEN` - Your Recombee private token

## Implementation Notes

The service uses the official [Recombee Java API Client](https://github.com/recombee/java-api-client) (version 5.0.0) to interact with the Recombee recommendation engine. The implementation follows best practices for proper error handling and fallback approaches.

## Dependencies

- Spring Boot 3.2.5
- Recombee Java API Client 5.0.0
