package org;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final int cyclesAmount = 100_000;
    public static BlockingQueue<String> textsA = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> textsB = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> textsC = new ArrayBlockingQueue<>(100);
    static AtomicInteger aCounter = new AtomicInteger(0);
    static AtomicInteger bCounter = new AtomicInteger(0);
    static AtomicInteger cCounter = new AtomicInteger(0);
    static List<Thread> blockingQueueThreads = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        Thread textGeneratorThread = new Thread(() -> {
            for (int i = 0; i < cyclesAmount; i++) {
                String generatedText = generateText("abc", 100_000);

                try {
                    textsA.put(generatedText);
                    textsB.put(generatedText);
                    textsC.put(generatedText);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        Thread textsACountThread = generateLetterIncrementerThread('a', textsA, aCounter);
        Thread textsBCountThread = generateLetterIncrementerThread('b', textsB, bCounter);
        Thread textsCCountThread = generateLetterIncrementerThread('c', textsC, cCounter);

        textGeneratorThread.start();
        textsACountThread.start();
        textsBCountThread.start();
        textsCCountThread.start();

        blockingQueueThreads.add(textGeneratorThread);
        blockingQueueThreads.add(textsACountThread);
        blockingQueueThreads.add(textsBCountThread);
        blockingQueueThreads.add(textsCCountThread);

        for (Thread blockingQueueThread : blockingQueueThreads) {
            blockingQueueThread.join();
        }

        System.out.println("Максимальное количество символов a: " + aCounter.get() + " шт");
        System.out.println("Максимальное количество символов b: " + bCounter.get() + " шт");
        System.out.println("Максимальное количество символов c: " + cCounter.get() + " шт");
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    public static Thread generateLetterIncrementerThread(
        char letter,
        BlockingQueue<String> texts,
        AtomicInteger counter
    ) {
        return new Thread(() -> {
            for (int i = 0; i < cyclesAmount; i++) {
                try {
                    int nextLetterCount = getLetterCount(letter, texts.take());
                    int currentTextsBMaxCount = counter.get();

                    if (nextLetterCount > currentTextsBMaxCount) {
                        counter.set(nextLetterCount);
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
    }

    public static int getLetterCount(char letter, String text) {
        int letterCount = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == letter) {
                letterCount++;
            }
        }

        return letterCount;
    }
}