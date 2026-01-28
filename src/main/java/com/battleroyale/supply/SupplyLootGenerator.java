package com.battleroyale.supply;

import com.battleroyale.BattleRoyalePlugin;
import com.battleroyale.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * 보급품 루팅 생성기
 * SUPPLY_DROP_CONFIG.md의 확률과 규칙에 따라 아이템 생성
 */
public class SupplyLootGenerator {

    private final Random random = new Random();
    private final ConfigManager config;

    // 최근 드롭된 총기 이력 추적 (중복 방지용)
    private final List<String> gunHistory = new LinkedList<>();
    private static final int HISTORY_SIZE = 12; // 최근 12개까지 기억 (중복 방지)

    public SupplyLootGenerator(BattleRoyalePlugin plugin) {
        this.config = plugin.getConfigManager();
    }

    /**
     * 보급 상자에 루팅 생성
     */
    public void generateLoot(Inventory inventory) {
        try {
            inventory.clear();

            String selectedGunAmmo = null;

            // 1. 총기 (100% 확률로 항상 1개 포함)
            GunData gun = selectRandomGun();
            if (gun != null) {
                inventory.setItem(getRandomSlot(inventory), gun.itemStack);
                selectedGunAmmo = gun.ammoType;

                // 미니건일 경우 .308 탄약 4세트 확정 (특수 케이스)
                if ("tacz:minigun".equals(gun.gunId)) {
                    for (int i = 0; i < 4; i++) {
                        ItemStack ammo = com.battleroyale.util.TaczItemUtil.createAmmo("tacz:308", 48);
                        int slot = getRandomSlot(inventory);
                        if (slot != -1) {
                            inventory.setItem(slot, ammo);
                        }
                    }
                }
            }

            // 2. 부착물 (config에서 확률 및 최대값 읽기)
            int attachmentCount = 0;
            int maxAttachments = config.getSupplyAttachmentMax();
            double attachmentChance = config.getSupplyAttachmentChance();
            for (int i = 0; i < 27 && attachmentCount < maxAttachments; i++) {
                if (random.nextDouble() < attachmentChance) {
                    ItemStack attachment = getRandomAttachment();
                    if (attachment != null) {
                        inventory.setItem(getRandomSlot(inventory), attachment);
                        attachmentCount++;
                    }
                }
            }

            // 3. 음식 (config에서 확률 및 최대값 읽기)
            int foodCount = 0;
            int maxFood = config.getSupplyFoodMax();
            double foodChance = config.getSupplyFoodChance();
            for (int i = 0; i < 27 && foodCount < maxFood; i++) {
                if (random.nextDouble() < foodChance) {
                    ItemStack food = getRandomFood();
                    if (food != null) {
                        inventory.setItem(getRandomSlot(inventory), food);
                        foodCount++;
                    }
                }
            }

            // 4. 장비 (config에서 확률 및 최대값 읽기)
            int equipmentCount = 0;
            int maxEquipment = config.getSupplyEquipmentMax();
            double equipmentChance = config.getSupplyEquipmentChance();
            for (int i = 0; i < 27 && equipmentCount < maxEquipment; i++) {
                if (random.nextDouble() < equipmentChance) {
                    ItemStack equipment = getRandomEquipment();
                    if (equipment != null) {
                        inventory.setItem(getRandomSlot(inventory), equipment);
                        equipmentCount++;
                    }
                }
            }

            // 5. 조약돌 (config에서 확률 및 개수 읽기)
            if (random.nextDouble() < config.getSupplyCobblestoneChance()) {
                int cobblestoneAmount = config.getSupplyCobblestoneAmount();
                inventory.setItem(getRandomSlot(inventory), new ItemStack(Material.COBBLESTONE, cobblestoneAmount));
            }

            // 5.5. 탄약 상자 (10% 확률로 레벨 0, 1, 2 한 세트 등장)
            if (random.nextDouble() < 0.10) {
                for (int level = 0; level <= 2; level++) {
                    int slot = getRandomSlot(inventory);
                    if (slot != -1) {
                        inventory.setItem(slot, com.battleroyale.util.TaczItemUtil.createAmmoBox(level));
                    }
                }
            }

            // 6. 탄약 생성 로직 (기존 요청대로 복구: 칸당 확률)
            // 총이 있으면 해당 총알이 8.5% 확률로, 없으면 무작위 총알이 4% 확률로 생성
            double ammoChancePerSlot = (selectedGunAmmo != null) ? 0.085 : 0.04;

            for (int i = 0; i < 27; i++) {
                if (inventory.getItem(i) == null && random.nextDouble() < ammoChancePerSlot) {
                    ItemStack ammo = getRandomAmmo(selectedGunAmmo);
                    if (ammo != null) {
                        inventory.setItem(i, ammo);
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[BattleRoyale] 아이템 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 빈 슬롯 찾기
     */
    private int getRandomSlot(Inventory inventory) {
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                emptySlots.add(i);
            }
        }

        if (emptySlots.isEmpty()) {
            return random.nextInt(27);
        }

        return emptySlots.get(random.nextInt(emptySlots.size()));
    }

    /**
     * 티어별 총기 선택
     */
    private GunData selectRandomGun() {
        int commonWeight = config.getGunTierCommon();
        int uncommonWeight = config.getGunTierUncommon();
        int rareWeight = config.getGunTierRare();
        int epicWeight = config.getGunTierEpic();
        int legendaryWeight = config.getGunTierLegendary();
        int totalWeight = commonWeight + uncommonWeight + rareWeight + epicWeight + legendaryWeight;

        if (totalWeight <= 0) {
            Bukkit.getLogger().warning("[BattleRoyale] 총기 티어 확률 설정이 잘못되었습니다 (합계가 0).");
            return null;
        }

        GunData selected = null;
        int maxAttempts = 5; // 중복이 아닌 총을 찾기 위한 최대 시도 횟수

        for (int i = 0; i < maxAttempts; i++) {
            int roll = random.nextInt(totalWeight);
            if (roll < legendaryWeight) {
                selected = getRandomLegendaryGun();
            } else if (roll < legendaryWeight + epicWeight) {
                selected = getRandomEpicGun();
            } else if (roll < legendaryWeight + epicWeight + rareWeight) {
                selected = getRandomRareGun();
            } else if (roll < legendaryWeight + epicWeight + rareWeight + uncommonWeight) {
                selected = getRandomUncommonGun();
            } else {
                selected = getRandomCommonGun();
            }

            // 최근에 드롭된 적이 없는 총기면 루프 종료
            if (selected != null && !gunHistory.contains(selected.gunId)) {
                break;
            }
        }

        // 이력 업데이트
        if (selected != null) {
            gunHistory.add(selected.gunId);
            if (gunHistory.size() > HISTORY_SIZE) {
                gunHistory.remove(0);
            }
        }

        return selected;
    }

    /**
     * Common 총기
     */
    private GunData getRandomCommonGun() {
        List<GunData> guns = Arrays.asList(
                new GunData(createTaczGun("tacz:glock_17", 17, "SEMI"), "9mm", "tacz:glock_17"),
                new GunData(createTaczGun("tacz:m1911", 7, "SEMI"), "45acp", "tacz:m1911"),
                new GunData(createTaczGun("tacz:uzi", 12, "AUTO"), "9mm", "tacz:uzi"),
                new GunData(createTaczGun("tacz:hk_mp5a5", 20, "AUTO"), "9mm", "tacz:hk_mp5a5"),
                new GunData(createTaczGun("tacz:db_long", 2, "SEMI"), "12g", "tacz:db_long"),
                new GunData(createTaczGun("tacz:m870", 5, "SEMI"), "12g", "tacz:m870"));
        GunData selected = guns.get(random.nextInt(guns.size()));
        addTierLore(selected.itemStack, "COMMON", "§f");
        return selected;
    }

    /**
     * Uncommon 총기
     */
    private GunData getRandomUncommonGun() {
        List<GunData> guns = Arrays.asList(
                new GunData(createTaczGun("tacz:scar_l", 30, "AUTO"), "556x45", "tacz:scar_l"),
                new GunData(createTaczGun("tacz:aug", 30, "AUTO"), "556x45", "tacz:aug"),
                new GunData(createTaczGun("tacz:m16a1", 20, "AUTO"), "556x45", "tacz:m16a1"),
                new GunData(createTaczGun("tacz:hk416d", 30, "AUTO"), "556x45", "tacz:hk416d"),
                new GunData(createTaczGun("tacz:ump45", 25, "AUTO"), "45acp", "tacz:ump45"),
                new GunData(createTaczGun("tacz:vector45", 21, "AUTO"), "45acp", "tacz:vector45"),
                new GunData(createTaczGun("tacz:spas_12", 5, "SEMI"), "12g", "tacz:spas_12"),
                new GunData(createTaczGun("tacz:aa12", 6, "SEMI"), "12g", "tacz:aa12"),
                new GunData(createTaczGun("tacz:m1014", 6, "SEMI"), "12g", "tacz:m1014"));
        GunData selected = guns.get(random.nextInt(guns.size()));
        addTierLore(selected.itemStack, "UNCOMMON", "§a");
        return selected;
    }

    /**
     * Rare 총기
     */
    private GunData getRandomRareGun() {
        List<GunData> guns = Arrays.asList(
                new GunData(createTaczGun("tacz:deagle", 7, "SEMI"), "50ae", "tacz:deagle"),
                new GunData(createTaczGun("tacz:sks_tactical", 10, "SEMI"), "762x39", "tacz:sks_tactical"),
                new GunData(createTaczGun("tacz:qbz_191", 30, "AUTO"), "556x45", "tacz:qbz_191"),
                new GunData(createTaczGun("tacz:ak47", 30, "AUTO"), "762x39", "tacz:ak47"),
                new GunData(createTaczGun("tacz:m700", 5, "SEMI"), "30_06", "tacz:m700"),
                new GunData(createTaczGun("tacz:springfield1873", 1, "SEMI"), "30_06", "tacz:springfield1873"));
        GunData selected = guns.get(random.nextInt(guns.size()));
        addTierLore(selected.itemStack, "RARE", "§b");
        return selected;
    }

    /**
     * Epic 총기
     */
    private GunData getRandomEpicGun() {
        List<GunData> guns = Arrays.asList(
                new GunData(createTaczGun("tacz:deagle_golden", 9, "SEMI"), "357mag", "tacz:deagle_golden"),
                new GunData(createTaczGun("tacz:timeless50", 6, "SEMI"), "50ae", "tacz:timeless50"),
                new GunData(createTaczGun("tacz:mk14", 10, "SEMI"), "308", "tacz:mk14"),
                new GunData(createTaczGun("tacz:scar_h", 20, "SEMI"), "308", "tacz:scar_h"),
                new GunData(createTaczGun("tacz:fn_fal", 20, "SEMI"), "308", "tacz:fn_fal"),
                new GunData(createTaczGun("tacz:ai_awp", 5, "SEMI"), "338", "tacz:ai_awp"),
                new GunData(createTaczGun("tacz:rpk", 40, "AUTO"), "762x39", "tacz:rpk"),
                new GunData(createTaczGun("tacz:fn_evolys", 30, "AUTO"), "556x45", "tacz:fn_evolys"),
                new GunData(createTaczGun("tacz:m249", 64, "AUTO"), "556x45", "tacz:m249"));
        GunData selected = guns.get(random.nextInt(guns.size()));
        addTierLore(selected.itemStack, "EPIC", "§d");
        return selected;
    }

    /**
     * Legendary 총기
     */
    private GunData getRandomLegendaryGun() {
        List<GunData> guns = Arrays.asList(
                new GunData(createTaczGun("tacz:m107", 10, "SEMI"), "50bmg", "tacz:m107"),
                new GunData(createTaczGun("tacz:m95", 5, "SEMI"), "50bmg", "tacz:m95"),
                new GunData(createTaczGunWithHeat("tacz:minigun", 9999, "AUTO", 46.0f), "308", "tacz:minigun"),
                new GunData(createTaczGun("tacz:rpg7", 1, "SEMI"), "rpg_rocket", "tacz:rpg7"));
        GunData selected = guns.get(random.nextInt(guns.size()));
        addTierLore(selected.itemStack, "LEGENDARY", "§6");
        return selected;
    }

    /**
     * TACZ 총기 아이템 생성
     */
    private ItemStack createTaczGun(String gunId, int ammo, String fireMode) {
        return com.battleroyale.util.TaczItemUtil.createGun(gunId, ammo, fireMode);
    }

    /**
     * Heat가 있는 TACZ 총기 (미니건용)
     */
    private ItemStack createTaczGunWithHeat(String gunId, int ammo, String fireMode, float heat) {
        return com.battleroyale.util.TaczItemUtil.createGun(gunId, ammo, fireMode, heat);
    }

    /**
     * 랜덤 부착물
     */
    private ItemStack getRandomAttachment() {
        List<String> attachments = Arrays.asList(
                "tacz:sight_sro_dot", "tacz:sight_srs_02", "tacz:sight_pk06_rifle", "tacz:sight_t2",
                "tacz:sight_552", "tacz:scope_hamr", "tacz:scope_lpvo_1_6", "tacz:scope_mk5hd",
                "tacz:muzzle_silencer_knight_qd", "tacz:muzzle_silencer_ursus", "tacz:muzzle_silencer_vulture",
                "tacz:muzzle_brake_cyclone_d2", "tacz:muzzle_brake_pioneer", "tacz:muzzle_brake_timeless50",
                "tacz:grip_se_5", "tacz:grip_osovets_black", "tacz:grip_td", "tacz:grip_vertical_military",
                "tacz:extended_mag_2", "tacz:extended_mag_3", "tacz:light_extended_mag_2",
                "tacz:laser_compact", "tacz:laser_lopro");

        String attachmentId = attachments.get(random.nextInt(attachments.size()));
        return com.battleroyale.util.TaczItemUtil.createAttachment(attachmentId);
    }

    /**
     * 랜덤 음식
     */
    private ItemStack getRandomFood() {
        List<Material> foods = Arrays.asList(
                Material.COOKED_BEEF,
                Material.COOKED_PORKCHOP,
                Material.COOKED_CHICKEN,
                Material.GOLDEN_CARROT,
                Material.GOLDEN_APPLE);

        Material food = foods.get(random.nextInt(foods.size()));
        int amount = food == Material.GOLDEN_APPLE ? 1 : random.nextInt(4) + 1;
        return new ItemStack(food, amount);
    }

    /**
     * 랜덤 장비
     */
    private ItemStack getRandomEquipment() {
        boolean isDiamond = random.nextDouble() < 0.2; // 20% 확률로 다이아

        List<Material> ironEquipment = Arrays.asList(
                Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS,
                Material.IRON_BOOTS, Material.IRON_SWORD, Material.IRON_PICKAXE);

        List<Material> diamondEquipment = Arrays.asList(
                Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS,
                Material.DIAMOND_BOOTS, Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE);

        List<Material> equipment = isDiamond ? diamondEquipment : ironEquipment;
        return new ItemStack(equipment.get(random.nextInt(equipment.size())));
    }

    /**
     * 랜덤 탄약
     */
    private ItemStack getRandomAmmo(String preferredAmmo) {
        Map<String, String> ammoIdMap = new HashMap<>();
        ammoIdMap.put("50ae", "tacz:50ae");
        ammoIdMap.put("9mm", "tacz:9mm");
        ammoIdMap.put("357mag", "tacz:357mag");
        ammoIdMap.put("45acp", "tacz:45acp");
        ammoIdMap.put("556x45", "tacz:556x45");
        ammoIdMap.put("30_06", "tacz:30_06");
        ammoIdMap.put("338", "tacz:338");
        ammoIdMap.put("50bmg", "tacz:50bmg");
        ammoIdMap.put("762x39", "tacz:762x39");
        ammoIdMap.put("12g", "tacz:12g");
        ammoIdMap.put("rpg_rocket", "tacz:rpg_rocket");
        ammoIdMap.put("308", "tacz:308");

        Map<String, Integer> ammoMaxCounts = new HashMap<>();
        ammoMaxCounts.put("50ae", 48);
        ammoMaxCounts.put("9mm", 60);
        ammoMaxCounts.put("357mag", 48);
        ammoMaxCounts.put("45acp", 60);
        ammoMaxCounts.put("556x45", 60);
        ammoMaxCounts.put("30_06", 36);
        ammoMaxCounts.put("338", 48);
        ammoMaxCounts.put("50bmg", 30);
        ammoMaxCounts.put("762x39", 60);
        ammoMaxCounts.put("12g", 36);
        ammoMaxCounts.put("rpg_rocket", 6);
        ammoMaxCounts.put("308", 48);

        String ammoType;
        if (preferredAmmo != null && ammoMaxCounts.containsKey(preferredAmmo)) {
            ammoType = preferredAmmo;
        } else {
            List<String> ammoTypes = new ArrayList<>(ammoMaxCounts.keySet());
            ammoType = ammoTypes.get(random.nextInt(ammoTypes.size()));
        }

        int maxCount = ammoMaxCounts.get(ammoType);
        boolean fullSet = random.nextBoolean(); // 50% 1세트, 50% 반세트
        int amount = fullSet ? maxCount : maxCount / 2;

        String ammoId = ammoIdMap.get(ammoType);
        return com.battleroyale.util.TaczItemUtil.createAmmo(ammoId, amount);
    }

    private void addTierLore(ItemStack item, String tierName, String colorCode) {
        if (item == null)
            return;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add("§7---");
            lore.add("§7Grade: " + colorCode + "§l" + tierName);
            lore.add("§7---");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }

    /**
     * 총기 데이터 클래스
     */
    private static class GunData {
        ItemStack itemStack;
        String ammoType;
        String gunId;

        GunData(ItemStack itemStack, String ammoType, String gunId) {
            this.itemStack = itemStack;
            this.ammoType = ammoType;
            this.gunId = gunId;
        }
    }
}
