# Pixora — Google Play Store Publishing & Data Safety Guide

## 1. Configured App Identity
- **App Name**: `Pixora` (Compliant: ≤ 30 characters, no emojis, no ALL CAPS, no promotional buzzwords)
- **Package Name (`applicationId`)**: `com.divyansh.pixora`
- **Advertising**: Ad-free (no AdMob SDK or Advertising ID permission)

---

## 2. Ready-to-Copy Play Console Store Listing Texts

### App Title (Max 30 chars)
`Pixora: 4K & 8K Wallpapers`

### Short Description (Max 80 chars)
`Pure Liquid Glass 4K & 8K wallpapers, AI wallpaper studio, and live blur preview.`

### Full Description (Max 4000 chars)
```text
Pixora is a modern 4K and 8K UHD wallpaper platform crafted with a Pure Liquid Glass interface, real-time adjustable optical blur, and an integrated 8K AI Wallpaper Creator.

KEY FEATURES:
• Curated 4K & 8K UHD Collections: Explore AMOLED, Abstract, Nature, Supercars, Space, Minimal, and Cyber collections.
• 8K AI Wallpaper Studio: Describe any scene and synthesize bespoke ultra-high-definition wallpapers with multi-cloud AI engines.
• Interactive Full-Screen Blur & Glass Preview: Test real-time frosted blur, upper canopy blur, and lock/home screen dock refraction before applying any wallpaper.
• 6 Selectable App Designs: Customize your interface layout and optical presets in Settings.
• Cloud Sync: Save favorites and track your wallpaper downloads across devices with Google Sign-In or Email authentication.
```

---

## 3. Google Play Console "Data Safety" Questionnaire Answers
- **Does your app collect or share any of the required user data types?** → **Yes**
- **Is all of the user data collected by your app encrypted in transit?** → **Yes** (HTTPS / TLS 1.3)
- **Do you provide a way for users to request that their data is deleted?** → **Yes** (Built-in under `Settings → Google Play & Data Safety → Delete Account & Data`)
- **Data Types Collected**:
  1. **Personal Info → Name & Email Address**: Collected for Account Management & Cloud Sync (Favorites / Downloads).
