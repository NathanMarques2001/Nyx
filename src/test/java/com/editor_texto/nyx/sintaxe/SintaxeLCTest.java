package com.editor_texto.nyx.sintaxe;

import com.editor_texto.nyx.ui.sintaxe.SintaxeLC;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SintaxeLCTest {

    @Test
    public void testCalcularRealceComStringVazia() {
        assertDoesNotThrow(() -> {
            SintaxeLC.calcularRealce("");
        });
    }

    @Test
    public void testCalcularRealceComStringNula() {
        assertDoesNotThrow(() -> {
            SintaxeLC.calcularRealce(null);
        });
    }
}
