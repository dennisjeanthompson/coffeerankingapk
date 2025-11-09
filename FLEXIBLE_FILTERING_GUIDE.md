# Flexible Coffee Shop Filtering - Implementation Notes

## What Changed

Updated `CoffeeShopRepository.getAllCoffeeShops()` to use **flexible, case-insensitive filtering** instead of exact match.

## Filtering Logic

The app now matches coffee shops using these keywords (case-insensitive):
- `coffee`
- `cafe`
- `café` (with accent)
- `espresso`
- `cappuccino`

### Search Strategy

1. Fetches **all documents** from `coffeeShops` collection
2. Filters client-side by checking if **type** OR **name** contains any keyword
3. Case-insensitive matching (`ignoreCase = true`)

## Examples of What Will Match

✅ **Will be included:**
- `type: "coffee shop"` ✓
- `type: "Coffee Shop"` ✓ (different case)
- `type: "COFFEE SHOP"` ✓ (all caps)
- `type: "cafe"` ✓
- `type: "Café"` ✓ (with accent)
- `type: "Espresso Bar"` ✓ (contains "espresso")
- `type: "Cappuccino Lounge"` ✓ (contains "cappuccino")
- `name: "Starbucks Coffee"` ✓ (name contains "coffee")
- `name: "Central Cafe"` ✓ (name contains "cafe")

❌ **Will be excluded:**
- `type: "restaurant"` ✗
- `type: "bar"` ✗
- `type: "bakery"` ✗ (unless name has coffee-related keyword)
- `name: "Joe's Diner"` with `type: "diner"` ✗

## Code Implementation

```kotlin
val coffeeRelatedKeywords = listOf("coffee", "cafe", "café", "espresso", "cappuccino")

val isCoffeeRelated = coffeeRelatedKeywords.any { keyword ->
    type.contains(keyword, ignoreCase = true) || 
    name.contains(keyword, ignoreCase = true)
}
```

## Sample Data Updated

`SampleDataSeeder` now creates 6 shops with different formats to demonstrate flexibility:

1. **Blue Bottle Coffee** - `type: "coffee shop"` (lowercase)
2. **Philz Coffee** - `type: "Coffee Shop"` (capitalized)
3. **Sightglass Coffee** - `type: "cafe"` (just "cafe")
4. **Four Barrel Coffee** - `type: "Espresso Bar"` (contains "espresso")
5. **Ritual Coffee Roasters** - `type: "Café"` (with accent)
6. **The Mill** - `type: "COFFEE HOUSE"` (all caps)

All 6 will be returned by `getAllCoffeeShops()` despite different formatting.

## Performance Considerations

### Current Approach (Client-Side Filtering)
- **Pros:**
  - Very flexible - no schema changes needed
  - Case-insensitive matching works automatically
  - Can match keywords in name OR type
  - Easy to add more keywords

- **Cons:**
  - Fetches ALL documents from collection (not scalable for 1000+ shops)
  - More bandwidth usage
  - Filtering happens after download

### Scalable Alternative (If you have 100+ shops)

Use a **keywords array field** in Firestore:

```javascript
// Firestore document structure
{
  "name": "Blue Bottle Coffee",
  "type": "Coffee Shop",
  "keywords": ["coffee", "cafe", "espresso"], // Searchable array
  "location": GeoPoint(...),
  // ...
}

// Query in repository
coffeeShopsCollection
    .whereArrayContainsAny("keywords", listOf("coffee", "cafe"))
    .get()
```

This uses server-side filtering (faster, less bandwidth).

## When to Switch Approaches

- **< 100 shops**: Current client-side filtering is fine
- **100-500 shops**: Consider pagination + client-side filter
- **500+ shops**: Switch to keywords array + `whereArrayContainsAny`
- **1000+ shops**: Add Algolia or Elasticsearch for full-text search

## Adding More Keywords

To include more coffee-related terms, update the keywords list:

```kotlin
val coffeeRelatedKeywords = listOf(
    "coffee", 
    "cafe", 
    "café", 
    "espresso", 
    "cappuccino",
    "latte",      // Add these
    "mocha",
    "barista",
    "roastery",
    "coffeehouse"
)
```

## Excluding False Positives

If you get unwanted matches (e.g., "Coffee Table Store"), add exclusion logic:

```kotlin
val excludeKeywords = listOf("table", "furniture", "shop")

val shouldExclude = excludeKeywords.any { keyword ->
    type.contains(keyword, ignoreCase = true)
}

if (isCoffeeRelated && !shouldExclude) {
    // Include this shop
}
```

## Testing

Run the seeder again to test with 6 different formats:
```kotlin
SampleDataSeeder.seedSampleCoffeeShops()
```

Expected result: Map shows **6 markers** with different type formats, all matching successfully.

## Firestore Console Example

Your Firestore documents can now have any of these formats:

```javascript
// Document 1
{ type: "coffee shop", name: "Brew Bar" }

// Document 2  
{ type: "Café", name: "French Corner" }

// Document 3
{ type: "COFFEE HOUSE", name: "The Mill" }

// Document 4
{ type: "restaurant", name: "Joe's Coffee Diner" } // Matches on name

// All 4 will be returned by getAllCoffeeShops()
```
