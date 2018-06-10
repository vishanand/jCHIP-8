import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

// Main Interpreter Class for the CHIP-8 VM

public class chip8 {

    /**
     * Initializes the interpreter object
     */
    public chip8() {
        this.mem = new memory();
        this.rand = new Random();
        this.display = new graphics();
        this.redrawFlag = false;
        this.pc = 0x200; //execution starting point
        this.loadFonts();
        this.keypad = new boolean[16];
        this.pressKey(-1);
        //clear registers & stack
        this.dt = 0;
        this.st = 0;
        this.I = 0;
        this.sp = 0;
        this.stack = new int[24];
        this.register = new int[16];

    }

    /**
     * Loads Chip-8 ROM into memory
     *
     * @param filename
     *            path to ROM file
     * @return whether or not the load operation succeeded
     */
    public boolean loadProgram(String filename) {
        try {
            InputStream inputStream = new FileInputStream(filename);

            //read file byte by byte
            int lastByte;
            int memLoc = 0x200; //start loading at 0x200
            while ((lastByte = inputStream.read()) != -1) {
                this.mem.setByte(memLoc, lastByte);
                memLoc++;
            }

            inputStream.close();

        } catch (FileNotFoundException e) {
            System.out.println("File '" + filename + "' not found!");
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Loads hex fonts into the memory space from 0x00 to 0x50
     */
    private void loadFonts() {
        final int fontSet[] = { 0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80 }; // F

        for (int i = 0; i < 0x50; i++) {
            this.mem.setByte(i, fontSet[i]);
        }
    }

    /**
     * Runs a single cycle of the CHIP-8 VM (Fetch-Decode-Execute)
     */
    public void runCycle() {
        // Fetch both bytes and merge them into a single int
        int byte1 = this.mem.getByte(this.pc);
        byte1 = byte1 << 8;
        int byte2 = this.mem.getByte(this.pc + 1);
        int opcode = byte1 | byte2;

        /*
         * if (this.dt == 1) { for (int i = 0; i < this.register.length; i++) {
         * System.out.print(Integer.toHexString(this.register[i]) + " "); }
         * System.out.print(Integer.toHexString(this.I) + " \n\n"); }
         */

        // Decode & Execute
        this.decodeAndExecute(opcode);

        // Decrement Timers
        if (this.dt > 0) {
            this.dt--;
        }
        if (this.st > 0) {
            this.st--;
        }

    }

    private void decodeAndExecute(int opcode) {
        // Decode & Execute

        int address = opcode & 0x0FFF; //last three nibbles
        int lastNibble = opcode & 0x000F;
        int KK = opcode & 0x00FF; // last byte
        int X = (opcode & 0x0F00) >> 8; // second nibble
        int Y = (opcode & 0x00F0) >> 4; // third nibble
        this.redrawFlag = false;

        switch ((opcode & 0xF000) >> 12) { // switch off of first nibble
            // 0NNN: SYS addr
            case 0x0: // jump to machine code
                if (opcode == 0xE0) { // 00E0 CLS
                    this.redrawFlag = true;
                    this.display.cls();
                } else if (opcode == 0xEE) { //00EE RET
                    this.pc = this.stack[this.sp];
                    this.sp--;
                } else {
                    System.out.println(
                            "0NNN encountered! Cannot jump to SYS instruction!  "
                                    + Integer.toHexString(address));
                }
                this.pc += 2;
                return;

            // 1NNN: JP addr
            case 0x1: // jump to addr by setting PC
                this.pc = address;
                return;

            // 2NNN: CALL addr
            case 0x2: //call subroutine
                this.sp++;
                this.stack[this.sp] = this.pc;
                this.pc = address;
                return;

            // ANNN: LD I, addr
            case 0xA: // set I to address
                this.I = address;
                this.pc += 2;
                return;

            // BNNN: JP V0, addr
            case 0xB: // jump to address + V0
                this.pc = address + this.register[0];
                System.out.println("BNNN " + Integer.toHexString(address));
                return;

            // 3XKK: SE Vx, byte
            case 0x3: // skip instruction if Vx = KK
                if (KK == this.register[X]) {
                    this.pc += 2;
                }
                this.pc += 2;
                return;

            // 4XKK: SNE Vx, byte
            case 0x4: // skip instruction if Vx != KK
                if (KK != this.register[X]) {
                    this.pc += 2;
                }
                this.pc += 2;
                return;

            // 5XY0: SE Vx, Vy
            case 0x5: // skip instruction if Vx == Vy
                if (lastNibble == 0x0) { // check if last nibble is zero
                    if (this.register[X] == this.register[Y]) {
                        this.pc += 2;
                    }
                    this.pc += 2;
                    return;
                }
                break;

            // 6XKK: LD Vx, byte
            case 0x6: // load KK into Vx
                this.register[X] = KK;
                this.pc += 2;
                return;

            // 7XKK: ADD Vx, byte
            case 0x7: // add KK into Vx
                this.register[X] += KK;
                this.register[X] &= 0xFF; //limit to 8 bits
                this.pc += 2;
                return;

            // 8XY*
            case 0x8:
                this.register[X] &= 0xFF; //limit to 8 bits
                this.register[Y] &= 0xFF; //limit to 8 bits
                switch (lastNibble) {
                    // 8XY0: LD Vx, Vy
                    case 0x0: // Vx = Vy
                        this.register[X] = this.register[Y];
                        this.pc += 2;
                        return;

                    // 8XY1: OR Vx, Vy
                    case 0x1: // Vx = Vx | Vy
                        this.register[X] = this.register[X] | this.register[Y];
                        this.pc += 2;
                        return;

                    // 8XY2: AND Vx, Vy
                    case 0x2: // Vx = Vx & Vy
                        this.register[X] = this.register[X] & this.register[Y];
                        this.pc += 2;
                        return;

                    // 8XY3: XOR Vx, Vy
                    case 0x3: // Vx = Vx ^ Vy
                        this.register[X] = this.register[X] ^ this.register[Y];
                        this.pc += 2;
                        return;

                    // 8XY4: ADD Vx, Vy
                    case 0x4: // Vx = Vx + Vy, set VF to carry
                        int result = this.register[X] + this.register[Y];
                        if (result > 255) {
                            this.register[0xF] = 1;
                        } else {
                            this.register[0xF] = 0;
                        }
                        this.register[X] = result & 0xFF;

                        this.pc += 2;
                        return;

                    // 8XY5: SUB Vx, Vy
                    case 0x5: // Vx = Vx - Vy, set flag if Vx > Vy
                        if (this.register[X] > this.register[Y]) {
                            this.register[0xF] = 1;
                        } else {
                            this.register[0xF] = 0;
                        }
                        this.register[X] = this.register[X] - this.register[Y];
                        if (this.register[X] < 0) { //no negatives allowed!
                            this.register[X] += 256;
                        }
                        this.pc += 2;
                        return;

                    // TODO: 8XY6 & 8XYE seem to have multiple meanings based on different interpreters
                    // 8XY6: SHR Vx {, Vy}
                    case 0x6: // Vx = Vy >> 1, set flag to least sig bit of Vx beforehand
                        this.register[0xF] = this.register[X] & 1;
                        this.register[X] = this.register[X] >> 1;
                        this.pc += 2;
                        return;

                    // 8XY7: SUBN Vx, Vy
                    case 0x7: // Vx = Vy - Vx, set flag if Vy > Vx
                        if (this.register[X] < this.register[Y]) {
                            this.register[0xF] = 1;
                        } else {
                            this.register[0xF] = 0;
                        }
                        this.register[X] = this.register[Y] - this.register[X];
                        if (this.register[X] < 0) { //no negatives allowed!
                            this.register[X] += 256;
                        }
                        this.pc += 2;
                        return;

                    // 8XYE: SHL Vx {, Vy}
                    case 0xE: // Vx = Vy << 1, set flag to most sig bit of Vx beforehand
                        this.register[0xF] = (this.register[X] & 0x80) >> 7;
                        this.register[X] = this.register[X] << 1;
                        this.pc += 2;
                        return;
                }
                break;

            // 9XY0: SNE Vx, Vy
            case 0x9: // skip instruction if Vx != Vy
                if (lastNibble == 0x0) {
                    if (this.register[X] != this.register[Y]) {
                        this.pc += 2;
                    }
                    this.pc += 2;
                    return;
                }
                return;

            // CXKK: RND Vx, byte
            case 0xC: // Vx = RND & KK
                this.register[X] = this.rand.nextInt(256) & KK;
                this.pc += 2;
                return;

            // DXYN: DRW Vx, Vy, Nibble
            case 0xD: // display
                int spriteAddress = this.I;
                boolean setFlag = false;
                for (int i = 0; i < lastNibble; i++) {
                    setFlag |= this.display.drawSprite(
                            this.mem.getByte(spriteAddress), this.register[X],
                            this.register[Y] + i);
                    spriteAddress++;
                }
                this.register[0xF] = 0;
                if (setFlag) {
                    this.register[0xF] = 1;
                }

                this.redrawFlag = true;
                this.pc += 2;
                return;

            // EX9E & EXA1
            case 0xE:
                // EX9E: SKP Vx
                if (KK == 0x9E) {
                    // skip next instruction if key stored in Vx is pressed
                    if (this.keypad[this.register[X]]) {
                        this.pc += 2;
                    }
                    this.pc += 2;
                    return;
                }

                // EXA1: SKNP Vx
                else if (KK == 0xA1) {
                    // skip next instruction if key stored in Vx is not pressed
                    if (!this.keypad[this.register[X]]) {
                        this.pc += 2;
                    }
                    this.pc += 2;
                    return;
                }
                break;

            // FX** instructions
            case 0xF:
                switch (KK) {
                    // FX07: LD Vx, DT
                    case 0x07:
                        this.register[X] = this.dt; // store value of x register into delay timer
                        this.pc += 2;
                        return;

                    // FX0A: LD Vx, K
                    case 0x0A: // wait for keypress then store value to Vx
                        int key = -1;
                        for (int i = 0; i <= 0xF; i++) {
                            if (this.keypad[i]) {
                                key = i;
                            }
                        }
                        if (key != -1) {
                            this.register[X] = key;
                            this.pc += 2; // only go to next instruction if key is pressed
                        }
                        return;

                    // FX15: LD DT, Vx
                    case 0x15: // set delay timer to value of Vx
                        this.dt = this.register[X];
                        this.pc += 2;
                        return;

                    // FX18: LD ST, Vx
                    case 0x18: // set sound timer to value of Vx
                        this.st = this.register[X];
                        this.pc += 2;
                        return;

                    // FX1E: ADD I, Vx
                    case 0x1E: // I += Vx
                        this.I += this.register[X];
                        this.pc += 2;
                        return;

                    // FX29: LD F, Vx
                    case 0x29: // set I to location of font for hex digit Vx
                        this.I = this.register[X] * 5;
                        this.pc += 2;
                        return;

                    // FX33: LD B, Vx
                    case 0x33:
                        // Store BCD representation of Vx in memory locations I, I+1, and I+2.
                        // this BCD conversion formula was taken from TJA
                        this.mem.setByte(this.I, this.register[X] / 100);
                        this.mem.setByte(this.I + 1,
                                (this.register[X] / 10) % 10);
                        this.mem.setByte(this.I + 2,
                                (this.register[X] % 100) % 10);
                        this.pc += 2;
                        return;

                    // FX55: LD [I], Vx
                    case 0x55: // copies registers V0 to Vx to memory starting at I
                        for (int i = 0; i <= X; i++) {
                            this.mem.setByte(this.I, this.register[i]);
                            this.I++;
                        }
                        this.I -= X + 1; // TODO: figure out if I is supposed to change or not in FX55 and FX65
                        this.pc += 2;
                        return;

                    // Fx65: LD Vx, [I]
                    case 0x65: // reads registers V0 to Vx from memory starting at I
                        for (int i = 0; i <= X; i++) {
                            this.register[i] = this.mem.getByte(this.I);
                            this.I++;
                        }
                        this.I -= X + 1;
                        this.pc += 2;
                        return;
                }
        }
        System.out.println("Unknown Upcode: " + Integer.toHexString(opcode));
        this.pc += 2;

    }

    public boolean shouldWeRedraw() {
        return this.redrawFlag;
    }

    public boolean shouldSoundPlay() {
        return this.st > 0;
    }

    /**
     * Press a key on the hexadecimal input keypad
     *
     * @param key
     *            Which Key to press (0-0xF), an invalid choice is interpreted
     *            as no key being pressed
     */
    public void pressKey(int key) {
        if (key == -1) {
            for (int i = 0; i <= 0xF; i++) { // clear all other keys
                this.keypad[i] = false;
            }
        }
        if (key >= 0 && key <= 0xF) {
            this.keypad[key] = true;
        }
    }

    /**
     * Release a key on the hexadecimal input keypad
     *
     * @param key
     *            Which key to release (0-0xF)
     */
    public void releaseKey(int key) {
        if (key >= 0 && key <= 0xF) {
            this.keypad[key] = false;
        }
    }

    public String getCPUInfo() {
        String cpuInfo = String.format(
                "<html><pre>OPCODE=%02X%02X, PC=%03X, I=%04X, SP=%02X, V0=%02X, V1=%02X, VF=%02X, ST=%02X, DT=%02X</pre></html>",
                this.mem.getByte(this.pc), this.mem.getByte(this.pc + 1),
                this.pc, this.I, this.sp, this.register[0x0],
                this.register[0x1], this.register[0xF], this.st, this.dt);
        return cpuInfo;
    }

    public memory mem; // 4KB of working RAM
    private Random rand;
    public graphics display; //64x32 monochrome display
    private boolean redrawFlag;
    private boolean keypad[]; //hexadecimal input keypad

    private int stack[]; // Call stack
    private int register[]; // V registers
    private int sp; // Stack Pointer
    private int I; // Index register
    private int pc; // Program Counter
    private int dt; // Delay Timer
    private int st; // Sound Timer

}
