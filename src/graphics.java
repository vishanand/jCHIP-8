
public class graphics {
    public graphics() {
        this.pixels = new boolean[64][32];
        this.cls();
    }

    public void cls() {
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 32; j++) {
                this.pixels[i][j] = false;
            }
        }
    }

    public boolean drawSprite(int spriteData, int x, int y) {
        boolean erased = false;

        for (int i = 7; i >= 0; i--) {
            if (((spriteData >> i) & 1) == 1) {
                erased |= this.drawPixel(7 - i + x, y);
            }
        }

        return erased;
    }

    public boolean drawPixel(int x, int y) {
        if (x < 0 || x > 63 || y < 0 || y > 31) { // don't draw if off screen
            System.out.println("x=" + x + " y=" + y);
            return false;
        }
        boolean erased = this.pixels[x][y];
        this.pixels[x][y] ^= true; //flip bit
        return erased;
    }

    public boolean getPixel(int x, int y) {
        return this.pixels[x][y];
    }

    private boolean pixels[][];
}
