/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.utils.player;

import meteordevelopment.meteorvnclient.MeteorVNClient;
import meteordevelopment.meteorvnclient.addons.AddonManager;
import meteordevelopment.meteorvnclient.addons.GithubRepo;
import meteordevelopment.meteorvnclient.addons.MeteorAddon;
import meteordevelopment.meteorvnclient.gui.GuiThemes;
import meteordevelopment.meteorvnclient.gui.screens.CommitsScreen;
import meteordevelopment.meteorvnclient.mixininterface.IText;
import meteordevelopment.meteorvnclient.utils.network.Http;
import meteordevelopment.meteorvnclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorvnclient.utils.render.MeteorToast;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import static meteordevelopment.meteorvnclient.MeteorVNClient.mc;

public class TitleScreenCredits {
    private static final List<Credit> credits = new ArrayList<>();

    private static final Identifier BACKGROUND_TEXTURE = MeteorVNClient.identifier("textures/mainscreen/default_background.png");
    private static final Identifier LOGO_TEXTURE = MeteorVNClient.identifier("textures/mainscreen/meteorvn_logo.png");

    private TitleScreenCredits() {
    }

    private static void init() {
        for (MeteorAddon addon : AddonManager.ADDONS) add(addon);

        credits.sort(Comparator.comparingInt(value -> value.addon == MeteorVNClient.ADDON ? Integer.MIN_VALUE : -mc.textRenderer.getWidth(value.text)));

        MeteorExecutor.execute(() -> {
            for (Credit credit : credits) {
                if (credit.addon.getRepo() == null) continue;

                GithubRepo repo = credit.addon.getRepo();
                // Lấy thông tin release mới nhất từ GitHub API (không cần xác thực)
                String releaseUrl = "https://api.github.com/repos/hongminh54/meteorvn-client/releases/latest";
                Http.Request request = Http.get(releaseUrl);
                HttpResponse<String> res = request.sendStringResponse();

                if (res.statusCode() == Http.SUCCESS) {
                    String json = res.body();
                    // Trích xuất phiên bản từ JSON
                    String latestVersionStr = parseVersionFromJson(json);
                    if (latestVersionStr != null) {
                        try {
                            meteordevelopment.meteorvnclient.utils.misc.Version latestVersion = new meteordevelopment.meteorvnclient.utils.misc.Version(latestVersionStr);
                            credit.latestVersion = latestVersion;

                            // Lấy phiên bản hiện tại của addon từ file JAR
                            String currentVersionStr = getAddonVersion(credit.addon);
                            meteordevelopment.meteorvnclient.utils.misc.Version currentVersion = new meteordevelopment.meteorvnclient.utils.misc.Version(currentVersionStr);
                            if (latestVersion.isHigherThan(currentVersion)) { // Có phiên bản mới
                                synchronized (credit.text) {
                                    credit.text.append(Text.literal("(Đã có phiên bản mới " + latestVersion + "!)").formatted(Formatting.GOLD));
                                    ((IText) credit.text).meteor$invalidateCache();
                                    credit.hasUpdate = true; // Đánh dấu có cập nhật
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            MeteorVNClient.LOG.error("Invalid version string for addon '{}': {}", credit.addon.name, latestVersionStr, e);
                        }
                    }
                } else if (res.statusCode() == Http.FORBIDDEN) {
                    MeteorVNClient.LOG.warn("Could not fetch updates for addon '{}': Rate-limited by GitHub.", credit.addon.name);
                } else {
                    MeteorVNClient.LOG.warn("Failed to fetch release info for addon '{}': Status code {}", credit.addon.name, res.statusCode());
                }
            }
        });
    }

    private static void add(MeteorAddon addon) {
        Credit credit = new Credit(addon);
        Credit test = new Credit(addon);

        credit.text.append(Text.literal(addon.name).styled(style -> style.withColor(addon.color.getPacked())));
        credit.text.append(Text.literal(" được làm bởi ").formatted(Formatting.GRAY));
        test.text.append(Text.literal("Thuộc bản quyền của").formatted(Formatting.GREEN));
        test.text.append(Text.literal(" meteordevelopment ").formatted(Formatting.DARK_PURPLE));

        for (int i = 0; i < addon.authors.length; i++) {
            if (i > 0) {
                credit.text.append(Text.literal(i == addon.authors.length - 1 ? " & " : ", ").formatted(Formatting.GRAY));
            }

            credit.text.append(Text.literal(addon.authors[i]).formatted(Formatting.WHITE));
        }

        credits.add(credit);
        credits.add(test);
    }

    public static void render(DrawContext context) {
        if (credits.isEmpty()) init();

        int screenWidth = mc.currentScreen.width;
        int screenHeight = mc.currentScreen.height;

        // Vẽ background gradient màu tím hồng
        renderGradientBackground(context, screenWidth, screenHeight);

        // Vẽ tiêu đề "MeteorVN Client" với kích thước lớn hơn
        renderTitle(context, screenWidth, screenHeight);

        // Vẽ hiệu ứng sao băng trên background
        MeteorShower.updateAndRender(context, screenWidth, screenHeight);

        // Vẽ credits và nút tải xuống
        int y = 3;
        for (Credit credit : credits) {
            synchronized (credit.text) {
                int x = mc.currentScreen.width - 3 - mc.textRenderer.getWidth(credit.text);
                context.drawTextWithShadow(mc.textRenderer, credit.text, x, y, -1);

                if (credit.hasUpdate) {
                    // Vẽ nút tải xuống (hình chữ nhật với "↓")
                    credit.buttonX = mc.currentScreen.width - 13; // Cách lề phải 3px, cách văn bản 10px
                    credit.buttonY = y;
                    context.fill(credit.buttonX, credit.buttonY, credit.buttonX + credit.buttonWidth, credit.buttonY + credit.buttonHeight, 0xFFAAAAAA); // Màu xám nhạt
                    context.drawText(mc.textRenderer, "↓", credit.buttonX + 3, credit.buttonY + 1, 0xFFFFFFFF, false); // Mũi tên trắng
                }
            }
            y += mc.textRenderer.fontHeight + 2;
        }
    }

    private static void renderTitle(DrawContext context, int screenWidth, int screenHeight) {
        try {
            // Tính toán kích thước logo responsive dựa trên màn hình
            float scaleFactor = Math.min(screenWidth / 1920.0f, screenHeight / 1080.0f); // Scale dựa trên 1920x1080
            scaleFactor = Math.max(0.5f, Math.min(1.5f, scaleFactor)); // Giới hạn scale từ 0.5x đến 1.5x

            int baseLogoWidth = 256; // Kích thước base của logo
            int baseLogoHeight = 64;
            int logoWidth = (int) (baseLogoWidth * scaleFactor);
            int logoHeight = (int) (baseLogoHeight * scaleFactor);

            // Positioning căn chỉnh đẹp hơn
            int x = (screenWidth - logoWidth) / 2; // Căn giữa ngang
            int y = (int) (screenHeight * 0.15f); // 15% từ trên xuống thay vì fixed 60px

            // Vẽ shadow cho logo (tạo depth)
            int shadowOffset = (int) (2 * scaleFactor);
            context.fill(x + shadowOffset, y + shadowOffset,
                        x + logoWidth + shadowOffset, y + logoHeight + shadowOffset,
                        0x40000000); // Shadow đen nhẹ

            // Vẽ logo chính
            context.drawTexture(RenderLayer::getGuiTextured, LOGO_TEXTURE,
                x, y, 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);

        } catch (Exception e) {
            // Fallback về text cũ nếu không load được logo
            renderFallbackTitle(context, screenWidth, screenHeight);
        }
    }

    private static void renderFallbackTitle(DrawContext context, int screenWidth, int screenHeight) {
        String meteorPart = "Meteor";
        String vnPart = "VN";
        String clientPart = " Client";

        // Tính toán scale cho text dựa trên màn hình
        float textScale = Math.min(screenWidth / 1920.0f, screenHeight / 1080.0f);
        textScale = Math.max(0.8f, Math.min(2.0f, textScale));

        int y = (int) (screenHeight * 0.15f); // Cùng vị trí với logo
        int meteorWidth = mc.textRenderer.getWidth(meteorPart);
        int vnWidth = mc.textRenderer.getWidth(vnPart);
        int clientWidth = mc.textRenderer.getWidth(clientPart);
        int totalWidth = meteorWidth + vnWidth + clientWidth;
        int x = (screenWidth - totalWidth) / 2;

        // Vẽ text với shadow đẹp hơn
        int shadowOffset = 2;

        // Shadow
        context.drawText(mc.textRenderer, Text.literal(meteorPart), x + shadowOffset, y + shadowOffset, 0x80000000, false);
        context.drawText(mc.textRenderer, Text.literal(vnPart), x + meteorWidth + shadowOffset, y + shadowOffset, 0x80000000, false);
        context.drawText(mc.textRenderer, Text.literal(clientPart), x + meteorWidth + vnWidth + shadowOffset, y + shadowOffset, 0x80000000, false);

        // Text chính
        context.drawText(mc.textRenderer, Text.literal(meteorPart), x, y, 0xFFFF69B4, false);
        context.drawText(mc.textRenderer, Text.literal(vnPart), x + meteorWidth, y, 0xFFFFFF00, false);
        context.drawText(mc.textRenderer, Text.literal(clientPart), x + meteorWidth + vnWidth, y, 0xFFFFFFFF, false);
    }

    private static void renderGradientBackground(DrawContext context, int screenWidth, int screenHeight) {
        try {
            // Vẽ background image với scaling thông minh
            // Tính toán scale để background luôn cover toàn màn hình
            float bgScale = Math.max(
                (float) screenWidth / 1920.0f,  // Giả sử background gốc là 1920x1080
                (float) screenHeight / 1080.0f
            );

            int scaledWidth = (int) (1920 * bgScale);
            int scaledHeight = (int) (1080 * bgScale);
            int offsetX = (screenWidth - scaledWidth) / 2;
            int offsetY = (screenHeight - scaledHeight) / 2;

            // Vẽ background image
            context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE,
                offsetX, offsetY, 0, 0, scaledWidth, scaledHeight, scaledWidth, scaledHeight);

            // Thêm overlay gradient tinh tế để tạo depth và contrast cho logo
            context.fillGradient(0, 0, screenWidth, screenHeight / 3,
                0x30000000, 0x10000000); // Gradient đen nhẹ ở trên
            context.fillGradient(0, screenHeight * 2 / 3, screenWidth, screenHeight,
                0x10000000, 0x30000000); // Gradient đen nhẹ ở dưới

        } catch (Exception e) {
            // Fallback về gradient cũ nếu không load được texture
            renderFallbackBackground(context, screenWidth, screenHeight);
        }
    }

    private static void renderFallbackBackground(DrawContext context, int screenWidth, int screenHeight) {
        // Gradient đẹp hơn cho fallback
        int colorTop = 0xFF2D1B69;    // Tím đậm
        int colorMid = 0xFF8B5CF6;    // Tím vừa
        int colorBottom = 0xFFEC4899; // Hồng

        // Vẽ gradient 3 màu
        context.fillGradient(0, 0, screenWidth, screenHeight / 2, colorTop, colorMid);
        context.fillGradient(0, screenHeight / 2, screenWidth, screenHeight, colorMid, colorBottom);
    }

    public static boolean onClicked(double mouseX, double mouseY) {
        int y = 3;
        for (Credit credit : credits) {
            int width;
            synchronized (credit.text) {
                width = mc.textRenderer.getWidth(credit.text);
            }
            int x = mc.currentScreen.width - 3 - width;

            // Kiểm tra nhấp vào văn bản để xem commit
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + mc.textRenderer.fontHeight + 2) {
                if (credit.addon.getRepo() != null && credit.addon.getCommit() != null) {
                    mc.setScreen(new CommitsScreen(GuiThemes.get(), credit.addon));
                    return true;
                }
            }

            // Kiểm tra nhấp vào nút tải xuống
            if (credit.hasUpdate && mouseX >= credit.buttonX && mouseX <= credit.buttonX + credit.buttonWidth &&
                mouseY >= credit.buttonY && mouseY <= credit.buttonY + credit.buttonHeight) {
                downloadUpdate(credit.addon);
                return true;
            }

            y += mc.textRenderer.fontHeight + 2;
        }
        return false;
    }

    private static class Credit {
        public final MeteorAddon addon;
        public final MutableText text = Text.empty();
        public boolean hasUpdate = false; // Trạng thái cập nhật
        public int buttonX, buttonY, buttonWidth = 10, buttonHeight = 10;
        public meteordevelopment.meteorvnclient.utils.misc.Version latestVersion = null;

        public Credit(MeteorAddon addon) {
            this.addon = addon;
        }
    }

    private static class Response {
        public Commit commit;
    }

    private static class Commit {
        public String sha;
    }

    private static void downloadUpdate(MeteorAddon addon) {
        MeteorExecutor.execute(() -> {
            try {
                GithubRepo repo = addon.getRepo();
                // Lấy thông tin release mới nhất từ GitHub API
                String releaseUrl = "https://api.github.com/repos/hongminh54/meteorvn-client/releases/latest"; // Repository chính
                Http.Request releaseRequest = Http.get(releaseUrl);
                HttpResponse<String> releaseResponse = releaseRequest.sendStringResponse();

                if (releaseResponse.statusCode() == Http.SUCCESS) {
                    // Phân tích JSON để lấy URL tải file
                    String json = releaseResponse.body();
                    String assetUrl = parseAssetUrl(json);
                    if (assetUrl != null) {
                        // Tải file từ URL tài nguyên (không cần xác thực)
                        Http.Request downloadRequest = Http.get(assetUrl);
                        HttpResponse<InputStream> fileResponse = downloadRequest.sendInputStreamResponse();

                        if (fileResponse == null || fileResponse.statusCode() != Http.SUCCESS) {
                            // Nếu không có sendInputStreamResponse(), dùng cách thay thế
                            byte[] fileData = downloadFileManually(assetUrl, null);
                            if (fileData != null) {
                                String filePath = "meteorvn-client/jars/" + addon.name + ".jar";
                                Files.createDirectories(Paths.get("meteorvn-client/jars/"));
                                Files.write(Paths.get(filePath), fileData);
                                // Lấy phiên bản mới từ JSON để hiển thị trong toast
                                String newVersion = parseVersionFromJson(json);
                                mc.getToastManager().add(new MeteorToast(Items.EMERALD, "Tải xuống thành công", "Đã cập nhập " + addon.name + " lên phiên bản " + newVersion));
                            } else {
                                throw new Exception("Tải về thất bại từ " + assetUrl);
                            }
                        } else {
                            byte[] fileData = fileResponse.body().readAllBytes();
                            String filePath = "meteorvn-client/jars/" + addon.name + ".jar";
                            Files.createDirectories(Paths.get("meteor-client/addons/"));
                            Files.write(Paths.get(filePath), fileData);
                            String newVersion = parseVersionFromJson(json);
                            mc.getToastManager().add(new MeteorToast(Items.EMERALD, "Tải xuống thành công", "Đã cập nhập " + addon.name + " lên phiên bản " + newVersion));
                        }
                    } else {
                        throw new Exception("Không có phiên bản mới!.");
                    }
                } else if (releaseResponse.statusCode() == Http.FORBIDDEN) {
                    throw new Exception("Bạn đã bị GitHub giới hạn tốc độ. Hãy cân nhắc sử dụng mã thông báo truy cập để thực hiện thêm yêu cầu..");
                } else {
                    throw new Exception("Không thể lấy thông tin phiên bản phát hành mới nhất.: " + releaseResponse.statusCode());
                }
            } catch (Exception e) {
                mc.getToastManager().add(new MeteorToast(Items.BARRIER, "Tải xuống thất bại", e.getMessage()));
                MeteorVNClient.LOG.error("Không thể tải xuống bản cập nhật cho '{}'.", addon.name, e);
            }
        });
    }

    // Phương thức thay thế để tải file thủ công nếu không có sendInputStreamResponse()
    private static byte[] downloadFileManually(String url, @Nullable GithubRepo repo) throws IOException {
        URL downloadUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();

        // Không thêm header "Authorization" vì không dùng token
        connection.setRequestProperty("Accept", "application/octet-stream");
        try (InputStream in = connection.getInputStream()) {
            return in.readAllBytes();
        } finally {
            connection.disconnect();
        }
    }

    // Hàm phân tích JSON để lấy URL tài nguyên (không thay đổi)
    private static String parseAssetUrl(String json) {
        int index = json.indexOf("\"browser_download_url\":\"");
        if (index != -1) {
            int start = index + 23;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
        return null;
    }

    private static String parseVersionFromJson(String json) {
        // Giả định phiên bản nằm trong trường "tag_name" (ví dụ: "v1.0.0" hoặc "1.0.0")
        int index = json.indexOf("\"tag_name\":\"");
        if (index != -1) {
            int start = index + 12; // "tag_name":" dài 12 ký tự
            int end = json.indexOf("\"", start);
            String tag = json.substring(start, end);
            // Loại bỏ tiền tố "v" nếu có (ví dụ: "v1.0.0" -> "1.0.0")
            return tag.startsWith("v") ? tag.substring(1) : tag;
        }
        return null;
    }

    private static String getAddonVersion(MeteorAddon addon) {
        // Giả định file JAR của addon nằm trong thư mục "meteor-client/addons/"
        try {
            String jarPath = "meteor-client/addons/" + addon.name + ".jar";
            JarFile jar = new JarFile(jarPath);
            Attributes attrs = jar.getManifest().getMainAttributes();
            String version = attrs.getValue("Implementation-Version");
            if (version != null && !version.isEmpty()) {
                // Chuẩn hóa phiên bản để đảm bảo định dạng "X.Y.Z"
                return normalizeVersion(version);
            }
        } catch (IOException e) {
            MeteorVNClient.LOG.error("Failed to read version for addon '{}'.", addon.name, e);
        }
        // Fallback: trả về giá trị mặc định hoặc null
        return "1.0.0"; // Giá trị mặc định nếu không tìm thấy
    }

    private static String normalizeVersion(String version) {
        // Chuẩn hóa chuỗi phiên bản để đảm bảo định dạng "X.Y.Z"
        version = version.replace("v", "").replaceAll("[^0-9.]", "");
        String[] parts = version.split("\\.");
        if (parts.length < 3) {
            // Thêm số 0 nếu thiếu
            StringBuilder normalized = new StringBuilder(parts[0]);
            for (int i = 1; i < 3; i++) {
                normalized.append(".").append(parts.length > i ? parts[i] : "0");
            }
            return normalized.toString();
        }
        return version;
    }
}
