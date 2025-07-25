/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.utils.network;

import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class PacketUtilsUtil {
    private static final String packetRegistryClass = """
    private static class PacketRegistry extends SimpleRegistry<Class<? extends Packet<?>>> {
        public PacketRegistry() {
            super(RegistryKey.ofRegistry(MeteorVNClient.identifier("packets")), Lifecycle.stable());
        }

        @Override
        public int size() {
            return S2C_PACKETS.keySet().size() + C2S_PACKETS.keySet().size();
        }

        @Override
        public Identifier getId(Class<? extends Packet<?>> entry) {
            return null;
        }

        @Override
        public Optional<RegistryKey<Class<? extends Packet<?>>>> getKey(Class<? extends Packet<?>> entry) {
            return Optional.empty();
        }

        @Override
        public int getRawId(Class<? extends Packet<?>> entry) {
            return 0;
        }

        @Override
        public Class<? extends Packet<?>> get(RegistryKey<Class<? extends Packet<?>>> key) {
            return null;
        }

        @Override
        public Class<? extends Packet<?>> get(Identifier id) {
            return null;
        }

        @Override
        public Lifecycle getLifecycle() {
            return null;
        }

        @Override
        public Set<Identifier> getIds() {
            return Collections.emptySet();
        }

        @Override
        public boolean containsId(Identifier id) {
            return false;
        }

        @Override
        public Class<? extends Packet<?>> get(int index) {
            return null;
        }

        @NotNull
        @Override
        public Iterator<Class<? extends Packet<?>>> iterator() {
            return Stream.concat(S2C_PACKETS.keySet().stream(), C2S_PACKETS.keySet().stream()).iterator();
        }

        @Override
        public boolean contains(RegistryKey<Class<? extends Packet<?>>> key) {
            return false;
        }

        @Override
        public Set<Map.Entry<RegistryKey<Class<? extends Packet<?>>>, Class<? extends Packet<?>>>> getEntrySet() {
            return Collections.emptySet();
        }

        @Override
        public Optional<RegistryEntry.Reference<Class<? extends Packet<?>>>> getRandom(Random random) {
            return Optional.empty();
        }

        @Override
        public Registry<Class<? extends Packet<?>>> freeze() {
            return null;
        }

        @Override
        public RegistryEntry.Reference<Class<? extends Packet<?>>> createEntry(Class<? extends Packet<?>> value) {
            return null;
        }

        @Override
        public Optional<RegistryEntry.Reference<Class<? extends Packet<?>>>> getEntry(int rawId) {
            return Optional.empty();
        }

        @Override
        public Stream<RegistryEntry.Reference<Class<? extends Packet<?>>>> streamEntries() {
            return null;
        }

        @Override
        public Set<RegistryKey<Class<? extends Packet<?>>>> getKeys() {
            return Collections.emptySet();
        }
    }
""";

    private PacketUtilsUtil() {
    }

    public static void main(String[] args) {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() throws IOException {
        // Generate PacketUtils.java
        File file = new File("src/main/java/%s/PacketUtils.java".formatted(PacketUtilsUtil.class.getPackageName().replace('.', '/')));
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("/*\n");
            writer.write(" * Tệp này là một phần của bản phân phối MeteorVN Client (https://github.com/hongminh54/mteorvn-client).\n");
            writer.write(" * Bản quyền thuộc bởi (c) MeteorVN Development.\n");
            writer.write(" */\n\n");

            writer.write("package meteordevelopment.meteorvnclient.utils.network;\n\n");

            //   Write imports
            writer.write("import com.mojang.serialization.Lifecycle;\n");
            writer.write("import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;\n");
            writer.write("import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;\n");
            writer.write("import meteordevelopment.meteorvnclient.MeteorVNClient;\n");
            writer.write("import net.minecraft.network.packet.Packet;\n");
            writer.write("import net.minecraft.registry.Registry;\n");
            writer.write("import net.minecraft.registry.RegistryKey;\n");
            writer.write("import net.minecraft.registry.SimpleRegistry;\n");
            writer.write("import net.minecraft.registry.entry.RegistryEntry;\n");
            writer.write("import net.minecraft.util.Identifier;\n");
            writer.write("import net.minecraft.util.math.random.Random;\n");
            writer.write("import org.jetbrains.annotations.NotNull;\n\n");

            writer.write("import java.util.*;\n");
            writer.write("import java.util.stream.Stream;\n");

            //   Write class
            writer.write("\npublic class PacketUtils {\n");

            //     Write fields
            writer.write("    public static final Registry<Class<? extends Packet<?>>> REGISTRY = new PacketRegistry();\n\n");
            writer.write("    private static final Map<Class<? extends Packet<?>>, String> S2C_PACKETS = new Reference2ObjectOpenHashMap<>();\n");
            writer.write("    private static final Map<Class<? extends Packet<?>>, String> C2S_PACKETS = new Reference2ObjectOpenHashMap<>();\n\n");
            writer.write("    private static final Map<String, Class<? extends Packet<?>>> S2C_PACKETS_R = new Object2ReferenceOpenHashMap<>();\n");
            writer.write("    private static final Map<String, Class<? extends Packet<?>>> C2S_PACKETS_R = new Object2ReferenceOpenHashMap<>();\n\n");

            //     Write static block
            writer.write("    static {\n");

            Comparator<Class<?>> packetsComparator = Comparator
                .comparing((Class<?> cls) -> cls.getName().substring(cls.getName().lastIndexOf('.') + 1))
                .thenComparing(Class::getName);

            // Client -> Sever Packets
            Reflections c2s = new Reflections("net.minecraft.network.packet.c2s", Scanners.SubTypes);
            Set<Class<? extends Packet>> c2sPackets = c2s.getSubTypesOf(Packet.class);
            SortedSet<Class<? extends Packet>> sortedC2SPackets = new TreeSet<>(packetsComparator);
            sortedC2SPackets.addAll(c2sPackets);

            for (Class<? extends Packet> c2sPacket : sortedC2SPackets) {
                String name = c2sPacket.getName();
                String className = name.substring(name.lastIndexOf('.') + 1).replace('$', '.');
                String fullName = name.replace('$', '.');

                writer.write("        C2S_PACKETS.put(%s.class, \"%s\");%n".formatted(fullName, className));
                writer.write("        C2S_PACKETS_R.put(\"%s\", %s.class);%n".formatted(className, fullName));
            }

            writer.newLine();

            // Server -> Client Packets
            Reflections s2c = new Reflections("net.minecraft.network.packet.s2c", Scanners.SubTypes);
            Set<Class<? extends Packet>> s2cPackets = s2c.getSubTypesOf(Packet.class);
            SortedSet<Class<? extends Packet>> sortedS2CPackets = new TreeSet<>(packetsComparator);
            sortedS2CPackets.addAll(s2cPackets);

            for (Class<? extends Packet> s2cPacket : sortedS2CPackets) {
                if (s2cPacket == BundlePacket.class || s2cPacket == BundleSplitterPacket.class) continue;
                String name = s2cPacket.getName();
                String className = name.substring(name.lastIndexOf('.') + 1).replace('$', '.');
                String fullName = name.replace('$', '.');

                writer.write("        S2C_PACKETS.put(%s.class, \"%s\");%n".formatted(fullName, className));
                writer.write("        S2C_PACKETS_R.put(\"%s\", %s.class);%n".formatted(className, fullName));
            }

            writer.write("    }\n\n");

            writer.write("    private PacketUtils() {\n");
            writer.write("    }\n\n");

            //     Write getName method
            writer.write("    public static String getName(Class<? extends Packet<?>> packetClass) {\n");
            writer.write("        String name = S2C_PACKETS.get(packetClass);\n");
            writer.write("        if (name != null) return name;\n");
            writer.write("        return C2S_PACKETS.get(packetClass);\n");
            writer.write("    }\n\n");

            //     Write getPacket method
            writer.write("    public static Class<? extends Packet<?>> getPacket(String name) {\n");
            writer.write("        Class<? extends Packet<?>> packet = S2C_PACKETS_R.get(name);\n");
            writer.write("        if (packet != null) return packet;\n");
            writer.write("        return C2S_PACKETS_R.get(name);\n");
            writer.write("    }\n\n");

            //     Write getS2CPackets method
            writer.write("    public static Set<Class<? extends Packet<?>>> getS2CPackets() {\n");
            writer.write("        return S2C_PACKETS.keySet();\n");
            writer.write("    }\n\n");

            //     Write getC2SPackets method
            writer.write("    public static Set<Class<? extends Packet<?>>> getC2SPackets() {\n");
            writer.write("        return C2S_PACKETS.keySet();\n");
            writer.write("    }\n\n");

            // Write PacketRegistry class
            writer.write(packetRegistryClass);

            //   Write end class
            writer.write("}\n");
        }
    }
}
