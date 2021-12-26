package net.skillbase.fsbm.modules;

import me.nullicorn.nedit.NBTReader;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FlipperHelper extends Module {
    private DecimalFormat formatter;
    private static final String hypixelEndpoint = "https://api.hypixel.net/";
    private static boolean firstRun = true;
    private static final JSONParser parser = new JSONParser();
    private static HashMap<String, FlippableItem> items = new HashMap<>();
    private static ArrayList<Flip> flips = new ArrayList<>();
    static Pattern pattern = Pattern.compile("ExtraAttributes:\\s*([\"{].*(id).*[\"}])\\s*[,}]");
    static Pattern enchantmentsPattern = Pattern.compile("enchantments:\\{(.*?(?=}))");
    private static final List<String> flippableItems = Arrays.asList("YETI_SWORD", "SUPERIOR_DRAGON_CHESTPLATE", "SUPERIOR_DRAGON_LEGGINGS", "SUPERIOR_DRAGON_HELMET", "SUPERIOR_DRAGON_BOOTS", "SHADOW_ASSASSIN_HELMET", "SHADOW_ASSASSIN_CHESTPLATE", "SHADOW_ASSASSIN_LEGGINGS", "SHADOW_ASSASSIN_BOOTS", "RAIDER_AXE", "EDIBLE_MACE", "REAPER_SWORD", "REVENANT_SWORD", "ASPECT_OF_THE_DRAGON", "BONZO_STAFF", "STARRED_BONZO_STAFF", "BAT_WAND", "ICE_SPRAY_WAND", "DAEDALUS_AXE", "FLORID_ZOMBIE_SWORD", "GIANTS_SWORD", "NECROMANCER_SWORD", "ZOMBIE_COMMANDER_WHIP", "PHANTOM_ROD", "SOUL_WHIP", "AXE_OF_THE_SHREDDED", "VOIDWALKER_KATANA", "VORPAL_KATANA", "VOIDEDGE_KATANA", "ATOMSPLIT_KATANA", "ASPECT_OF_THE_VOID", "NECRON_BLADE", "HYPERION", "VALKYRIE", "ASTRAEA", "SCYLLA", "NECRON_HANDLE", "TERMINATOR", "JUDGEMENT_CORE", "NULL_BLADE", "STARRED_SHADOW_ASSASSIN_HELMET", "STARRED_SHADOW_ASSASSIN_CHESTPLATE", "STARRED_SHADOW_ASSASSIN_LEGGINGS", "STARRED_SHADOW_ASSASSIN_BOOTS", "PERFECT_HELMET_12", "PERFECT_CHESTPLATE_12", "PERFECT_LEGGINGS_12", "PERFECT_BOOTS_12", "PERFECT_HELMET_11", "PERFECT_CHESTPLATE_11", "PERFECT_LEGGINGS_11", "PERFECT_BOOTS_11", "PERFECT_HELMET_10", "PERFECT_CHESTPLATE_10", "PERFECT_LEGGINGS_10", "PERFECT_BOOTS_10", "PERFECT_HELMET_9", "PERFECT_CHESTPLATE_9", "PERFECT_LEGGINGS_9", "PERFECT_BOOTS_9", "PERFECT_HELMET_8", "PERFECT_CHESTPLATE_8", "PERFECT_LEGGINGS_8", "PERFECT_BOOTS_8", "PERFECT_HELMET_7", "PERFECT_CHESTPLATE_7", "PERFECT_LEGGINGS_7", "PERFECT_BOOTS_7", "PERFECT_HELMET_6", "PERFECT_CHESTPLATE_6", "PERFECT_LEGGINGS_6", "PERFECT_BOOTS_6", "PERFECT_HELMET_5", "PERFECT_CHESTPLATE_5", "PERFECT_LEGGINGS_5", "PERFECT_BOOTS_5", "GOLD_LIVID_HEAD", "DIAMOND_LIVID_HEAD", "DIAMOND_SADAN_HEAD", "GOLD_SADAN_HEAD", "DIAMOND_NECRON_HEAD", "GOLD_NECRON_HEAD", "GOLD_PROFESSOR_HEAD", "DIAMOND_FPROFESSOR_HEAD", "GEMSTONE_GAUNTLET", "LIVID_DAGGER", "SHADOW_FURY", "TITANIUM_DRILL_1", "TITANIUM_DRILL_2", "TITANIUM_DRILL_3", "TITANIUM_DRILL_4", "TITANIUM_DRILL_5", "TITANIUM_DRILL_6", "TITANIUM_DRILL_7", "TITANIUM_DRILL_8", "TITANIUM_DRILL_9", "TITANIUM_DRILL_10", "SCYTHE_BLADE", "NECROMANCER_LORD_HELMET", "NECROMANCER_LORD_CHESTPLATE", "NECROMANCER_LORD_LEGGINGS", "NECROMANCER_LORD_BOOTS", "SPEED_WITHER_HELMET", "SPEED_WITHER_CHESTPLATE", "SPEED_WITHER_LEGGINGS", "SPEED_WITHER_BOOTS", "WISE_WITHER_HELMET", "WISE_WITHER_CHESTPLATE", "WISE_WITHER_LEGGINGS", "WISE_WITHER_BOOTS", "POWER_WITHER_HELMET", "POWER_WITHER_CHESTPLATE", "POWER_WITHER_LEGGINGS", "POWER_WITHER_BOOTS", "TANK_WITHER_HELMET", "TANK_WITHER_CHESTPLATE", "TANK_WITHER_LEGGINGS", "TANK_WITHER_BOOTS", "WITHER_HELMET", "WITHER_CHESTPLATE", "WITHER_LEGGINGS", "WITHER_BOOTS", "LIVID_FRAGMENT");
    private static final List<String> reforges = Arrays.asList("fabled:2000000", "withered:2300000", "withered:2300000", "bulky:3600000", "dirty:300000", "salty:100000", "treacherous:500000", "lucky:1600000", "stiff:175000", "spiritual:3500000", "submerged:12000000", "giant:1400000", "loving:900000", "warped:4600000", "renowned:11000000", "ancient:730000", "necrotic:400000", "perfect:650000", "suspicious:1500000");
    private static final List<String> goodEnchantments = Arrays.asList("growth:7:230000000", "growth:6:700000", "protection:7:50000000", "protection:6:300000", "chimera:x:100000000", "ultimate_one_for_all:1:8000000", "ultimate_wise:x:160000", "wisdom:x:150000", "overload:x:700000", "vicious:x:2125000", "smite:7:1000000", "ultimate_last_stand:x:220000", "sharpness:7:25000000", "sharpness:6:180000", "ultimate_rend:x:440000", "ultimate_soul_eater:x:1500000", "ultimate_legion:x:1500000");
    private int timer = 1001;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean verified = true;

    public String createSha1(File file) throws IOException {
        InputStream fis = new FileInputStream(file);
        String digest = DigestUtils.sha1Hex(fis);
        fis.close();
        return digest;
    }

    @Override // net.skillbase.fsbm.modules.Module
    public void setup(String name, boolean loadedBefore) {
        super.setup(name, loadedBefore);
        this.verified = true;
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols myNumber = formatter.getDecimalFormatSymbols();
        myNumber.setGroupingSeparator(',');
        formatter.setDecimalFormatSymbols(myNumber);
        this.formatter = formatter;
    }

    @Override // net.skillbase.fsbm.modules.Module
    public void onEnable() {
        this.timer = 1001;
    }

    public static JSONObject doRequest(String url) throws IOException, ParseException {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(url).openConnection();
        httpsURLConnection.setDoInput(true);
        Scanner in = new Scanner(httpsURLConnection.getResponseCode() == 200 ? httpsURLConnection.getInputStream() : httpsURLConnection.getErrorStream());
        StringBuilder jsonString = new StringBuilder();
        while (in.hasNext()) {
            jsonString.append(in.next());
        }
        return (JSONObject) parser.parse(jsonString.toString());
    }

    @Override // net.skillbase.fsbm.modules.Module
    public void onUpdate(TickEvent.ClientTickEvent event) throws Exception {
        if (this.verified) {
            if (this.timer >= 1000) {
                System.out.println("Time up!");
                this.timer = 0;
                try {
                    this.moduleManager.getScheduler().execute(() -> {
                        System.out.println("Starting!");
                        JSONObject firstRequest = null;
                        try {
                            firstRequest = doRequest("https://api.hypixel.net/skyblock/auctions");
                        } catch (IOException | ParseException e) {
                        }
                        int pagesCount = Integer.parseInt(firstRequest.get("totalPages").toString());
                        System.out.println("Parsing through " + pagesCount + " pages. Total " + Integer.parseInt(firstRequest.get("totalAuctions").toString()) + " auctions");
                        parsePage((JSONArray) firstRequest.get("auctions"));
                        ArrayList<Thread> threads = new ArrayList<>();
                        for (int i = 1; i < pagesCount; i++) {
                            int finalI = i;
                            Thread thread = new Thread(() -> {
                                try {
                                    parsePage((JSONArray) doRequest("https://api.hypixel.net/skyblock/auctions?page=" + finalI).get("auctions"));
                                } catch (Exception e2) {
                                }
                            });
                            thread.setDaemon(true);
                            thread.start();
                            threads.add(thread);
                        }
                        Iterator<Thread> it = threads.iterator();
                        while (it.hasNext()) {
                            try {
                                it.next().join(10000);
                            } catch (InterruptedException e2) {
                                e2.printStackTrace();
                            }
                        }
                        for (Map.Entry<String, FlippableItem> entry : items.entrySet()) {
                            FlippableItem item = entry.getValue();
                            if (item.lowestPriceAfter == 0) {
                                item.lowestPriceAfter = item.lowestPrice;
                            }
                            int diff = item.lowestPriceAfter - item.lowestPrice;
                            double itemAspect = (double) (((float) item.lowestPriceAfter) / ((float) item.lowestPrice));
                            if (diff > 50000 && itemAspect >= 1.3d) {
                                if (itemAspect > 1.5d) {
                                    int i2 = (int) (((double) item.lowestPriceAfter) + (itemAspect / 10.0d));
                                }
                                String str = "/viewauction " + item.lowestBinAh;
                            }
                        }
                        if (!firstRun) {
                            this.mc.ingameGUI.getChatGUI().clearChatMessages();
                            this.mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "===================================="));
                            Iterator<Flip> it2 = flips.iterator();
                            while (it2.hasNext()) {
                                Flip flip = it2.next();
                                ClientCommandHandler.instance.executeCommand(this.mc.thePlayer, "sendflip " +
                                        flip.getItemName() +
                                        " " + this.formatter.format((long)
                                        (flip.getMustBe() - flip.getPrice())) + " " + this.formatter.format((long) flip.getMustBe()) + " " + this.formatter.format((long) flip.getPrice()));
                            }
                            this.mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "=================================================="));
                            flips.clear();
                        } else {
                            firstRun = false;
                        }
                        System.out.println("parsed;");
                    });
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } else {
                this.timer++;
            }
        }
    }
    @Override // net.skillbase.fsbm.modules.Module
    protected KeyBinding registerKeybinding() {
        return new KeyBinding("Toggle AutoFlipper", 82, "fsbm.category");
    }

    private static void parsePage(JSONArray page) {
        String[] wrapped;
        Iterator it = page.iterator();
        while (it.hasNext()) {
            JSONObject auction = (JSONObject) it.next();
            if (auction.get("bin") != null && Boolean.parseBoolean(auction.get("bin").toString())) {
                try {
                    String itemData = NBTReader.readBase64(auction.get("item_bytes").toString()).toString();
                    Matcher matcher = pattern.matcher(itemData);
                    int price = Integer.parseInt(auction.get("starting_bid").toString());
                    if (matcher.find()) {
                        String[] attrData = matcher.group().replaceAll("\"", "").split(",");
                        String itemId = null;
                        String reforge = null;
                        for (String swing : attrData) {
                            if (swing.startsWith("id")) {
                                itemId = swing.replaceAll("id:", "");
                            } else if (swing.startsWith("modifier")) {
                                reforge = swing.replaceAll("modifier:", "");
                            }
                        }
                        if (itemId != null) {
                            String auctionId = auction.get("uuid").toString();
                            if (!firstRun) {
                                String[] enchantments = new String[0];
                                Matcher enchantmentsMatcher = enchantmentsPattern.matcher(itemData);
                                if (enchantmentsMatcher.find()) {
                                    enchantments = enchantmentsMatcher.group(1).split(",");
                                }
                                boolean foundCool = false;
                                if (items.containsKey(itemId)) {
                                    int mustBePrice = items.get(itemId).lowestPrice;
                                    for (String enchantment : enchantments) {
                                        String[] enchantmentData = enchantment.split(":");
                                        if (enchantmentData.length == 2 && (wrapped = getWrappedEnchantment(enchantmentData)) != null) {
                                            foundCool = true;
                                            mustBePrice = (int) (((double) mustBePrice) + Math.floor((wrapped[1].equals("x") ? Math.pow(2.0d, (double) (Integer.parseInt(enchantmentData[1]) - 1)) * ((double) Integer.parseInt(wrapped[2])) : (double) Integer.parseInt(wrapped[2])) * 0.7d));
                                        }
                                    }
                                    if (reforge != null) {
                                        Iterator<String> it2 = reforges.iterator();
                                        while (true) {
                                            if (!it2.hasNext()) {
                                                break;
                                            }
                                            String goodReforge = it2.next();
                                            if (goodReforge.equals(reforge)) {
                                                String[] reforgeData = goodReforge.split(":");
                                                if (reforgeData.length == 2) {
                                                    mustBePrice += Integer.parseInt(reforgeData[1]);
                                                    break;
                                                }
                                                System.out.println("found abobus: " + goodReforge);
                                            }
                                        }
                                    }
                                    if (itemId.startsWith("STARRED_SHADOW") || itemId.startsWith("STARRED_LAST")) {
                                        mustBePrice += 800000;
                                    }
                                    if (price < mustBePrice) {
                                        flips.add(new Flip(price, mustBePrice, auctionId, auction.get("item_name").toString()));
                                    }
                                    if (foundCool) {
                                    }
                                }
                            }
                            if (flippableItems.contains(itemId)) {
                                if (items.containsKey(itemId)) {
                                    FlippableItem item = items.get(itemId);
                                    if (item.lowestPrice == 0) {
                                        item.lowestPrice = price;
                                    } else if (item.lowestPrice >= price) {
                                        item.lowestPriceAfter = item.lowestPrice;
                                        item.lowestPrice = price;
                                        item.lowestBinAh = auctionId;
                                    }
                                } else {
                                    items.put(itemId, new FlippableItem(Integer.parseInt(auction.get("starting_bid").toString()), 0));
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String[] getWrappedEnchantment(String[] enchantmentData) {
        for (String goodEnchantment : goodEnchantments) {
            String[] goodEnchantData = goodEnchantment.split(":");
            if (goodEnchantData[0].startsWith(enchantmentData[0])) {
                try {
                    if (goodEnchantData[1].equals("x") || goodEnchantData[1].equals(enchantmentData[1])) {
                        return goodEnchantData;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }
        }
        return null;
    }
}