package com.mycompany.swing;
import javax.swing.*;
import java.awt.event.*;

public class SWING {

    // evaluates expressions like "343+343+343" or "10*2-5"
    static double evaluate(String expr) {
        expr = expr.trim();

        // scan right-to-left for + or - (lowest precedence)
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if ((c == '+' || c == '-') && i > 0) {
                double left  = evaluate(expr.substring(0, i));
                double right = evaluate(expr.substring(i + 1));
                return c == '+' ? left + right : left - right;
            }
        }

        // scan right-to-left for * or / (higher precedence)
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if ((c == '*' || c == '/') && i > 0) {
                double left  = evaluate(expr.substring(0, i));
                double right = evaluate(expr.substring(i + 1));
                return c == '*' ? left * right : left / right;
            }
        }

        return Double.parseDouble(expr);
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Calculator");

        JTextField text = new JTextField();

        JButton b1 = new JButton("1");
        JButton b2 = new JButton("2");
        JButton b3 = new JButton("3");
        JButton b4 = new JButton("4");
        JButton b5 = new JButton("5");
        JButton b6 = new JButton("6");
        JButton b7 = new JButton("7");
        JButton plus = new JButton("+");
        JButton equal = new JButton("=");
        JButton sinBtn = new JButton("sin");
        JButton cosBtn = new JButton("cos");
        JButton closeParen = new JButton(")");
        JButton clear = new JButton("C");

        // positions
        text.setBounds(50, 30, 200, 30);

        b1.setBounds(50, 80, 50, 50);
        b2.setBounds(110, 80, 50, 50);
        b3.setBounds(170, 80, 50, 50);

        plus.setBounds(50, 140, 50, 50);
        equal.setBounds(110, 140, 50, 50);
        clear.setBounds(170, 140, 50, 50);

        sinBtn.setBounds(50, 200, 65, 50);
        cosBtn.setBounds(125, 200, 65, 50);
        closeParen.setBounds(200, 200, 40, 50);

        // add buttons
        frame.add(text);
        frame.add(b1);
        frame.add(b2);
        frame.add(b3);
        frame.add(plus);
        frame.add(equal);
        frame.add(clear);
        frame.add(sinBtn);
        frame.add(cosBtn);
        frame.add(closeParen);

        // button actions
        b1.addActionListener(e -> text.setText(text.getText() + "1"));
        b2.addActionListener(e -> text.setText(text.getText() + "2"));
        b3.addActionListener(e -> text.setText(text.getText() + "3"));

        plus.addActionListener(e -> text.setText(text.getText() + "+"));

        sinBtn.addActionListener(e -> text.setText(text.getText() + "sin("));
        cosBtn.addActionListener(e -> text.setText(text.getText() + "cos("));

        closeParen.addActionListener(e -> text.setText(text.getText() + ")"));

        clear.addActionListener(e -> text.setText(""));

        equal.addActionListener(e -> {
            String s = text.getText().trim();
            try {
                if (s.startsWith("sin(") && s.endsWith(")")) {
                    // evaluate whatever is inside sin(...) first, then apply sin
                    double inner = evaluate(s.substring(4, s.length() - 1));
                    double result = Math.sin(Math.toRadians(inner));
                    text.setText(String.format("%.4f", result));
                } else if (s.startsWith("cos(") && s.endsWith(")")) {
                    // evaluate whatever is inside cos(...) first, then apply cos
                    double inner = evaluate(s.substring(4, s.length() - 1));
                    double result = Math.cos(Math.toRadians(inner));
                    text.setText(String.format("%.4f", result));
                } else {
                    // plain expression like 1+2 or 3*4
                    double result = evaluate(s);
                    text.setText(String.format("%.4f", result).replaceAll("\\.?0+$", ""));
                }
            } catch (Exception ex) {
                text.setText("Error");
            }
        });

        // frame settings
        frame.setSize(300, 320);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
