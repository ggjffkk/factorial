import java.math.BigInteger;
import java.util.Scanner;
import java.util.concurrent.*;

public class factorial {
    private final ConcurrentHashMap<Integer, BigInteger> factorialMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean cancelled = false;

    public static void main(String[] args) {
        factorial calculator = new factorial();
        calculator.run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Введіть число, щоб визначити факторіал (або 'вийти' для завершення): ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("вийти")) {
                System.out.println("Завершення роботи.");
                break;
            }

            try {
                int number = Integer.parseInt(input);
                if (number < 0) {
                    System.out.println("Факторіал не визначений для від'ємних чисел.");
                    continue;
                }

                Future<BigInteger> future = executorService.submit(new FactorialTask(number));

                // перевіряю на скасування
                if (cancelled) {
                    System.out.println("Обчислення було скасовано.");
                    break;
                }

                // результат
                BigInteger result = future.get();
                factorialMap.put(number, result);
                System.out.println("Результат: " + number + "! = " + result);

            } catch (NumberFormatException e) {
                System.out.println("Будь ласка, введіть дійсне число.");
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Сталася помилка при обчисленні факторіалу: " + e.getMessage());
            }
        }

        executorService.shutdown();
        scanner.close();
    }

    private class FactorialTask implements Callable<BigInteger> {
        private final int number;

        public FactorialTask(int number) {
            this.number = number;
        }

        @Override
        public BigInteger call() {
            if (cancelled) {
                throw new CancellationException("Обчислення скасовано.");
            }
            return calculateFactorial(number);
        }

        private BigInteger calculateFactorial(int n) {
            BigInteger result = BigInteger.ONE;
            for (int i = 2; i <= n; i++) {
                result = result.multiply(BigInteger.valueOf(i));
            }
            return result;
        }
    }
}