# WALLX Security Specification (Phase 0 TDD)

## 1. Data Invariants
1. **Authentication Gate**: Unauthenticated callers cannot read or write any collection.
2. **PII Isolation (`users`)**: User documents contain `email` (PII) and may only be read by the document owner (`request.auth.uid == userId`) or a verified administrator (`isAdmin()`).
3. **Privilege Escalation Prevention**: Normal users creating `/users/{userId}` must set `role == 'user'` and `status == 'active'`, and cannot modify `role` or `status` during updates.
4. **Admin-Only Catalog Mutation**: Only verified administrators (`isAdmin()`) can create, edit metadata of, or delete documents in `/wallpapers`, `/categories`, `/appSettings`, and `/admins`.
5. **Upload Validation**: Every `/wallpapers/{wallpaperId}` document enforces `fileSizeBytes <= 5242880` (5 MB max) and `mimeType in ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']`.
6. **Relational Integrity**: Creating a `/favorites/{favoriteId}` or `/downloads/{downloadId}` record requires `exists(/databases/$(database)/documents/wallpapers/$(incoming().wallpaperId))` and `incoming().userId == request.auth.uid`.
7. **Query Alignment**: All `list` operations check `resource.data` (`status == 'published'`, `enabled == true`, `userId == request.auth.uid`, `visibility == 'public'`, or `isAdmin()`).

## 2. The Dirty Dozen Payloads
1. **Unauthenticated Scrape**: Unauthenticated `get`/`list` on `/wallpapers` -> REJECTED.
2. **Cross-User PII Leak**: Alice (`alice_123`) attempts `get` on `/users/bob_456` -> REJECTED.
3. **Self-Assigned Admin Role**: User creates `/users/alice_123` with `role: 'admin'` -> REJECTED.
4. **Role Escalation Update**: User updates `/users/alice_123` with `role: 'admin'` -> REJECTED.
5. **Unverified Admin Email Spoof**: User with `email: 'd99312907@gmail.com'` but `email_verified: false` attempts to create a wallpaper -> REJECTED.
6. **Unauthorized Wallpaper Upload**: Non-admin user attempts `create` on `/wallpapers/w1` -> REJECTED.
7. **Oversized Wallpaper (> 5 MB)**: Admin attempts `create` on `/wallpapers/w1` with `fileSizeBytes: 6000000` -> REJECTED.
8. **Invalid MIME Type**: Admin attempts `create` on `/wallpapers/w1` with `mimeType: 'application/x-msdownload'` -> REJECTED.
9. **Orphaned Favorite**: User attempts `create` on `/favorites/fav1` referencing non-existent `wallpaperId` -> REJECTED.
10. **Cross-User Favorite Spoof**: Alice attempts `create` on `/favorites/fav1` with `userId: 'bob_456'` -> REJECTED.
11. **Shadow Ghost Field Injection**: User updates `/users/alice_123` adding `isSuperAdmin: true` -> REJECTED.
12. **Unfiltered Query Scraping**: Non-admin user lists `/favorites` without `.where('userId', '==', uid)` -> REJECTED.
