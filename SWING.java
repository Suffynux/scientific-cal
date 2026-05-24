import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SWING {

    // ── state ──────────────────────────────────────────────────────────────
    static boolean isDeg   = true;
    static boolean isInv   = false;
    static double  ans     = 0;
    static boolean justEvaled = false;

    static JLabel  ansLabel    = new JLabel("", SwingConstants.RIGHT);
    static JLabel  display     = new JLabel("0", SwingConstants.RIGHT);
    static StringBuilder expr  = new StringBuilder();

    // button references we need to relabel on Inv toggle
    static JButton btnSin, btnCos, btnTan, btnLn, btnLog, btnSqrt, btnPow, btnDeg, btnRad;

    // ── colours ────────────────────────────────────────────────────────────
    static final Color BG       = new Color(235, 240, 255);
    static final Color BTN_NORM = new Color(255, 255, 255);
    static final Color BTN_FUNC = new Color(220, 228, 255);
    static final Color BTN_EQ   = new Color(66, 103, 212);
    static final Color BTN_MODE = new Color(200, 210, 245);
    static final Color TXT_DARK = new Color(30, 30, 60);
    static final Color TXT_BLUE = new Color(66, 103, 212);
    static final Color TXT_EQ   = Color.WHITE;

    // ── expression evaluator ───────────────────────────────────────────────
    static int pos;
    static String src;

    static double eval(String s) {
        src = s.trim(); pos = 0;
        double v = parseExpr();
        if (pos != src.length()) throw new RuntimeException("Unexpected char");
        return v;
    }

    static double parseExpr() {
        double v = parseTerm();
        while (pos < src.length() && (src.charAt(pos)=='+' || src.charAt(pos)=='-')) {
            char op = src.charAt(pos++);
            double r = parseTerm();
            v = op=='+' ? v+r : v-r;
        }
        return v;
    }

    static double parseTerm() {
        double v = parsePow();
        while (pos < src.length() && (src.charAt(pos)=='*' || src.charAt(pos)=='/' || src.charAt(pos)=='%')) {
            char op = src.charAt(pos++);
            double r = parsePow();
            if (op=='*') v*=r;
            else if (op=='/') { if(r==0) throw new RuntimeException("÷0"); v/=r; }
            else v = v % r;
        }
        return v;
    }

    static double parsePow() {
        double base = parsePostfix();
        if (pos < src.length() && src.charAt(pos)=='^') { pos++; return Math.pow(base, parseUnary()); }
        return base;
    }

    static double parsePostfix() {
        double v = parseUnary();
        while (pos < src.length() && src.charAt(pos)=='!') { pos++; v = factorial(v); }
        return v;
    }

    static double parseUnary() {
        skipWS();
        if (pos < src.length() && src.charAt(pos)=='-') { pos++; return -parseUnary(); }
        if (pos < src.length() && src.charAt(pos)=='+') { pos++; return  parseUnary(); }
        return parsePrimary();
    }

    static double parsePrimary() {
        skipWS();
        if (pos >= src.length()) throw new RuntimeException("Unexpected end");
        char c = src.charAt(pos);

        if (c == '(') {
            pos++;
            double v = parseExpr();
            skipWS();
            if (pos >= src.length() || src.charAt(pos)!=')') throw new RuntimeException("Missing )");
            pos++;
            return v;
        }

        if (Character.isDigit(c) || c=='.') return parseNum();

        if (Character.isLetter(c)) {
            String id = parseIdent();
            if (id.equals("pi") || id.equals("PI")) return Math.PI;
            if (id.equals("e"))  return Math.E;
            if (id.equals("ans") || id.equals("Ans")) return ans;

            skipWS();
            if (pos < src.length() && src.charAt(pos)=='(') {
                pos++;
                double arg = parseExpr();
                skipWS();
                if (pos >= src.length() || src.charAt(pos)!=')') throw new RuntimeException("Missing )");
                pos++;
                return applyFunc(id, arg);
            }
            throw new RuntimeException("Unknown: " + id);
        }
        throw new RuntimeException("Unexpected: " + c);
    }

    static double applyFunc(String f, double x) {
        switch (f) {
            case "sin":   return Math.sin(isDeg ? Math.toRadians(x) : x);
            case "cos":   return Math.cos(isDeg ? Math.toRadians(x) : x);
            case "tan":   return Math.tan(isDeg ? Math.toRadians(x) : x);
            case "asin":  { double r=Math.asin(x); return isDeg?Math.toDegrees(r):r; }
            case "acos":  { double r=Math.acos(x); return isDeg?Math.toDegrees(r):r; }
            case "atan":  { double r=Math.atan(x); return isDeg?Math.toDegrees(r):r; }
            case "ln":    return Math.log(x);
            case "log":   return Math.log10(x);
            case "exp":   return Math.exp(x);
            case "sqrt":  return Math.sqrt(x);
            case "cbrt":  return Math.cbrt(x);
            case "abs":   return Math.abs(x);
            default: throw new RuntimeException("Unknown function: " + f);
        }
    }

    static double factorial(double v) {
        if (v < 0 || v != Math.floor(v)) throw new RuntimeException("Factorial needs non-negative int");
        if (v > 170) throw new RuntimeException("Overflow");
        double r = 1;
        for (int i = 2; i <= (int)v; i++) r *= i;
        return r;
    }

    static double parseNum() {
        int start = pos;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos)=='.')) pos++;
        if (pos < src.length() && (src.charAt(pos)=='e' || src.charAt(pos)=='E')) {
            pos++;
            if (pos < src.length() && (src.charAt(pos)=='+'||src.charAt(pos)=='-')) pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        }
        return Double.parseDouble(src.substring(start, pos));
    }

    static String parseIdent() {
        int start = pos;
        while (pos < src.length() && Character.isLetterOrDigit(src.charAt(pos))) pos++;
        return src.substring(start, pos);
    }

    static void skipWS() { while (pos < src.length() && src.charAt(pos)==' ') pos++; }

    // ── format result ──────────────────────────────────────────────────────
    static String fmt(double v) {
        if (Double.isNaN(v))      return "Error";
        if (Double.isInfinite(v)) return v > 0 ? "Infinity" : "-Infinity";
        if (v == Math.floor(v) && Math.abs(v) < 1e15) return String.valueOf((long)v);
        String s = String.format("%.10f", v).replaceAll("0+$","").replaceAll("\\.$","");
        return s;
    }

    // ── button actions ─────────────────────────────────────────────────────
    static void append(String s) {
        if (justEvaled) { expr.setLength(0); justEvaled = false; }
        expr.append(s);
        display.setText(expr.toString());
    }

    static void calculate() {
        String s = expr.toString().trim();
        if (s.isEmpty()) return;
        try {
            double result = eval(s);
            ansLabel.setText("Ans = " + fmt(ans));
            ans = result;
            ansLabel.setText("Ans = " + fmt(ans));
            display.setText(fmt(result));
            expr.setLength(0);
            expr.append(fmt(result));
            justEvaled = true;
        } catch (Exception ex) {
            display.setText("Error");
            expr.setLength(0);
            justEvaled = true;
        }
    }

    static void toggleInv() {
        isInv = !isInv;
        btnSin.setText(isInv ? "sin⁻¹" : "sin");
        btnCos.setText(isInv ? "cos⁻¹" : "cos");
        btnTan.setText(isInv ? "tan⁻¹" : "tan");
        btnLn.setText(isInv  ? "eˣ"    : "ln");
        btnLog.setText(isInv ? "10ˣ"   : "log");
        btnSqrt.setText(isInv ? "x²"   : "√");
        btnPow.setText(isInv  ? "ʸ√x"  : "xʸ");
    }

    // ── build a styled button ──────────────────────────────────────────────
    static JButton makeBtn(String label, Color bg, Color fg) {
        JButton b = new JButton(label);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setBorder(new RoundedBorder(14));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── main ───────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Scientific Calculator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            JPanel root = new JPanel(new BorderLayout(8, 8));
            root.setBackground(BG);
            root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            // ── display panel ────────────────────────────────────────────
            JPanel displayPanel = new JPanel(new BorderLayout());
            displayPanel.setBackground(Color.WHITE);
            displayPanel.setBorder(new RoundedBorder(16));
            displayPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(16),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));

            ansLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            ansLabel.setForeground(new Color(120,130,170));
            ansLabel.setText(" ");

            display.setFont(new Font("SansSerif", Font.BOLD, 34));
            display.setForeground(TXT_DARK);

            displayPanel.add(ansLabel, BorderLayout.NORTH);
            displayPanel.add(display,  BorderLayout.CENTER);
            root.add(displayPanel, BorderLayout.NORTH);

            // ── button grid ───────────────────────────────────────────────
            JPanel grid = new JPanel(new GridLayout(6, 7, 6, 6));
            grid.setBackground(BG);

            // Row 1: Deg | Rad | x! | ( | ) | % | AC
            btnDeg = makeBtn("Deg", BTN_MODE, TXT_BLUE);
            btnRad = makeBtn("Rad", BTN_NORM, TXT_DARK);
            JButton btnFact   = makeBtn("x!",  BTN_FUNC, TXT_BLUE);
            JButton btnLP     = makeBtn("(",   BTN_NORM, TXT_BLUE);
            JButton btnRP     = makeBtn(")",   BTN_NORM, TXT_BLUE);
            JButton btnPct    = makeBtn("%",   BTN_NORM, TXT_DARK);
            JButton btnAC     = makeBtn("AC",  BTN_NORM, new Color(220,60,60));

            // Row 2: Inv | sin | ln | 7 | 8 | 9 | ÷
            JButton btnInv    = makeBtn("Inv", BTN_FUNC, TXT_BLUE);
            btnSin            = makeBtn("sin", BTN_FUNC, TXT_BLUE);
            btnLn             = makeBtn("ln",  BTN_FUNC, TXT_BLUE);
            JButton btn7      = makeBtn("7",   BTN_NORM, TXT_DARK);
            JButton btn8      = makeBtn("8",   BTN_NORM, TXT_DARK);
            JButton btn9      = makeBtn("9",   BTN_NORM, TXT_DARK);
            JButton btnDiv    = makeBtn("÷",   BTN_NORM, TXT_BLUE);

            // Row 3: π | cos | log | 4 | 5 | 6 | ×
            JButton btnPi     = makeBtn("π",   BTN_FUNC, TXT_BLUE);
            btnCos            = makeBtn("cos", BTN_FUNC, TXT_BLUE);
            btnLog            = makeBtn("log", BTN_FUNC, TXT_BLUE);
            JButton btn4      = makeBtn("4",   BTN_NORM, TXT_DARK);
            JButton btn5      = makeBtn("5",   BTN_NORM, TXT_DARK);
            JButton btn6      = makeBtn("6",   BTN_NORM, TXT_DARK);
            JButton btnMul    = makeBtn("×",   BTN_NORM, TXT_BLUE);

            // Row 4: e | tan | √ | 1 | 2 | 3 | −
            JButton btnE      = makeBtn("e",   BTN_FUNC, TXT_BLUE);
            btnTan            = makeBtn("tan", BTN_FUNC, TXT_BLUE);
            btnSqrt           = makeBtn("√",   BTN_FUNC, TXT_BLUE);
            JButton btn1      = makeBtn("1",   BTN_NORM, TXT_DARK);
            JButton btn2      = makeBtn("2",   BTN_NORM, TXT_DARK);
            JButton btn3      = makeBtn("3",   BTN_NORM, TXT_DARK);
            JButton btnSub    = makeBtn("−",   BTN_NORM, TXT_BLUE);

            // Row 5: Ans | EXP | xʸ | 0 | . | = | +
            JButton btnAns    = makeBtn("Ans", BTN_FUNC, TXT_BLUE);
            JButton btnEXP    = makeBtn("EXP", BTN_FUNC, TXT_BLUE);
            btnPow            = makeBtn("xʸ",  BTN_FUNC, TXT_BLUE);
            JButton btn0      = makeBtn("0",   BTN_NORM, TXT_DARK);
            JButton btnDot    = makeBtn(".",   BTN_NORM, TXT_DARK);
            JButton btnEq     = makeBtn("=",   BTN_EQ,   TXT_EQ);
            JButton btnAdd    = makeBtn("+",   BTN_NORM, TXT_BLUE);

            // make = wider visually with bold font
            btnEq.setFont(new Font("SansSerif", Font.BOLD, 18));

            // ── add to grid ───────────────────────────────────────────────
            for (JButton b : new JButton[]{
                btnDeg, btnRad, btnFact, btnLP,  btnRP,  btnPct, btnAC,
                btnInv, btnSin, btnLn,  btn7,   btn8,   btn9,   btnDiv,
                btnPi,  btnCos, btnLog, btn4,   btn5,   btn6,   btnMul,
                btnE,   btnTan, btnSqrt,btn1,   btn2,   btn3,   btnSub,
                btnAns, btnEXP, btnPow, btn0,   btnDot, btnEq,  btnAdd
            }) {
                b.setPreferredSize(new Dimension(64, 48));
                grid.add(b);
            }

            root.add(grid, BorderLayout.CENTER);
            frame.setContentPane(root);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // ── wire up actions ───────────────────────────────────────────

            // Digit / dot
            btn0.addActionListener(e -> append("0"));
            btn1.addActionListener(e -> append("1"));
            btn2.addActionListener(e -> append("2"));
            btn3.addActionListener(e -> append("3"));
            btn4.addActionListener(e -> append("4"));
            btn5.addActionListener(e -> append("5"));
            btn6.addActionListener(e -> append("6"));
            btn7.addActionListener(e -> append("7"));
            btn8.addActionListener(e -> append("8"));
            btn9.addActionListener(e -> append("9"));
            btnDot.addActionListener(e -> append("."));

            // Operators
            btnAdd.addActionListener(e -> append("+"));
            btnSub.addActionListener(e -> append("-"));
            btnMul.addActionListener(e -> append("*"));
            btnDiv.addActionListener(e -> append("/"));
            btnPct.addActionListener(e -> append("%"));
            btnLP.addActionListener(e  -> append("("));
            btnRP.addActionListener(e  -> append(")"));

            // Powers / roots
            btnPow.addActionListener(e -> {
                if (isInv) append("^(1/");   // ʸ√x  →  x^(1/y)
                else       append("^");
            });
            btnSqrt.addActionListener(e -> {
                if (isInv) append("^2");     // x²
                else       append("sqrt(");  // √
            });

            // Factorial
            btnFact.addActionListener(e -> append("!"));

            // EXP (scientific notation  ×10^)
            btnEXP.addActionListener(e -> append("*10^"));

            // Constants
            btnPi.addActionListener(e  -> append("pi"));
            btnE.addActionListener(e   -> append("e"));
            btnAns.addActionListener(e -> append("ans"));

            // Trig / log (toggled by Inv)
            btnSin.addActionListener(e -> append(isInv ? "asin(" : "sin("));
            btnCos.addActionListener(e -> append(isInv ? "acos(" : "cos("));
            btnTan.addActionListener(e -> append(isInv ? "atan(" : "tan("));
            btnLn.addActionListener(e  -> {
                if (isInv) append("exp(");   // eˣ
                else       append("ln(");
            });
            btnLog.addActionListener(e -> {
                if (isInv) append("10^(");   // 10ˣ
                else       append("log(");
            });

            // Deg / Rad toggle
            btnDeg.addActionListener(e -> {
                isDeg = true;
                btnDeg.setBackground(BTN_MODE);
                btnRad.setBackground(BTN_NORM);
            });
            btnRad.addActionListener(e -> {
                isDeg = false;
                btnRad.setBackground(BTN_MODE);
                btnDeg.setBackground(BTN_NORM);
            });

            // Inv toggle
            btnInv.addActionListener(e -> {
                toggleInv();
                btnInv.setBackground(isInv ? new Color(180,195,255) : BTN_FUNC);
            });

            // AC (all clear)
            btnAC.addActionListener(e -> {
                expr.setLength(0);
                display.setText("0");
                ansLabel.setText(" ");
                ans = 0;
                justEvaled = false;
            });

            // =
            btnEq.addActionListener(e -> calculate());

            // keyboard support
            frame.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (Character.isDigit(c)||c=='.'||c=='+'||c=='-'||c=='*'||c=='/'||c=='('||c==')'||c=='%') {
                        append(String.valueOf(c));
                    } else if (c=='^') {
                        append("^");
                    } else if (c=='\n'||c=='=') {
                        calculate();
                    } else if (e.getKeyCode()==KeyEvent.VK_BACK_SPACE) {
                        if (expr.length()>0) {
                            expr.deleteCharAt(expr.length()-1);
                            display.setText(expr.length()==0 ? "0" : expr.toString());
                        }
                    }
                }
            });
            frame.setFocusable(true);
            frame.requestFocus();
        });
    }

    // ── rounded border helper ─────────────────────────────────────────────
    static class RoundedBorder implements Border {
        private final int r;
        RoundedBorder(int r) { this.r = r; }
        public Insets getBorderInsets(Component c) { return new Insets(4,8,4,8); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200,210,240));
            g2.drawRoundRect(x,y,w-1,h-1,r,r);
            g2.dispose();
        }
    }
}
