# 📦 보급품 내용물 및 NBT 데이터 정리

하나의 상자에 여러 아이템이 드롭될 수 있습니다.
(예: 1번칸엔 16개의 조약돌, 6번칸엔 황금당근 4개, 12번칸엔 다이아몬드 곡괭이 1개, 18번칸엔 Glock 17 1개, 24번칸엔 9mm 탄약 20발)

---

## 🎲 상자 루팅 시스템 (Loot Generation Flow)

### 드롭 순서 및 확률
| 순서 | 카테고리 | 확률 | 최대 개수 | 비고 |
|:---:|:---|:---:|:---:|:---|
| 1 | 총기 | **35%** | 1개 | 티어별 가중치: Common 50 / Uncommon 30 / Rare 15 / Epic 4 / Legendary 1 |
| 2 | 부착물 | 슬롯당 **8%** | 2개 | - |
| 3 | 음식 | 슬롯당 **15%** | 3개 | - |
| 4 | 장비 (갑옷/도구) | 슬롯당 **10%** | 2개 | - |
| 5 | 조약돌 (16개) | **25%** | 1칸 | - |
| 6 | 탄약 | 빈 칸당 **6%** | 제한 없음 | 마지막에 롤, 50% 1세트 / 50% 반세트 |

### 탄약 드롭 규칙
*   **총기가 있는 상자**: 해당 총기에 맞는 탄약만 드롭.
*   **총기가 없는 상자**: 랜덤한 종류의 탄약 드롭.
*   드롭 시 **50% 확률**로 1세트(최대 수량), **50% 확률**로 반세트(최대 수량의 절반).

---

## 🔫 [TACZ] 총기 (Guns)
*모든 총기는 기본적으로 장전된 상태(HasBulletInBarrel:1b)로 지급되며, 옆에 사용 탄약이 표기되어 있습니다.*

### ⚪ **Common** (가중치: 50)
- **Glock 17** (사용 탄약: **9mm**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:17,GunFireMode:"SEMI",GunId:"tacz:glock_17",HasBulletInBarrel:1b}}`
- **M1911** (사용 탄약: **45acp**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:7,GunFireMode:"SEMI",GunId:"tacz:m1911",HasBulletInBarrel:1b}}`
- **UZI** (사용 탄약: **9mm**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:12,GunFireMode:"AUTO",GunId:"tacz:uzi",HasBulletInBarrel:1b}}`
- **MP5A5** (사용 탄약: **9mm**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:20,GunFireMode:"AUTO",GunId:"tacz:hk_mp5a5",HasBulletInBarrel:1b}}`
- **DB Long** (사용 탄약: **12g**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:2,GunFireMode:"SEMI",GunId:"tacz:db_long",HasBulletInBarrel:1b}}`
- **M870** (사용 탄약: **12g**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:5,GunFireMode:"SEMI",GunId:"tacz:m870",HasBulletInBarrel:1b}}`

### 🟢 **Uncommon** (가중치: 30)
- **SCAR-L** (사용 탄약: **556x45**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:30,GunFireMode:"AUTO",GunId:"tacz:scar_l",HasBulletInBarrel:1b}}`
- **AUG** (사용 탄약: **556x45**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:30,GunFireMode:"AUTO",GunId:"tacz:aug",HasBulletInBarrel:1b}}`
- **M16A1** (사용 탄약: **556x45**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:20,GunFireMode:"AUTO",GunId:"tacz:m16a1",HasBulletInBarrel:1b}}`
- **HK416D** (사용 탄약: **556x45**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:30,GunFireMode:"AUTO",GunId:"tacz:hk416d",HasBulletInBarrel:1b}}`
- **UMP45** (사용 탄약: **45acp**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:25,GunFireMode:"AUTO",GunId:"tacz:ump45",HasBulletInBarrel:1b}}`
- **Vector 45** (사용 탄약: **45acp**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:21,GunFireMode:"AUTO",GunId:"tacz:vector45",HasBulletInBarrel:1b}}`
- **SPAS-12** (사용 탄약: **12g**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:5,GunFireMode:"SEMI",GunId:"tacz:spas_12",HasBulletInBarrel:1b}}`
- **AA-12** (사용 탄약: **12g**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:6,GunFireMode:"SEMI",GunId:"tacz:aa12",HasBulletInBarrel:1b}}`
- **M1014** (사용 탄약: **12g**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:6,GunFireMode:"SEMI",GunId:"tacz:m1014",HasBulletInBarrel:1b}}`

### 🔵 **Rare** (가중치: 15)
- **Desert Eagle** (사용 탄약: **50ae**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:7,GunFireMode:"SEMI",GunId:"tacz:deagle",HasBulletInBarrel:1b}}`
- **SKS Tactical** (사용 탄약: **762x39**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:10,GunFireMode:"SEMI",GunId:"tacz:sks_tactical",HasBulletInBarrel:1b}}`
- **QBZ-191** (사용 탄약: **556x45**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:30,GunFireMode:"AUTO",GunId:"tacz:qbz_191",HasBulletInBarrel:1b}}`
- **AK-47** (사용 탄약: **762x39**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:30,GunFireMode:"AUTO",GunId:"tacz:ak47",HasBulletInBarrel:1b}}`
- **M700** (사용 탄약: **30_06**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:5,GunFireMode:"SEMI",GunId:"tacz:m700",HasBulletInBarrel:1b}}`
- **Springfield 1873** (사용 탄약: **30_06**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:1,GunFireMode:"SEMI",GunId:"tacz:springfield1873",HasBulletInBarrel:1b}}`

### 🟣 **Epic** (가중치: 4)
- **Desert Eagle Golden** (사용 탄약: **357mag**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:9,GunFireMode:"SEMI",GunId:"tacz:deagle_golden",HasBulletInBarrel:1b}}`
- **Timeless 50** (사용 탄약: **50ae**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:6,GunFireMode:"SEMI",GunId:"tacz:timeless50",HasBulletInBarrel:1b}}`
- **MK14** (사용 탄약: **762x39**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:10,GunFireMode:"SEMI",GunId:"tacz:mk14",HasBulletInBarrel:1b}}`
- **SCAR-H** (사용 탄약: **762x39**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:20,GunFireMode:"SEMI",GunId:"tacz:scar_h",HasBulletInBarrel:1b}}`
- **FN FAL** (사용 탄약: **762x39**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:20,GunFireMode:"SEMI",GunId:"tacz:fn_fal",HasBulletInBarrel:1b}}`
- **AI AWP** (사용 탄약: **338**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:5,GunFireMode:"SEMI",GunId:"tacz:ai_awp",HasBulletInBarrel:1b}}`
- **RPK** (사용 탄약: **762x39**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:40,GunFireMode:"AUTO",GunId:"tacz:rpk",HasBulletInBarrel:1b}}`
- **FN EVOLYS** (사용 탄약: **556x45**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:30,GunFireMode:"AUTO",GunId:"tacz:fn_evolys",HasBulletInBarrel:1b}}`
- **M249** (사용 탄약: **556x45**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:64,GunFireMode:"AUTO",GunId:"tacz:m249",HasBulletInBarrel:1b}}`

### 🟡 **Legendary** (가중치: 1)
- **M107** (사용 탄약: **50bmg**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:10,GunFireMode:"SEMI",GunId:"tacz:m107",HasBulletInBarrel:1b}}`
- **M95** (사용 탄약: **50bmg**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:5,GunFireMode:"SEMI",GunId:"tacz:m95",HasBulletInBarrel:1b}}`
- **Minigun** (사용 탄약: **762x39**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:9999,GunFireMode:"AUTO",GunId:"tacz:minigun",HasBulletInBarrel:1b,HeatAmount:46.0f}}`
- **RPG-7** (사용 탄약: **RPG Rocket**): `{id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:1,GunFireMode:"SEMI",GunId:"tacz:rpg7",HasBulletInBarrel:1b}}`

---

## 🛠️ [TACZ] 부착물 (Attachments)
*슬롯당 8% 확률, 최대 2개*

### 🔭 스코프 (Scopes)
- **SRO Dot**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:sight_sro_dot"}}`
- **SRS 02**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:sight_srs_02"}}`
- **PK06**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:sight_pk06_rifle"}}`
- **T2**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:sight_t2"}}`
- **552 Sight**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:sight_552"}}`
- **HAMR**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:scope_hamr"}}`
- **LPVO 1-6x**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:scope_lpvo_1_6"}}`
- **MK5HD**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:scope_mk5hd"}}`

### 🔇 총구 (Muzzle)
- **Knight QD**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:muzzle_silencer_knight_qd"}}`
- **Ursus**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:muzzle_silencer_ursus"}}`
- **Vulture**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:muzzle_silencer_vulture"}}`
- **Cyclone D2**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:muzzle_brake_cyclone_d2"}}`
- **Pioneer**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:muzzle_brake_pioneer"}}`
- **Timeless 50**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:muzzle_brake_timeless50"}}`
- **TRex**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:muzzle_brake_trex"}}`

### 🦵 손잡이 / 개머리판 (Grip & Stock)
- **SE-5**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:grip_se_5"}}`
- **Osovets**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:grip_osovets_black"}}`
- **TD Grip**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:grip_td"}}`
- **Vertical Mil**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:grip_vertical_military"}}`
- **RK1 B25U**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:grip_rk1_b25u"}}`
- **OEM Stock Tac**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:oem_stock_tactical"}}`
- **OEM Stock Light**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:oem_stock_light"}}`
- **OEM Stock Heavy**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:oem_stock_heavy"}}`

### 🔋 탄창 / 레이저 (Mag & Laser)
- **Extended 2**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:extended_mag_2"}}`
- **Extended 3**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:extended_mag_3"}}`
- **Light Ext 2**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:light_extended_mag_2"}}`
- **Light Ext 3**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:light_extended_mag_3"}}`
- **Shotgun Ext 3**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:shotgun_extended_mag_3"}}`
- **Sniper Ext 3**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:sniper_extended_mag_3"}}`
- **Ammo Mod I** (희귀): `{id:"tacz:attachment",tag:{AttachmentId:"tacz:ammo_mod_i"}}`
- **Ammo Mod HE** (희귀): `{id:"tacz:attachment",tag:{AttachmentId:"tacz:ammo_mod_he"}}`
- **Ammo Mod FMJ** (희귀): `{id:"tacz:attachment",tag:{AttachmentId:"tacz:ammo_mod_fmj"}}`
- **Laser Compact**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:laser_compact"}}`
- **Laser Lopro**: `{id:"tacz:attachment",tag:{AttachmentId:"tacz:laser_lopro"}}`

---

## 🎯 [TACZ] 탄약 (Ammo)
*빈 칸당 6% 확률, 50% 1세트 / 50% 반세트*
- **50ae**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:50ae"}}`     최대 48개
- **9mm**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:9mm"}}`       최대 60개
- **357mag**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:357mag"}}` 최대 48개
- **308**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:308"}}`       최대 48개
- **45acp**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:45acp"}}`   최대 60개
- **556x45**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:556x45"}}` 최대 60개
- **30_06**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:30_06"}}`   최대 36개
- **338**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:338"}}`       최대 48개
- **50bmg**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:50bmg"}}`   최대 30개
- **762x39**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:762x39"}}` 최대 60개
- **12g**: `{id:"tacz:ammo",tag:{AmmoId:"tacz:12g"}}`       최대 36개
- **RPG Rocket** (희귀): `{id:"tacz:ammo",tag:{AmmoId:"tacz:rpg_rocket"}}` 최대 6개

---

## 🍗 음식 (Food)
*슬롯당 15% 확률, 최대 3개*
- **서바이벌 기본**: 구운 소고기, 구운 돼지고기, 스테이크, 구운 닭고기
- **고급 소비템**: 황금당근, 황금 사과 (희귀)

---

## 🛡️ 장비 (Equipment)
*슬롯당 10% 확률, 최대 2개*
- **철 장비**: 철 헬멧, 철 흉갑, 철 레깅스, 철 부츠, 철 검, 철 곡괭이
- **다이아 장비** (희귀): 다이아 헬멧, 다이아 흉갑, 다이아 레깅스, 다이아 부츠, 다이아 검, 다이아 곡괭이

---

## 🧱 조약돌 (Cobblestone)
*25% 확률, 16개 고정*
- 엄폐용 건축 재료

---

## 📊 총기 티어별 가중치 요약
| 티어 | 가중치 | 확률 환산 | 대상 예시 |
| :--- | :---: | :---: | :--- |
| **Common** | 50 | 50% | Glock 17, UZI, M870, DB Long, MP5A5, M1911 |
| **Uncommon** | 30 | 30% | SCAR-L, HK416D, AUG, Vector 45, AA-12, UMP45 |
| **Rare** | 15 | 15% | AK-47, Desert Eagle, M700, QBZ-191, SKS |
| **Epic** | 4 | 4% | MK14, AI AWP, M249, SCAR-H, RPK, FN FAL |
| **Legendary** | 1 | 1% | M107, M95, Minigun, RPG-7 |
