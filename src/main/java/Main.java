import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static List<ArrayBlockingQueue<String>> queueList = Stream
            .generate(() -> new ArrayBlockingQueue<String>(100))
            .limit("abc".length())
            .collect(Collectors.toList());

    public static void main(String[] args) throws InterruptedException {
        Thread textGenerator = new Thread(() ->
        {
            for (int i = 0; i < 10_000; i++) {
                String text = generateText("abc", 100_000);
                try {
                    for (int j = 0; j < "abc".length(); j++) {
                        queueList.get(j).put(text);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        textGenerator.start();

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < "abc".length(); i++) {

            int finalI = i;
            Thread thread = new Thread(() -> {
                char ch = "abc".charAt(finalI);
                int maxA = findMaxCharCount(queueList.get(finalI), ch);
                System.out.println("Наибольшее количество символов " + ch + " в строке " + "равно " + maxA);
            });

            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private static int findMaxCharCount(BlockingQueue<String> queue, char letter) {
        int count = 0;
        int max = 0;
        String text;
        try {
            for (int i = 0; i < 10_000; i++) {
                text = queue.take();
                for (char c : text.toCharArray()) {
                    if (c == letter) count++;
                }
                if (count > max) max = count;
                count = 0;
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " was interrupted");
            return -1;
        }
        return max;
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}
