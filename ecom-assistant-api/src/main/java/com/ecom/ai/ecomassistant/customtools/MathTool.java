package com.ecom.ai.ecomassistant.customtools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Component
public class MathTool {
    @Tool(description = "Calculate the nth Fibonacci number (0-based index)")
    public long fibonacci(int n) {
        if (n < 0) throw new IllegalArgumentException("Index must be non-negative");
        if (n == 0) return 0;
        if (n == 1) return 1;

        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long next = a + b;
            a = b;
            b = next;
        }
        return b;
    }

    @Tool(description = "Check if a number is prime")
    public boolean isPrime(int number) {
        if (number <= 1) return false;
        return IntStream.rangeClosed(2, (int) Math.sqrt(number))
                .allMatch(n -> number % n != 0);
    }

    @Tool(description = "Calculate factorial of a number")
    public long factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative numbers not allowed");
        return IntStream.rangeClosed(1, n)
                .reduce(1, (a, b) -> a * b);
    }

    @Tool(description = "Solve a quadratic equation ax^2 + bx + c = 0. Returns real roots if any.")
    public String solveQuadratic(double a, double b, double c) {
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return "No real roots.";
        } else if (discriminant == 0) {
            double root = -b / (2 * a);
            return "One real root: x = " + root;
        } else {
            double root1 = (-b + Math.sqrt(discriminant)) / (2 * a);
            double root2 = (-b - Math.sqrt(discriminant)) / (2 * a);
            return "Two real roots: x1 = " + root1 + ", x2 = " + root2;
        }
    }
}
