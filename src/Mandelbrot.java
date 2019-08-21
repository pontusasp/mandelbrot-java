import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class Mandelbrot implements MouseListener, KeyListener {

    private BufferedImage image, screen, temp;
    public static double aimX = -1.9;
    public static double aimY = 0.258;
    //public static double aimX = -1.8999977308451679;
    //public static double aimY = 0.253;
    public static double zoomX = 1.1;
    public static double zoomY = 1.05;
    public static double zoomDepth = 1.00957;

    private static boolean pause = true;
    private static boolean pauseX, pauseY, pauseZ;
    private static boolean zoomOut = false;

    public void draw(Graphics g) {
        temp = screen;
        screen = image;
        image = temp;
        g.drawImage(image, 0, 0, null);
    }

    public Mandelbrot() {
        image = new BufferedImage(winWidth, winHeight, BufferedImage.TYPE_INT_RGB);
        screen = new BufferedImage(winWidth, winHeight, BufferedImage.TYPE_INT_RGB);

        JFrame frame = new JFrame("Mandelbrot reella tal");
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                for (double x = minX; x < maxX; x += stepX) {
                    for (double y = minY; y < maxY; y += stepY) {
                        drawPoint(image, x, y);
                    }
                }
                zoomTo(aimX, aimY, zoomX, zoomY);
                zoomDepth(zoomDepth);
                draw(g);
                repaint();
            }
        };

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        panel.addMouseListener(this);
        frame.addKeyListener(this);

        panel.setPreferredSize(new Dimension(winWidth, winHeight));
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }



    public static int winWidth = 1000;
    public static int winHeight = 1000;
    public static double minX = -2, maxX = 2;
    public static double minY = -2, maxY = 2;

    private static double lenX = maxX - minX;
    private static double lenY = maxY - minY;
    private static double stepX = lenX / winWidth;
    private static double stepY = lenY / winHeight;

    private static double depth = 30;
    private static double zoom = -20;

    private static int maxColor = (int) (0xFF / depth);

    public static void drawPoint(BufferedImage img, double x, double y) {
        int rx = toPixelX(x);
        int ry = toPixelY(y);

        if(pause && (rx == toPixelX(aimX) || ry == toPixelY(aimY))) {
            img.setRGB(rx, ry, 0xFFFFFF);
            return;
        }

        double a = 0, b = 0;
        int n = 0;
        while (a * a + b * b < 4 && n < depth + (int) zoom) {
            a = a * a + x;
            b = b * b + y;
            n++;
        }
        try {
            img.setRGB(rx, ry, ((int)(max(n - (int) zoom, 0) * maxColor / 1.5) << 16)
                    | ((max(n - (int) zoom, 0) * maxColor / 2) << 8)
                    | (max(n - (int) zoom, 0) * maxColor));
            //img.setRGB(toPixelX(x), toPixelY(y), 0xFFFFFF);
        } catch(ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println(x + ":" + y);
            System.out.println(toPixelX(x) + ":" + toPixelY(y));
            System.exit(-1);
        }
    }

    private static int max(double a, double b) {
        if(a > b) return (int) a;
        else return (int) b;
    }
    private static int min(double a, double b) {
        if(a < b) return (int) a;
        else return (int) b;
    }

    private static int toPixelX(double x) {
        int rx = (int) (((x - minX) / lenX) * winWidth);
        if(rx >= winWidth)
            return winWidth - 1;
        else return rx;
    }

    private static int toPixelY(double y) {
        int ry = (int) (((y - minY) / lenY) * winHeight);
        if(ry >= winHeight)
            return winHeight - 1;
        else return ry;
    }

    public static void main(String[] args) {
        new Mandelbrot();
    }

    public static void zoomTo(double x, double y, double scaleX, double scaleY) {
        if(!zoomOut) {
            scaleX = 1 / scaleX;
            scaleY = 1 / scaleY;
        }
        double xl = (x - minX) * scaleX;
        double xr = (maxX - x) * scaleX;
        double yt = (y - minY) * scaleY;
        double yb = (maxY - y) * scaleY;
        setLimits(x - xl, x + xr, y - yt, y + yb, depth);
    }

    public static void zoomDepth(double scale) {
        if(pause || pauseZ) return;
        if(zoomOut) scale = 1/scale;
        zoom = (zoom + depth) * scale - depth;
    }

    public static void setLimits(double minX, double maxX, double minY, double maxY, double depth) {
        if(pause) return;
        if(!pauseX) {
            Mandelbrot.minX = minX;
            Mandelbrot.maxX = maxX;
        }
        if(!pauseY) {
            Mandelbrot.minY = minY;
            Mandelbrot.maxY = maxY;
        }
        lenX = maxX - minX;
        lenY = maxY - minY;
        stepX = lenX / winWidth;
        stepY = lenY / winHeight;
        Mandelbrot.depth = depth;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            double scaleX = e.getX() / (double) winWidth;
            double scaleY = e.getY() / (double) winHeight;
            aimX = lenX * scaleX + minX;
            aimY = lenY * scaleY + minY;
            //System.out.println("AIM (" + aimX + ", " + aimY + ")");
        } else {
            zoomOut = !zoomOut;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyChar()) {
            case 'z':
                pauseZ = !pauseZ;
                break;
            case 'x':
                pauseX = !pauseX;
                break;
            case 'c':
                pauseY = !pauseY;
                break;
            case ' ':
                pause = !pause;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}


// Lars Erik