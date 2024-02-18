package com.vernonlim.chip8;

import java.util.Stack;
import java.lang.Math;

public class Chip8 {
    public byte[] memory = new byte[4096];
    public byte[] registers = new byte[16];
    public Stack<Short> stack = new Stack<Short>();
    public short[] rom;
    public short pc = 0;
    public short ireg = 0;

    // I *think* this is fine due to two's complement
    public static short extract(short code, int start, int end) {
        if (start < 0 || end > 3) {
            throw new IndexOutOfBoundsException();
        }

        short returnValue = 0;
        for (int i = start; i <= end; i++) {
            returnValue <<= 4;
            returnValue += (code >> ((3 - i) * 4)) & 0xF;
        }

        return returnValue;
    }

    public static short extract(short code, int val) {
        return extract(code, val, val);
    }

    public static boolean testCode(short code, int mask, int test) {
        return ((code & mask) == test);
    }

    public void runOpCode(short code) {
        // 00E0 - clear the screen
        if (testCode(code, 0xFFFF, 0x00E0)) {
            System.out.println("TODO: Clear screen");
            return;
        }
        // 00EE - return from subroutine
        if (testCode(code, 0xFFFF, 0x00EE)) {
            pc = stack.pop();
            return;
        }
        // 0NNN - machine code call
        if (testCode(code, 0xF000, 0x0000)) {
            System.out.println("ERROR: Instruction 0NNN not supported");
            return;
        }

        // 1NNN - jump to address in code
        if (testCode(code, 0xF000, 0x1000)) {
            // the PC will be postincremented
            pc = (short) (extract(code, 1, 3) - 1);
            return;
        }

        // 2NNN - call subroutine at address in code 
        if (testCode(code, 0xF000, 0x2000)) {
            stack.push((short)(pc + 1));

            pc = (short) (extract(code, 1, 3) - 1);
            return;
        }

        // 3XNN - if VX == NN skip following instruction
        if (testCode(code, 0xF000, 0x3000)) {
            if (registers[extract(code, 1)] == extract(code, 2, 3)) {
                pc++;
            }
            return;
        }

        // 4XNN - if VX != NN skip following instruction
        if (testCode(code, 0xF000, 0x4000)) {
            if (registers[extract(code, 1)] != extract(code, 2, 3)) {
                pc++;
            }
            return;
        }

        // 5XY0 - skip the following instruction if VX == VY
        if (testCode(code, 0xF000, 0x5000)) {
            if (registers[extract(code, 1)] == registers[extract(code, 2)]) {
                pc++;
            }
            return;
        }

        // 6XNN - store number NN in register VX
        if (testCode(code, 0xF000, 0x6000)) {
            registers[extract(code, 1)] = (byte) extract(code, 2, 3);
            return;
        }

        // 7XNN - add number NN to register VX
        if (testCode(code, 0xF000, 0x7000)) {
            registers[extract(code, 1)] += (byte) extract(code, 2, 3);
            return;
        }

        // 8XY0 - set VX to VY
        if (testCode(code, 0xF00F, 0x8000)) {
            registers[extract(code, 1)] = registers[extract(code, 2)];
            return;
        }

        // 8XY1 - set VX to VX OR VY
        if (testCode(code, 0xF00F, 0x8001)) {
            registers[extract(code, 1)] = (byte) (registers[extract(code, 1)] | registers[extract(code, 2)]);
            return;
        }

        // 8XY2 - set VX to VX AND VY
        if (testCode(code, 0xF00F, 0x8002)) {
            registers[extract(code, 1)] = (byte) (registers[extract(code, 1)] & registers[extract(code, 2)]);
            return;
        }

        // 8XY3 - set VX to VX XOR VY
        if (testCode(code, 0xF00F, 0x8003)) {
            registers[extract(code, 1)] = (byte) (registers[extract(code, 1)] ^ registers[extract(code, 2)]);
            return;
        }

        // 8XY4 - set VX to VX + VY, set VF to 01 if an overflow occurs, 00 if not
        if (testCode(code, 0xF00F, 0x8004)) {
            int vx = registers[extract(code, 1)] & 0xF;
            int vy = registers[extract(code, 2)] & 0xF;

            if (vx + vy > 255) {
                registers[15] = 1;                
            } else {
                registers[15] = 0;
            }

            // hopefully two's complement handles this
            registers[extract(code, 1)] = (byte) (vx + vy);
            return;
        }

        // 8XY5 - set VX to VX - VY, set VF to 00 if an underflow occurs, 01 if not
        if (testCode(code, 0xF00F, 0x8005)) {
            int vx = registers[extract(code, 1)] & 0xF;
            int vy = registers[extract(code, 2)] & 0xF;

            if (vy > vx) {
                registers[15] = 1;                
            } else {
                registers[15] = 0;
            }

            // same here
            registers[extract(code, 1)] = (byte) (vx - vy);
            return;
        }

        // 8XY6 - stores the LSB of VX in VF and then shifts VX to the right by 1
        if (testCode(code, 0xF00F, 0x8006)) {
            registers[15] = (byte) extract((short) registers[extract(code, 1, 1)], 3);
            registers[extract(code, 1, 1)] = (byte) ((registers[extract(code, 1, 1)] >> 1) & 0b01111111);
            return;
        }

        // 8XY7 - set VX to VY - VX. if underflow, VF = 00 and 01 otherwise
        if (testCode(code, 0xF00F, 0x8007)) {
            int vx = registers[extract(code, 1)] & 0xF;
            int vy = registers[extract(code, 2)] & 0xF;

            if (vx > vy) {
                registers[15] = 1;                
            } else {
                registers[15] = 0;
            }

            registers[extract(code, 2)] = (byte) (vy - vx);
            return;
        }

        // 8XYE - set VX
        if (testCode(code, 0xF00F, 0x800E)) {
            registers[15] = (byte) extract((short) registers[extract(code, 1, 1)], 1);
            registers[extract(code, 1, 1)] = (byte) (registers[extract(code, 1, 1)] << 1);
            return;
        }

        // 9XY0 - skip the next instruction if VX != VY
        if (testCode(code, 0xF00F, 0x9000)) {
            if (registers[extract(code, 1)] != registers[extract(code, 2)]) {
                pc++;
            }
            return;
        }

        // ANNN - sets I to the address NNN
        if (testCode(code, 0xF000, 0xA000)) {
            ireg = extract(code, 1, 3);
            return;
        }

        // BNNN - jumps to NNN + V0
        if (testCode(code, 0xF000, 0xB000)) {
            pc = (short) ((extract(code, 1, 3) + (registers[0] & 0xF)) - 1);
            return;
        }

        // CXNN - sets VX to the results of a bitwise AND operation on a random number and NN
        if (testCode(code, 0xF000, 0xC000)) {
            registers[extract(code, 1)] = (byte) ((Math.round(Math.random() * 255)) & extract(code, 2, 3));
            return;
        }        
    
        // DXYN - draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels. 
        // Some other details to be made later
        if (testCode(code, 0xF000, 0xD000)) {
            System.out.println("TODO: Draw sprite");
            return;
        }

        // EX9E - skips the next instruction if the key stored in VX is pressed
        if (testCode(code, 0xF0FF, 0xE09E)) {
            // placeholder
            byte keycode = 10;
            if (registers[extract(code, 1)] == keycode) {
                pc++;
            }
            return;
        }

        // EXA1 - skips the next instruction if the key stored in VX is not pressed
        if (testCode(code, 0xF0FF, 0xE0A1)) {
            // placeholder
            byte keycode = 10;
            if (registers[extract(code, 1)] != keycode) {
                pc++;
            }
            return;
        }

        // FX07 - sets VX to the value of the delay timer
        if (testCode(code, 0xF0FF, 0xF007)) {
            // placeholder
            byte delayTimer = 60;
            registers[extract(code, 1)] = delayTimer;
            return;
        }

        // FX0A - await a key press, then store in VX
        if (testCode(code, 0xF0FF, 0xF00A)) {
            // placeholder
            byte keypress = 10;
            registers[extract(code, 1)] = keypress;
            return;
        }

        // FX15 - sets the delay timer to VX
        if (testCode(code, 0xF0FF, 0xF015)) {
            System.out.println("TODO: Set delay timer to VX");            
            return;
        }

        // FX18 - sets the sound timer to VX
        if (testCode(code, 0xF0FF, 0xF018)) {
            System.out.println("TODO: Set sound timer to VX");            
            return;
        }

        // FX1E - adds VX to I
        if (testCode(code, 0xF0FF, 0xF01E)) {
            ireg += registers[extract(code, 1)];
            return;
        }

        // FX29 - sets I to the location of the sprite for the character in VX
        if (testCode(code, 0xF0FF, 0xF029)) {
            System.out.println("TODO: Whatever the hell this is");
            return;
        }

        // FX33 - stores the BCD representation of VX, with the hundreds digit in memory at the location in I
        // the tens digit at location I+1, and the ones digit at location I+2
        if (testCode(code, 0xF0FF, 0xF033)) {
            System.out.println("TODO: BCD");
            return;
        }

        // FX55 - stores from V0 to VX with values from memory, starting at address I
        if (testCode(code, 0xF0FF, 0xF055)) {
            for (int i = 0; i <= extract(code, 1); i++) {
                memory[ireg + i] = registers[i];
            }
            return;
        }

        // FX65 - fills from V0 to VX with values from memory, starting at address I
        if (testCode(code, 0xF0FF, 0xF065)) {
            for (int i = 0; i <= extract(code, 1); i++) {
                registers[i] = memory[ireg + i];
            }
            return;
        }
    }

    public Chip8(short[] input) {
        rom = input;
    }

    public Chip8() {
        this(new short[] {});
    }
}
