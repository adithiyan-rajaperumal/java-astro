package org.vedic.astro;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import de.thmac.swisseph.SweConst;
public class SweConstDumpTest {
    @Test
    public void dump() {
        for (Field f : SweConst.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
                try {
                    System.out.println(f.getName() + " = " + f.get(null));
                } catch (Exception e) {}
            }
        }
    }
}
