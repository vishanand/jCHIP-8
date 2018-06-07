// Class to handle the CHIP-8 System Memory

public class memory {

    public memory() {
        this.RAM = new int[4096]; // initialize 4KB of memory
    }

    public void setByte(int addr, int value) {
        if (addr >= 0x000 && addr <= 0xFFF) { //check for bounds
            this.RAM[addr] = value & 0xFF; //flag off all bits outside of last 8
            return;
        }
        throw new java.lang.Error(
                "setByte() tried to access memory outside of bounds!");
    }

    public int getByte(int addr) {
        if (addr >= 0x000 && addr <= 0xFFF) { //check for bounds
            return this.RAM[addr];
        }
        throw new java.lang.Error(
                "getByte() tried to access memory outside of bounds!");
    }

    private int RAM[]; // System memory
}
