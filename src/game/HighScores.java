package game;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;

public class HighScores {
    private static final String FILE =
            System.getProperty("user.home") + "/highscores.txt";



    public static void save(String name, int score) {
        try {
            File f = new File(FILE);

            // Fix: avoid null parent folder crash
            File parent = f.getParentFile();
            if (parent != null) parent.mkdirs();

            try (PrintWriter pw = new PrintWriter(new FileWriter(f, true))) {
                pw.println(name + "," + score);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static java.util.List<String> loadTop() {
        java.util.List<String> out = new ArrayList<>();
        try {
            File f = new File(FILE);
            if (!f.exists()) return out;

            java.util.List<String> lines = Files.readAllLines(f.toPath());

            // فلتر السطور غير الصالحة ثم رتب تنازليًا حسب السكور
            java.util.List<String> valid = new ArrayList<>();
            for (String line : lines) {
                if (line == null) continue;
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String scorePart = parts[1].trim();
                try {
                    Integer.parseInt(scorePart); // فقط للتأكد
                    valid.add(line);
                } catch (NumberFormatException ex) {
                    // تجاهل السطر إذا السكور مش عدد صحيح
                }
            }

            valid.sort((a, b) -> {
                int sa = Integer.parseInt(a.split(",")[1].trim());
                int sb = Integer.parseInt(b.split(",")[1].trim());
                return Integer.compare(sb, sa); // تنازلي
            });

            int limit = Math.min(10, valid.size());
            for (int i = 0; i < limit; i++) out.add(valid.get(i));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }
}
