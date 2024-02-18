package com.vernonlim.chip8;

import java.io.*;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws Exception {
        File file = new File("./test.txt");

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String st;
        
        while ((st = reader.readLine()) != null) {
            System.out.println(st);
        }

        reader.close();

        byte[] test = Files.readAllBytes(file.toPath());
        System.out.println(test);

        Chip8 chip = new Chip8(new short[] {0x1234, 0x2AB5, 0x0B82, 0x2983});
        System.out.printf("%X\n", Chip8.extract((short) 0xEBCD, 0, 1));
        System.out.printf("%X\n", Chip8.extract((short) 0xEBCD, 0, 2));
        System.out.printf("%X\n", Chip8.extract((short) 0xEBCD, 0, 3));
        System.out.printf("%X\n", Chip8.extract((short) 0xEBCD, 1, 1));
        System.out.printf("%X\n", Chip8.extract((short) 0xEBCD, 1, 2));
        System.out.printf("%X\n", Chip8.extract((short) 0xEBCD, 1, 3));
        System.out.printf("%X\n", Chip8.extract((short) 0xEBCD, 3));

        System.out.println(chip.testCode((short) 0xF289, 0xF00F, 0xF009));
    }
}