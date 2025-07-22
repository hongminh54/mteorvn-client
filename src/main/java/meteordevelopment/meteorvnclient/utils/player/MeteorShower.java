package meteordevelopment.meteorvnclient.utils.player;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MeteorShower {
    private static final Random random = new Random();
    private static final List<Meteor> meteors = new ArrayList<>();

    static class Meteor {
        float x, y, speed, angle;
        int life;

        Meteor(int screenWidth, int screenHeight) {
            // Khởi tạo sao băng ngẫu nhiên từ trên cùng
            this.x = random.nextFloat() * screenWidth;
            this.y = -10; // Bắt đầu từ ngoài màn hình trên
            this.speed = 2 + random.nextFloat() * 4; // Tăng tốc độ tối đa để sao băng di chuyển nhanh hơn
            this.angle = random.nextFloat() * 360; // Góc di chuyển ngẫu nhiên
            this.life = 60 + random.nextInt(40); // Tuổi thọ (frame) của sao băng
        }

        void update(int screenWidth, int screenHeight) {
            // Di chuyển sao băng
            x += MathHelper.cos((float) Math.toRadians(angle)) * speed;
            y += MathHelper.sin((float) Math.toRadians(angle)) * speed;
            life--;

            // Xóa sao băng khi ra khỏi màn hình hoặc hết tuổi thọ
            if (y > screenHeight || life <= 0) {
                meteors.remove(this);
            }
        }

        void render(DrawContext context, int screenWidth, int screenHeight) {
            // Vẽ sao băng như một đường nét sáng, lớn hơn một chút
            float alpha = (float) life / 100; // Mờ dần khi gần hết tuổi thọ
            int color = (int) (255 * alpha) << 24 | 0xFFFFFF; // Màu trắng với độ trong suốt
            context.fill((int) x, (int) y, (int) (x + 4), (int) (y + 10), color); // Tăng kích thước đường nét
        }
    }

    public static void updateAndRender(DrawContext context, int screenWidth, int screenHeight) {
        // Cập nhật và vẽ tất cả sao băng
        for (Meteor meteor : new ArrayList<>(meteors)) {
            meteor.update(screenWidth, screenHeight);
            meteor.render(context, screenWidth, screenHeight);
        }

        // Thêm sao băng mới ngẫu nhiên (tăng tần suất, mỗi 10 frame)
        if (random.nextInt(10) == 0) { // Tăng tần suất sao băng
            meteors.add(new Meteor(screenWidth, screenHeight));
        }

        // Giới hạn số lượng sao băng để tránh quá tải (tối đa 20 sao băng cùng lúc)
        while (meteors.size() > 20) {
            meteors.remove(0); // Xóa sao băng cũ nhất nếu quá nhiều
        }
    }
}
