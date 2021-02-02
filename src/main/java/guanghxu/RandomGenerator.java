package guanghxu;

import java.util.Random;

public class RandomGenerator {
    public static int getId() {
        return new Random().nextInt(10000);
    }

    public static String getName() {
        return "Name" + getId();
    }
}
