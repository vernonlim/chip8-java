package com.vernonlim.chip8;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class Chip8Tests {
    Chip8 chip;

    @BeforeEach
    void init() {
        chip = new Chip8();
    }


    void opCodeRegTest(int opcode, int reg, int val) {
        chip.runOpCode((short) opcode);
        assertEquals(chip.registers[(byte) reg], (byte) val);
    }

    @Test
    @DisplayName("Basic Test")
    void addsTwoNumbers() {
        int test = 2 + 3;
        int test2 = 3 + 2;
        assertEquals(test, test2, "associativity");
    }

    // @ParameterizedTest(name = "{0} + {1} = {2}")
	// @CsvSource({
	// 		"0,    1,   1",
	// 		"1,    2,   3",
	// 		"49,  51, 100",
	// 		"1,  100, 101"
    // })

    @Test
    @DisplayName("6XNN Test")
    void test6XNN() {
        opCodeRegTest(0x62FF, 2, 0xFF);
    }

    @Test
    @DisplayName("7XNN Test") 
    void test7XNN() {
        opCodeRegTest(0x7211, 2, 0x11);
        opCodeRegTest(0x7211, 2, 0x22);
        opCodeRegTest(0x73FF, 3, 0xFF);
        System.out.printf("%X\n", chip.registers[3]);
        opCodeRegTest(0x7311, 3, 0x10);
    }













}
